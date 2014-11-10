/**
 * 
 */
package com.logica.ndk.tm.utilities.ocr;

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
public interface NoOcr {
  @WebMethod
  public String executeSync(
      @WebParam(name = "cdmId") String cdmId) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(
      @WebParam(name = "cdmId") String cdmId) throws BusinessException, SystemException;

}
