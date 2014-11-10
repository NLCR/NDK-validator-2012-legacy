package com.logica.ndk.tm.utilities.wa;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;

public class HarvestLogImpl extends AbstractUtility {
	//static final List<String> logFiles = Arrays.asList(TmConfig.instance().getStringArray("import.harvest.logFiles"));
	//static final List<String> filesToConvert = Arrays.asList(TmConfig.instance().getStringArray("import.harvest.logToConvert"));
	//HashMap<String, String> mapOfLogs2Convert = new HashMap<String, String>();
	
	public String execute(String cdmId) throws SystemException {
		try {
	    final File rawDataDir = new File(cdm.getRawDataDir(cdmId).getAbsolutePath());
	    final File dataDir = new File(cdm.getLogsDataDir(cdmId).getAbsolutePath());
	    if(!dataDir.exists()) {
	      if (dataDir.mkdirs() == false) {
          throw new SystemException("Error creating dir:" + dataDir.getAbsoluteFile(), ErrorCodes.CREATING_DIR_FAILED);
        }
	    }
	    //initializeLogs2ConvertHashMap();
	    //Set<String> origNames = mapOfLogs2Convert.keySet();

	    final File[] files = rawDataDir.listFiles();
	    for (File dataFile : files) {
	      if (dataFile.isFile()) {
	      	//String shortFilename = logFile.getName().toLowerCase();
	      	//if (logFiles.contains(shortFilename)) {
	      		retriedCopyFileToDirectory(dataFile, dataDir);
	      	//} else if (origNames.contains(shortFilename)) {
	      		//retriedCopyFile(logFile, new File(logDir, mapOfLogs2Convert.get(shortFilename)));
	      	//}
	      } else {
	        retriedCopyDirectoryToDirectory(dataFile, dataDir);
	      }
	    } 
		} catch (Exception e) {
			throw new SystemException("Error while copying log files for harvest", ErrorCodes.COPY_FILES_FAILED);
		}
		return cdmId;
	}
	
	/**
	 * Creates map with pairs: origin filename, new filename (eg. for renaming files)
	 * @throws SystemException
	 */
  /*private void initializeLogs2ConvertHashMap() throws SystemException {
    for (int i = 0; i < filesToConvert.size(); i++) {
      String pairOfMapping = filesToConvert.get(i);
      String[] pair = pairOfMapping.split("=>");
      if (pair.length == 2) {
      	mapOfLogs2Convert.put(pair[0], pair[1]);
      } else {        
        log.error("Bad configuration in tm-config-defaults.xml file: " + pair);
        throw new SystemException("Bad configuration in tm-config-defaults.xml file: " + pair,ErrorCodes.INCORRECT_CONFIGURATION);       
      }
    }
  }*/
  
  
  
  @RetryOnFailure(attempts = 3)
  private void retriedCopyFile(File source, File destination) throws IOException {
      FileUtils.copyFile(source, destination);
  }
  
  @RetryOnFailure(attempts = 3)
  private void retriedCopyDirectoryToDirectory(File source, File destination) throws IOException {
      FileUtils.copyDirectoryToDirectory(source, destination);
  }
  
}
