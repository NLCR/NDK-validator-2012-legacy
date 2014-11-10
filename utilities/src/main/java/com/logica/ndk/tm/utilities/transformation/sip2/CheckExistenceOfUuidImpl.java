package com.logica.ndk.tm.utilities.transformation.sip2;

import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;

public class CheckExistenceOfUuidImpl extends AbstractUtility{

  public String execute(String uuid, String locality, Boolean throwExceptionOnExist){
    log.info("Execution started, uuid " + uuid);
    
    KrameriusHelper krameriusHelper = new KrameriusHelper(locality);
    boolean pidExistence = krameriusHelper.checkPidExistence(uuid);
    if(pidExistence){
      if(throwExceptionOnExist){
        throw new BusinessException(String.format("Document with given uuid %s exist in k4", uuid), ErrorCodes.DOCUMENT_EXIST_IN_K4);
      }
    }else{
      if(!throwExceptionOnExist){
        throw new BusinessException(String.format("Document with given uuid %s not exist in k4", uuid), ErrorCodes.DOCUMENT_NOT_EXIST_IN_K4);
      }
    }
    
    
    return ResponseStatus.RESPONSE_OK;
  }
  
}
