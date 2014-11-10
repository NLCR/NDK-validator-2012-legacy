package com.logica.ndk.tm.utilities.transformation.sip2;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author brizat
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface HttpClient {

  @WebMethod
  public void doPostSync(
      @WebParam(name = "url") String url,
      @WebParam(name = "params") String params,
      @WebParam(name = "userName") String userName,
      @WebParam(name = "password") String password) throws BusinessException, SystemException;

  /**
   * Async execution
   * 
   * @param cdmId
   *          CDM ID
   */
  @WebMethod
  public void doPostAsync(
      @WebParam(name = "url") String url,
      @WebParam(name = "params") String params,
      @WebParam(name = "userName") String userName,
      @WebParam(name = "password") String password) throws BusinessException, SystemException; 
  

}