package com.logica.ndk.tm.utilities.shutdown;

import javax.jws.WebMethod;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Utility for testing TM state
 * 
 * @author brizat
 *
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface ShutDownService {

  /**
   * Return true if TM is shutting down
   * 
   * @return
   */
  @WebMethod
  public Boolean isShuttingDownSync() throws BusinessException, SystemException;

  
  @WebMethod
  public void isShuttingDownAsync() throws BusinessException, SystemException;
}
