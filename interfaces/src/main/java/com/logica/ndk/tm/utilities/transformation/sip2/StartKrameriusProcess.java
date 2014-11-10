package com.logica.ndk.tm.utilities.transformation.sip2;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author korvasm
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface StartKrameriusProcess {

  /**
   * Sync execution
   * 
   * @param profile
   *          profile to be executed
   * @uuid
   */
  @WebMethod
  public String executeSync(
      @WebParam(name = "uuid") String uuid,
      @WebParam(name = "locality") String locality,
      @WebParam(name = "cdmId") String cdmId) throws SystemException, BusinessException;

  /**
   * Async execution
   * 
   * @param profile
   *          profile to be executed
   * @uuid
   */
  @WebMethod
  public void executeAsync(
      @WebParam(name = "uuid") String uuid,
      @WebParam(name = "locality") String locality,
      @WebParam(name = "cdmId") String cdmId) throws SystemException, BusinessException;

}
