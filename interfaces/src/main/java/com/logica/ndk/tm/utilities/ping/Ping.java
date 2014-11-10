package com.logica.ndk.tm.utilities.ping;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Utility for ping - test if system is up and working.
 * 
 * @author Rudolf Daco
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface Ping {

  /**
   * Returns OK message or throw exception if error parameter is true.
   * 
   * @param error
   *          true if methos shoul throw exception.
   * @return
   * @throws PingException
   */
  @WebMethod
  public String executeSync(@WebParam(name = "error") String error) throws PingException, BusinessException, SystemException;

  @WebMethod
  public void executeAsync(@WebParam(name = "error") String error) throws PingException, BusinessException, SystemException;
}
