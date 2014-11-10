package com.logica.ndk.tm.utilities.transformation.sip1;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.sip1.exception.SIP1ImportFailedException;

/**
 * Check SIP1 import result
 * @author majdaf
 *
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CheckSIP1ImportResult {
  
  /**
   * Sync execution
   * @param cdmId CDM ID
   */
  @WebMethod
  @WebResult(name = "result")
  public String executeSync (
      @WebParam(name = "cdmId") String cdmId) throws SIP1ImportFailedException, BusinessException, SystemException;

  /**
   * Async execution
   * @param cdmId CDM ID
   */
  @WebMethod
  public void executeAsync(
      @WebParam(name = "cdmId") String cdmId) throws SIP1ImportFailedException, BusinessException, SystemException;

}
