package com.logica.ndk.tm.utilities.transformation;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface MergeCDM {
  @WebMethod
  public String executeSync(@WebParam(name = "cdmIdMaster") String cdmIdMaster, @WebParam(name = "cdmIdSlave") String cdmIdSlave) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(@WebParam(name = "cdmIdMaster") String cdmIdMaster, @WebParam(name = "cdmIdSlave") String cdmIdSlave) throws BusinessException, SystemException;
}
