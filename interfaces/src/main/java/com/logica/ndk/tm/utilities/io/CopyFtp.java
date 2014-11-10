/**
 * 
 */
package com.logica.ndk.tm.utilities.io;

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
public interface CopyFtp {
  
  @WebMethod
  public String executeSync(
      @WebParam(name = "url") String url,
      @WebParam(name = "destDir") String destDir,
      @WebParam(name = "login") String login,
      @WebParam(name = "password") String password) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(
      @WebParam(name = "url") String url,
      @WebParam(name = "destDir") String destDir,
      @WebParam(name = "login") String login,
      @WebParam(name = "password") String password) throws BusinessException, SystemException;
}


