/**
 * 
 */
package com.logica.ndk.tm.utilities.validation;

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
public interface ValidateMD5 {

  @WebMethod
  public ValidationViolationsWrapper executeSync(
      @WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "throwException") Boolean throwException) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(
      @WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "throwException") Boolean throwException) throws BusinessException, SystemException;
}