package com.logica.ndk.tm.utilities.uuid;

import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Utility to generate uuid.
 * 
 * @author rudi
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface GenerateUuid {
  /**
   * @return uuid
   * @throws
   */
  @WebMethod
  @WebResult(name = "uuid")
  public String executeSync() throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync() throws BusinessException, SystemException;
}
