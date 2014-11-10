package com.logica.ndk.tm.utilities.io;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author brizat
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CopyToLTPImput {

  @WebMethod
  public void executeAsync(@WebParam(name = "uuid") String uuid) throws BusinessException, SystemException;

  @WebMethod
  public void executeSync(@WebParam(name = "uuid") String uuid) throws BusinessException, SystemException;
}
