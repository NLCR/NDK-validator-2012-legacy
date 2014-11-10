/**
 * 
 */
package com.logica.ndk.tm.utilities.urnnbn;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author kovalcikm
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface DeactivateUrnNbn {

  @WebMethod
  public String executeSync(
      @WebParam(name = "urnnbn") String urnnbn,
      @WebParam(name = "note") String note) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(
      @WebParam(name = "urnnbn") String urnnbn,
      @WebParam(name = "note") String note) throws BusinessException, SystemException;

}
