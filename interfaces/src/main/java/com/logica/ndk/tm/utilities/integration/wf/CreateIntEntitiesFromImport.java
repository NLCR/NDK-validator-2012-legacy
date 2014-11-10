/**
 * 
 */
package com.logica.ndk.tm.utilities.integration.wf;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author kovalcikm
 *
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CreateIntEntitiesFromImport {
  

  @WebMethod
  @WebResult(name = "taskIds")
  public List<String> executeSync (
  		@WebParam(name = "taskId") Long taskId,
  		@WebParam(name = "url") String url) 
  				throws BusinessException, SystemException;


  @WebMethod
  public void executeAsync(
  		@WebParam(name = "taskId") Long taskId,
  		@WebParam(name = "url") String url)
  				throws BusinessException, SystemException;

}
