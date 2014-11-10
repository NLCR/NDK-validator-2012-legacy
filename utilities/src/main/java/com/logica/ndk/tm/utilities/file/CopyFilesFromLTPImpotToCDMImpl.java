package com.logica.ndk.tm.utilities.file;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.io.CopyToImpl;

/** Utility copy files from LTP import folder to CDM designated to update (rescan, addScan).
 * 
 * 
 * @author brizat
 *
 */
public class CopyFilesFromLTPImpotToCDMImpl extends AbstractUtility{
  
  private static String FLAG_FROM_LTP_FILE_NAME = "importLTP.flg";
  private static String DONE_PREFIX = "done_";
  private static String PROCESING_PREFIX = "procesing_";
  
  private CDM cdm = new CDM();
  private HashMap<String, String> copyDirs;
  
  /**
   * @param cdmId
   * @param sourcePath
   * @return number of files in mc (used for pageCount and scanCount)
   */
  public String execute(String cdmId, String sourcePath){
    log.info("Utility CopyFilesFromLTPImpotToCDMImpl started");
    log.info(String.format("CdmId: %s, source path: %s",  cdmId, sourcePath));
    File sourceDir = new File(sourcePath);
    if(!sourceDir.exists() || !sourceDir.isDirectory()){
      log.error(String.format("Source dir %s does not exist or not directory.", sourceDir.getAbsoluteFile()));
      throw new BusinessException(String.format("Source dir %s does not exist or not directory", sourceDir.getAbsoluteFile()), ErrorCodes.IMPORT_LTP_DIR_DOES_NOT_EXIST);
    }
    
    //create import lpt flag file
    File importLTPFlag = new File(cdm.getCdmDir(cdmId), FLAG_FROM_LTP_FILE_NAME);
    if(!importLTPFlag.exists()){
      try {
        importLTPFlag.createNewFile();
      }
      catch (IOException e) {
        log.error("Error while creating import from ltp flag file " + importLTPFlag.getAbsolutePath());
        throw new SystemException("Error while creating import from ltp flag file " + importLTPFlag.getName(), ErrorCodes.IMPORT_LTP_FLAG_FILE_CREATE_FAILED);
      }
    }
    
    File cdmDataDir = cdm.getCdmDataDir(cdmId);
    if(!cdmDataDir.exists()){
      log.error(String.format("Target cdm with id %s does not exist.", cdmId));
      throw new BusinessException(String.format("Target cdm with id %s does not exist.", cdmId), ErrorCodes.IMPORT_LTP_TARGET_CDM_DOES_NOT_EXIST);
    }
    
    initCopyDirsHashMap();
    
    CopyToImpl copy = new CopyToImpl();
    Iterator<String> it = copyDirs.keySet().iterator();
    
    //Copy folders
    while (it.hasNext()) {
      String key = it.next();
      copy.copy(sourceDir.getAbsolutePath() + File.separator + key, cdmDataDir.getAbsolutePath() + File.separator + copyDirs.get(key));
    }
    
    //copy files
    copy.copy(sourceDir.getAbsolutePath(), cdmDataDir.getAbsolutePath());
    
    //rename import folder to done
    //File renameFile = new File(sourceDir.getParentFile(), DONE_PREFIX + sourceDir.getName().substring(PROCESING_PREFIX.length()));
    //sourceDir.renameTo(renameFile);
    
    int numberOfFilesInMCDir = cdm.getMasterCopyDir(cdmId).listFiles().length;
    log.info(String.format("Number of files in MC: %d", numberOfFilesInMCDir));
    return Integer.toString(numberOfFilesInMCDir); 
  }
  
  private void initCopyDirsHashMap(){
    copyDirs = new HashMap<String, String>();
    copyDirs.put("masterCopy", CDMSchemaDir.MC_DIR.getDirName());
    copyDirs.put("ALTO", CDMSchemaDir.ALTO_DIR.getDirName());
    copyDirs.put("amdSec", CDMSchemaDir.AMD_DIR.getDirName());
    copyDirs.put("TXT", CDMSchemaDir.TXT_DIR.getDirName());
    copyDirs.put("userCopy", CDMSchemaDir.UC_DIR.getDirName());
  }
  
}
