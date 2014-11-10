package com.logica.ndk.tm.utilities.transformation.scantailor;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.FileIOUtils;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.wf.task.Scan;

public class RunScantailorPreprocessImpl extends RunScantailorAbstract {
  private static final String STATEPP_PARAMETER_NAME = "statePP";
  private static final int PP_SUCCESS = 2;
  private static final String PROCESS_TYPE = "preprocess";
  private static final String SCANTAILOR_CONFIG_EXT = "scanTailor"; 
  
  public int execute(final String cdmId, String profile, String colorMode, String cropType, 
  		Integer dimensionX, Integer dimensionY,	Integer outputDpi) {
    if (outputDpi == 0) {
      outputDpi = TmConfig.instance().getInt("utility.scantailor.defaultDpi");
    }
    removeInvalidConfigFiles(cdmId);
    return super.execute(cdmId, profile, colorMode, cropType, dimensionX, dimensionY, outputDpi);
  }
  
	@Override
	protected Collection<File> getScantailorProjectFiles(String cdmId) {
		return FileUtils.listFiles(cdm.getScantailorConfigsDir(cdmId), new String[] {SCANTAILOR_CONFIG_EXT}, false);
	}

	@Override
	protected void updateConfigFile(File configFile, String cdmId) {
		_updateConfigFile(configFile, cdm.getJpgTiffImagePath(cdmId).getAbsolutePath(), 
  			cdm.getScantailorTempOutDir(cdmId).getAbsolutePath());
	}

	@Override
	protected boolean getValidity(String cdmId, String scanId) {
		int statePP = Integer.parseInt(getScanParameter(cdmId, scanId, STATEPP_PARAMETER_NAME));
   	if (statePP == PP_SUCCESS) {
  		log.debug("Going to skip preprocessing for already processed scan");
  		return false;
   	}
   	return super.getValidity(cdmId, scanId);
	}

	@Override
	protected int getNumberOfFilesAfterProcess(int estimation, String cdmId) {
		return estimation;
	}

	@Override
	protected String getProcessType() {
		return PROCESS_TYPE;
	}
	
	@Override
	protected String getSTOutputDir(String cdmId) {
		return cdm.getScantailorTempOutDir(cdmId).getAbsolutePath();
	}
	
	private void removeInvalidConfigFiles(String cdmId) {
		List<String> profiles = Arrays.asList(SCANTAILOR_PROFILES);
	  List<Scan> scansList = getScansListFromCsv(cdmId, cdm);
    for (Scan scan:scansList) {      
      if (!profiles.contains(scan.getProfilePPCode())) {
      	File scanTailorPrj = new File(cdm.getScantailorConfigsDir(cdmId), scan.getScanId() + "." + SCANTAILOR_CONFIG_EXT);
        File scanTailorPrjTempout = new File(cdm.getScantailorTempOutDir(cdmId), scan.getScanId() + "." + SCANTAILOR_CONFIG_EXT);
      	if (scanTailorPrj.exists()) {
        	// backup ST configuration file, then delete it 
        	FileIOUtils.copyFile(scanTailorPrj, new File(cdm.getBackupDir(cdmId), scanTailorPrj.getName() + "-" + getDateTime()), true);        
        	if (!scanTailorPrj.delete()) {
        		throw new SystemException("Cannot delete stConfig: " + scanTailorPrj, ErrorCodes.FILE_DELETE_FAILED);
        	}      		      		       		
      	}
        if (scanTailorPrjTempout.exists()) {
          // backup ST configuration file, then delete it 
          FileIOUtils.copyFile(scanTailorPrjTempout, new File(cdm.getBackupDir(cdmId), scanTailorPrjTempout.getName() + "-tempout-" + getDateTime()), true);        
          if (!scanTailorPrjTempout.delete()) {
            throw new SystemException("Cannot delete stConfig: " + scanTailorPrjTempout, ErrorCodes.FILE_DELETE_FAILED);
          }                             
        }
      }
    }
	}
	
}
