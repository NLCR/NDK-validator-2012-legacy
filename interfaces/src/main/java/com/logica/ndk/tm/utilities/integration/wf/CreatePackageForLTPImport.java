/**
 * 
 */
package com.logica.ndk.tm.utilities.integration.wf;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author brizat
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CreatePackageForLTPImport {

  @WebMethod
  public String executeSync(
      @WebParam(name = "url") String url,
      @WebParam(name = "taskId") Long taskId,
      @WebParam(name = "cdmId") String cdmId) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(
      @WebParam(name = "url") String uuid,
      @WebParam(name = "taskId") Long taskId,
      @WebParam(name = "cdmId") String cdmId) throws BusinessException, SystemException;
}
