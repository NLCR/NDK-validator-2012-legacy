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
 * @author brizat
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CheckJBPMProcessStateElsewhere {
  @WebMethod
  public String executeSync(
      @WebParam(name = "processId") String processId,
      @WebParam(name = "processInstanceId") Integer processInstanceId) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(
      @WebParam(name = "processId") String processId,
      @WebParam(name = "processInstanceId") Integer processInstanceId) throws BusinessException, SystemException;
}
