package com.logica.ndk.tm.utilities.urnnbn;

import javax.jws.WebMethod;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author ondrusekl
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface UrnNbnAgent {

  @WebMethod
  public String executeSync() throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync() throws BusinessException, SystemException;
}
