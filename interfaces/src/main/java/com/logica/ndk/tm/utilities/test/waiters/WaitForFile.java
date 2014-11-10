package com.logica.ndk.tm.utilities.test.waiters;

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
public interface WaitForFile {

  @WebMethod
  public String executeSync(@WebParam(name = "path") String path,@WebParam(name = "processIntanceId") Long processIntanceId) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(@WebParam(name = "path") String path, @WebParam(name = "processIntanceId") Long processIntanceId) throws BusinessException, SystemException;
}
