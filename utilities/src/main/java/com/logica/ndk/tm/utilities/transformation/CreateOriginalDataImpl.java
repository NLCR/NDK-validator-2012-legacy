/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord.Operation;

/**
 * @author kovalcikm
 */
public class CreateOriginalDataImpl extends AbstractUtility {

  public static final int BUFFER_SIZE = 1024;
  public static final String AGENT = "NDK-TM";
  public static final String AGENT_VERSION = "1.0";
  private static final String PRESERVATION_LEVEL_VALUE = "preservation";

  public String execute(String cdmId) {
    checkNotNull(cdmId);
    log.info("Utility CreateOriginalData started.");
    CDM cdm = new CDM();
    File originalDataDir = cdm.getOriginalDataDir(cdmId);

    if (!originalDataDir.exists()) {
      originalDataDir.mkdir();
    }

    Collection<File> originalDataFiles = FileUtils.listFiles(cdm.getRawDataDir(cdmId), FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter());
    IOFileFilter fileFilter = new WildcardFileFilter(TmConfig.instance().getStringArray("utility.fileChar.imgExtensions"), IOCase.INSENSITIVE);
    Collection<File> rawDataImgFiles = FileUtils.listFiles(cdm.getRawDataDir(cdmId), fileFilter, FileFilterUtils.trueFileFilter());

    if (!cdm.getOriginalDataDir(cdmId).exists()) {
      cdm.getOriginalDataDir(cdmId).mkdir();
    }

    for (File file : rawDataImgFiles) {
      originalDataFiles.remove(file);
    }
    int counter = 0;

    for (File file : originalDataFiles) {
      try {
        //FileUtils.copyFileToDirectory(file, originalDataDir);
        retriedCopyFileToDirectory(file, originalDataDir);
      }
      catch (IOException e) {
        throw new SystemException("Copy file to directory failed.", ErrorCodes.COPY_FILES_FAILED);
      }
    }

    log.info("Utility CreateOriginalData finished. " + counter + " files copied to originalData.");
    return ResponseStatus.RESPONSE_OK;
  }
  
}
