package com.logica.ndk.tm.utilities.io;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.ImportFromLTPHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author brizat
 *
 */
public class CopyFromLtpToTrasDirImpl extends AbstractUtility {

  private static String COMPLETE_PREFIX = "complete_";
  private static String PROCESING_PREFIX = "procesing_";
  private static String transferDirPath = TmConfig.instance().getString("import.ltp.transferInDir");

  public void execute(String url, String note, String instanceId) throws Exception {
    log.info("Utility CopyFromLtpToTrasDirImpl started");
    File sourceDir = new File(url);

    if (!sourceDir.exists() || !sourceDir.isDirectory()) {
      log.error(String.format("Source dir \"%s\" does not exist!", sourceDir.getAbsolutePath()));
      throw new SystemException(String.format("Source dir does not exist!", sourceDir.getAbsolutePath()));
    }

    File noteFile = new File(sourceDir, ImportFromLTPHelper.NOTE_FILE_NAME);
    File taskIdFile = new File(sourceDir, ImportFromLTPHelper.INSTANCE_ID_FILE_NAME);
    try {
      //FileUtils.writeStringToFile(noteFile, note);
      retriedWriteStringToFile(noteFile, note);
      //FileUtils.writeStringToFile(taskIdFile, instanceId);
      retriedWriteStringToFile(taskIdFile, instanceId);
    }
    catch (IOException e) {
      log.error("Could not write note to file: " , e);
    }
    //String targetDirPath = transferDirPath + File.separator + PROCESING_PREFIX + sourceDir.getName().substring(COMPLETE_PREFIX.length());
    File targetDir = new File(transferDirPath + File.separator + PROCESING_PREFIX + sourceDir.getName().substring(COMPLETE_PREFIX.length()));
    new CopyToImpl().copy(sourceDir.getAbsolutePath(), targetDir.getAbsolutePath(), null);
    
    File completeDir = new File(transferDirPath + File.separator + COMPLETE_PREFIX + targetDir.getName().substring(PROCESING_PREFIX.length()));
    
    boolean result = targetDir.renameTo(completeDir);
    if (!result) {
      log.error("Unable to rename " + targetDir + " to " + completeDir);
      throw new Exception("Unable to rename " + targetDir + " to " + completeDir);
    }
    log.debug("Renamed to: " + completeDir);
    log.info("Utility CopyFromLtpToTrasDirImpl finished");
  }
  
  @RetryOnFailure(attempts = 3)
  private void retriedWriteStringToFile(File file, String string, Boolean... params) throws IOException {
    if(params.length > 0) {
      FileUtils.writeStringToFile(file, string, "UTF-8", params[0].booleanValue());
        
    } else {
      FileUtils.writeStringToFile(file, string, "UTF-8");
      
    }
  }
  
}
