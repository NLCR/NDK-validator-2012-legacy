package com.logica.ndk.tm.utilities.file;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;

public class AnalyzeImportImpl extends AbstractUtility {
  
  private static final String IMPORT_K4 = TmConfig.instance().getString("import.type.k4");
  
  private static final String RESPONSE_DEFAULT = "OK";
  private static final String RESPONSE_DJVU = TmConfig.instance().getString("import.kramerius.response.djvu");
  private static final String RESPONSE_JPEG = TmConfig.instance().getString("import.kramerius.response.jpeg");

  private static final String FOLDER_IMG = TmConfig.instance().getString("import.kramerius.djvuFolder");
  private static final String FOLDER_IMG_AMD = TmConfig.instance().getString("import.kramerius.djvuAmdFolder");
  private static final String FOLDER_JPEG = TmConfig.instance().getString("import.kramerius.jpegFolder");
  private static final String FOLDER_JPEG_AMD = TmConfig.instance().getString("import.kramerius.jpegAmdFolder");

  private static final String EXT_DJVU = TmConfig.instance().getString("import.kramerius.djvuExt");
  private static final String EXT_JPEG = TmConfig.instance().getString("import.kramerius.jpegExt");
  private static final String EXT_AMD = TmConfig.instance().getString("import.kramerius.amdExt");
  
	public String execute(String cdmId) {
		Preconditions.checkNotNull(cdmId);
		log.info("Utility AnalyzeImport started. cdmId:" + cdmId);

		String importType = cdm.getCdmProperties(cdmId).getProperty("importType");
		log.debug("ImportType: " + importType);

		if (importType != null && importType.equals(IMPORT_K4)) {

			// check if raw data contains JPEG files to convert from
			File jpegDir = new File(cdm.getRawDataDir(cdmId), FOLDER_JPEG);
			if (jpegDir.exists() && jpegDir.isDirectory()) {
				log.info("JPEG folder exists.");

				// check if administrative metadata folder exists
				File amdDir = new File(cdm.getRawDataDir(cdmId), FOLDER_JPEG_AMD);
				if (amdDir.exists() && jpegDir.isDirectory()) {
					log.info("AMD folder exists.");

					// check if count of files are matching
					String[] jpegFilter = { EXT_JPEG };
					String[] amdFilter = { EXT_AMD };
					int numOfJpegs = FileUtils.listFiles(jpegDir, jpegFilter,false).size();
					int numOfAmds = FileUtils.listFiles(amdDir, amdFilter,false).size();

					if (numOfAmds == numOfJpegs) {
						log.info("Count of JPEG and AMD files match, JPEG conversion.");
						return RESPONSE_JPEG;
					}
					log.info("Count of JPEG and AMD files does not match, trying DJVU.");
				}
			}
			
			File imgDir = new File(cdm.getRawDataDir(cdmId), FOLDER_IMG);
			if (imgDir.exists() && imgDir.isDirectory()) {
				log.info("IMG folder exists.");

				// check if administrative metadata folder exists
				File amdDir = new File(cdm.getRawDataDir(cdmId), FOLDER_IMG_AMD);
				if (amdDir.exists() && imgDir.isDirectory()) {
					log.info("AMD folder exists.");

					// check if count of files are matching
					String[] djvuFilter = { EXT_DJVU };
					String[] amdFilter = { EXT_AMD };
					int numOfDjvus = FileUtils.listFiles(imgDir, djvuFilter,false).size();
					int numOfAmds = FileUtils.listFiles(amdDir, amdFilter,false).size();

					if (numOfAmds == numOfDjvus) {
						log.info("Count of DJVU and AMD files match, DJVU conversion.");
						return RESPONSE_DJVU;
					}
					log.info("Count of DJVU and AMD files does not match, throwing exception.");
				}
			}
			
			log.error("Count of JPEG/DJVU with their respecitve AMD files does not match.");
			throw new SystemException("Count of JPEG/DJVU with their respecitve AMD files does not match.", ErrorCodes.ERROR_DURING_VALIDATION);

		}

		log.info("Using default response of Analyze import - OK.");
		return RESPONSE_DEFAULT;
	}

}
