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
public interface VerifyEntityK4 {

  @WebMethod
  public String verifySync(@WebParam(name = "cdmId") String cdmId) throws BusinessException, SystemException;

  @WebMethod
  public void verifyAsync(@WebParam(name = "cdmId") String cdmId) throws BusinessException, SystemException;
}
