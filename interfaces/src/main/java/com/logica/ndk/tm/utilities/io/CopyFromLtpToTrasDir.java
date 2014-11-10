package com.logica.ndk.tm.utilities.io;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author brizat
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CopyFromLtpToTrasDir {

  @WebMethod
  public void executeSync(
      @WebParam(name = "url") String url,
      @WebParam(name = "note") String note,
      @WebParam(name = "instanceId") String instanceId) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(
      @WebParam(name = "url") String url,
      @WebParam(name = "note") String note,
      @WebParam(name = "instanceId") String instanceId) throws BusinessException, SystemException;
  
}
