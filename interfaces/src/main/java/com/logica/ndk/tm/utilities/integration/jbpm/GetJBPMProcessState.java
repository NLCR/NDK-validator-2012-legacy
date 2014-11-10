/**
 * 
 */
package com.logica.ndk.tm.utilities.integration.jbpm;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author kovalcikm
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface GetJBPMProcessState {
  @WebMethod
  public String executeSync(
      @WebParam(name = "processName") String processName,
      @WebParam(name = "processId") Integer processId) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(
      @WebParam(name = "processName") String processName,
      @WebParam(name = "processId") Integer processId) throws BusinessException, SystemException;
}