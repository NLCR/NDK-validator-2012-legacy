package com.logica.ndk.tm.utilities.file;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author salaid
 *
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface AnalyzeImport {
  
  /**
   * Utility for analyzing the contents of import package.
   * 
   * @param cdmId
   *          id of CDM package
   * @return OK
   */
  @WebMethod
  public String executeSync(@WebParam(name = "cdmId") String cdmId) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(@WebParam(name = "cdmId") String cdmId) throws BusinessException, SystemException;

}
