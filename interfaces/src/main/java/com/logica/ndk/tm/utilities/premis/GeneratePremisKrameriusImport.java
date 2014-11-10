package com.logica.ndk.tm.utilities.premis;

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
public interface GeneratePremisKrameriusImport {
  
  @WebMethod
  public String executeSync(@WebParam(name = "cdmId") String cdmId) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(@WebParam(name = "cdmId") String cdmId) throws BusinessException, SystemException;

}
