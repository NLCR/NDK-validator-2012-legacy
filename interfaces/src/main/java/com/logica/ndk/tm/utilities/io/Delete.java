package com.logica.ndk.tm.utilities.io;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author ondrusekl
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface Delete {

  @WebMethod
  public String deleteSync(@WebParam(name = "path") String path, @WebParam(name = "notThrowNotFoudEx") Boolean notThrowNotFoudEx) throws BusinessException, SystemException;

  @WebMethod
  public void deleteAsync(@WebParam(name = "path") String path, @WebParam(name = "notThrowNotFoudEx") Boolean notThrowNotFoudEx) throws BusinessException, SystemException;
}
