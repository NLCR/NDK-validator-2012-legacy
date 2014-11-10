package com.logica.ndk.tm.utilities.transformation.em;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author ondrusekl
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CreateEmConfig {

  @WebMethod
  public Integer createSync(@WebParam(name = "cdmId") String cdmId, @WebParam(name = "ocr") String ocr) throws BusinessException, SystemException;

  @WebMethod
  public void createAsync(@WebParam(name = "cdmId") String cdmId, @WebParam(name = "ocr") String ocr) throws BusinessException, SystemException;
}
