package com.logica.ndk.tm.utilities.io;

import javax.jws.WebMethod;
import javax.jws.WebParam;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Validates if file has encoding UTF-8.
 * 
 * @author Rudolf Daco
 */
public interface ValidateEncodingUTF8 {
  @WebMethod
  public Boolean executeSync(@WebParam(name = "file") String file) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(@WebParam(name = "file") String file) throws BusinessException, SystemException;
}
