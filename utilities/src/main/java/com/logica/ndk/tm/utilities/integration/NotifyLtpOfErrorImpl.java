package com.logica.ndk.tm.utilities.integration;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.ImportFromLTPHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.sip1.SIP1ImportConsts;

/**
 * @author brizat
 */
public class NotifyLtpOfErrorImpl extends AbstractUtility {

  private static String TARGET_PATH = TmConfig.instance().getString("import.ltp.transferOutDir");

  public void notify(String cdmId) {
    log.info("Notify ltp error started!");

    File importDir = new File(TARGET_PATH);

    String pendingTargetName = SIP1ImportConsts.SIP_STATUS_PENDING + "_" + SIP1ImportConsts.SIP_CDM_PREFIX + cdmId;
    String completeTargetName = SIP1ImportConsts.SIP_STATUS_COMPLETE + "_" + SIP1ImportConsts.SIP_CDM_PREFIX + cdmId;
    
    if (importDir.exists()) {
      File pendingTarget = new File(importDir, pendingTargetName);
      File errorFile = new File(pendingTarget, SIP1ImportConsts.SIP_STATUS_ERROR + "_" + cdmId);
      File taskIdFile = new File(cdm.getCdmDataDir(cdmId), ImportFromLTPHelper.INSTANCE_ID_FILE_NAME);
      if(!taskIdFile.exists()){
        log.error("Task id file not exist! " + taskIdFile.getAbsolutePath());
        throw new SystemException("File with taskId not exist!", ErrorCodes.NOTIFY_LTP_WF_ERROR); 
      }
      if(!pendingTarget.exists()){
        pendingTarget.mkdir();
      }
      if (!errorFile.exists()) {
        try {
          errorFile.createNewFile();
        }
        catch (IOException e) {
          log.error("Error while creating notification file!", e);
          throw new SystemException("Error while creating notification file!", e, ErrorCodes.NOTIFY_LTP_WF_ERROR);
        }
      }
      
      try {
        //FileUtils.copyFile(taskIdFile, new File(pendingTarget + File.separator + ImportFromLTPHelper.INSTANCE_ID_FILE_NAME));
        retriedCopyFile(taskIdFile, new File(pendingTarget + File.separator + ImportFromLTPHelper.INSTANCE_ID_FILE_NAME));
      }
      catch (IOException e) {
        log.error(String.format("Error at copy %s file to %s!", taskIdFile.getAbsolutePath(), pendingTarget.getAbsolutePath()), e);
        throw new SystemException(String.format("Error at copy %s file to %s!", taskIdFile.getAbsolutePath(), pendingTarget.getAbsolutePath()), e, ErrorCodes.NOTIFY_LTP_WF_ERROR);
      }
      
      File completeTarget = new File(importDir, completeTargetName);
      pendingTarget.renameTo(completeTarget);
    }
    else {
      log.error("Target dit does not exist! " + TARGET_PATH);
      throw new SystemException("Error while creating notification file!, target dir does not exist!" + TARGET_PATH, ErrorCodes.NOTIFY_LTP_WF_ERROR);
    }

  }
  
  @RetryOnFailure(attempts = 3)
  private void retriedCopyFile(File source, File destination) throws IOException {
      FileUtils.copyFile(source, destination);
  }

}
