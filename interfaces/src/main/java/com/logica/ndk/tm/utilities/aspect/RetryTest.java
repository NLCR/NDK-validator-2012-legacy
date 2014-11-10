package com.logica.ndk.tm.utilities.aspect;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.alto.exception.InconsistentDataException;
import com.logica.ndk.tm.utilities.alto.exception.InvalidSourceFolderException;

/**
 * 
 * 
 * @author majdaf
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface RetryTest {
  @WebMethod
  public void executeSync();

  @WebMethod
  public void executeAsync();
}
