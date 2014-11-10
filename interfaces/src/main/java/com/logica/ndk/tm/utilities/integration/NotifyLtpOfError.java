package com.logica.ndk.tm.utilities.integration;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author ondrusekl
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface NotifyLtpOfError {

  @WebMethod
  public String notifySync(@WebParam(name = "cdmId") String cdmId) throws BusinessException, SystemException;

  @WebMethod
  public void notifyAsync(@WebParam(name = "cdmId") String cdmId) throws BusinessException, SystemException;
}
