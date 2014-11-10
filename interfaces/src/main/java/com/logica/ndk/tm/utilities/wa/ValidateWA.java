package com.logica.ndk.tm.utilities.wa;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Validates WA (warc or arc). Reads all records in file to validate whole file.
 * 
 * @author Rudolf Daco
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface ValidateWA {
  @WebMethod
  public Boolean executeSync(@WebParam(name = "sourceDir") String sourceDir) throws WAException, BusinessException, SystemException;

  @WebMethod
  public void executeAsync(@WebParam(name = "sourceDir") String sourceDir) throws WAException, BusinessException, SystemException;
}
