/**
 * 
 */
package com.logica.ndk.tm.utilities.fs;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author kovalcikm
 *
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface PrepareProcessData {
  
  @WebMethod
  public String executeSync(
      @WebParam(name = "dataPath") String dataPath) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(
      @WebParam(name = "dataPath") String dataPath) throws BusinessException, SystemException;

}
