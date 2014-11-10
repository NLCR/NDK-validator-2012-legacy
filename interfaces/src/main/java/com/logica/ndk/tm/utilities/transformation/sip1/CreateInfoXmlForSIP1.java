package com.logica.ndk.tm.utilities.transformation.sip1;

import java.io.IOException;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author brizat
 *
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CreateInfoXmlForSIP1 {
  
  /**
   * Sync execution
   * 
   * @param cdmId
   *          CDM ID
   */
  @WebMethod
  public String executeSync(
      @WebParam(name = "cdmId") String cdmId) throws BusinessException, SystemException;

  /**
   * Async execution
   * 
   * @param cdmId
   *          CDM ID
   */
  @WebMethod
  public void executeAsync(
      @WebParam(name = "cdmId") String cdmId) throws BusinessException, SystemException;
  
}
