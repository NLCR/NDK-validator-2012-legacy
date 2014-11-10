/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.sip2;

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
public interface StartKrameriusIndexProcess {
  
  @WebMethod
  public String executeSync(
      @WebParam(name = "locality") String locality,
      @WebParam(name = "cdmId") String cdmId) throws SystemException, BusinessException;


  @WebMethod
  public void executeAsync(
      @WebParam(name = "locality") String locality,
      @WebParam(name = "cdmId") String cdmId) throws SystemException, BusinessException;
}
