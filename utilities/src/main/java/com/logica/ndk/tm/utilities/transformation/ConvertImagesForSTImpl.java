package com.logica.ndk.tm.utilities.transformation;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.FileIOUtils;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.commandline.CmdLineAdvancedImpl;

public class ConvertImagesForSTImpl extends CmdLineAdvancedImpl {
	private static final String flatDataJpg = "flatDataJpg";

	private static final int priorityCount;
	private static final HashMap<Integer, List<File>> storages = new HashMap<Integer, List<File>>();
	private static final long diskSpaceTreshold;
	private static final IOFileFilter fileFilter;

	static {
		priorityCount = TmConfig.instance().getInt("utility.convertImagesForST.priorityCount");
		for (int i = 0; i < priorityCount; i++) {
			ArrayList<File> list = new ArrayList<File>();
			for (Object storageName: TmConfig.instance().getList("utility.convertImagesForST.priority" + String.valueOf(i))) {
				list.add(new File(storageName.toString()));
			}
			storages.put(Integer.valueOf(i), list);
		}
		diskSpaceTreshold = TmConfig.instance().getLong("utility.convertImagesForST.diskSpaceTreshold");
		fileFilter = new WildcardFileFilter(TmConfig.instance().getStringArray("utility.flatData.imgExt"), IOCase.INSENSITIVE);
	}
	
  public String execute(String imageDirName, String cdmId) throws BusinessException, SystemException {
		log.info("Converting tiff for ScanTailor started");
		checkNotNull(cdmId, "cdmId argument must not be null");
		log.info("Image directory for convertig: " + imageDirName);
		File imageDir = new File(imageDirName);
		List<File> imageFiles = (List<File>) FileUtils.listFiles(imageDir, fileFilter, FileFilterUtils.falseFileFilter());
		File jpgTiffLocationFile = cdm.getJpgTiffLocationFile(cdmId);
		File dir4convertedFiles = jpgTiffLocationFile.canRead() ? 
				cdm.getJpgTiffImagePath(cdmId) : new File(new File(getBaseDir(), cdmId), flatDataJpg);
		if (!dir4convertedFiles.exists() && !dir4convertedFiles.mkdirs()) {
			throw new SystemException("Error creating dir:" + dir4convertedFiles, ErrorCodes.CREATING_DIR_FAILED);
		}
		String dirPath4convertedFiles = dir4convertedFiles.getAbsolutePath();
		for (File imgFile : imageFiles) {
			String imgFileName = imgFile.getName();
			File convertedFile = new File(dir4convertedFiles, imgFileName);
			if (isConvNeeded(imgFile, convertedFile)) {
				log.info("Converting TIFF: {} to JPG-TIFF: {}.", imgFileName, convertedFile);
				execute("utility.convertImagesForST", imgFile.getAbsolutePath(), dirPath4convertedFiles);
			} else {
				log.info("Converted JPG-TIFF: {} already exists and is newer than source. Skipping conversion.", convertedFile);
			}
		}
		if (!jpgTiffLocationFile.exists()) {
			writeImagePath(cdmId, dirPath4convertedFiles);
		}
    return ResponseStatus.RESPONSE_OK;
  }
  
	private File getBaseDir() {
		for (int i = 0; i < priorityCount; i++) {
			File[] storagesInSamePriority = storages.get(i).toArray(new File[storages.get(i).size()]);		                    
			Arrays.sort(storagesInSamePriority, new Comparator<File>() {
				public int compare(File f1, File f2) {
					return Long.valueOf(f1.getUsableSpace()).compareTo(
							f2.getUsableSpace());
				}
			});
			File maxFreeStorage = storagesInSamePriority[storagesInSamePriority.length-1];                                               	
			if (maxFreeStorage.getUsableSpace() > diskSpaceTreshold) {
				return maxFreeStorage;
			}
		}
		log.error("There is insufficient free disk space in all storages");
		throw new SystemException("There is insufficient free disk space in all storages");
	}
	
	private void writeImagePath(String cdmId, String imagePath) {
		File scantailorConfigDir = cdm.getScantailorConfigsDir(cdmId);
		if (!scantailorConfigDir.exists()) {
			if (!scantailorConfigDir.mkdirs()) {
				throw new SystemException("Error while creating scantailorConfigDir. ", ErrorCodes.CREATING_DIR_FAILED);
			}
		}
		FileIOUtils.writeToFile(cdm.getJpgTiffLocationFile(cdmId), imagePath, "jpgTiffLocation file");
	}
			
}
