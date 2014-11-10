package com.logica.ndk.tm.utilities.io;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.transformation.sip1.SIP1ImportConsts;

public class CopyToLTPImputImpl extends AbstractUtility {

  public void execute(String uuid) {
    String importDir = SIP1ImportConsts.SIP_IMPORT_DIR;
    String sourceDirPath = TmConfig.instance().getString("import.ltp.transferOutDir"); 
    File sourceDir = new File(sourceDirPath + File.separator + SIP1ImportConsts.SIP_STATUS_PENDING + "_" + SIP1ImportConsts.SIP_CDM_PREFIX + uuid);
    
    if(!sourceDir.exists()){
      log.error("Source dir not exist. " + sourceDir.getAbsolutePath());
      throw new BusinessException("Source dir not exist. " + sourceDir.getAbsolutePath(), ErrorCodes.IMPORT_LTP_SOURCE_DIR_NOT_EXIST);
    }
    
    String pendingTargetName = importDir + SIP1ImportConsts.SIP_STATUS_PENDING + "_" + SIP1ImportConsts.SIP_CDM_PREFIX + uuid;
    String completeTargetName = importDir + SIP1ImportConsts.SIP_STATUS_COMPLETE + "_" + SIP1ImportConsts.SIP_CDM_PREFIX + uuid;
    
    //Copy
    new CopyToImpl().copy(sourceDir.getAbsolutePath(), pendingTargetName, null);
    
    //Rename to complete
    File pending = new File(pendingTargetName);
    pending.renameTo(new File(completeTargetName));
    
    //Delete source
    try {
      //FileUtils.deleteDirectory(sourceDir);
      retriedDeleteDirectory(sourceDir);
    }
    catch (IOException e) {
      log.error("Error while removing source directory! " + sourceDir.getAbsolutePath());
    }
  }
  
  @RetryOnFailure(attempts = 3)
  private void retriedDeleteDirectory(File target) throws IOException {
      FileUtils.deleteDirectory(target);
  }

}
