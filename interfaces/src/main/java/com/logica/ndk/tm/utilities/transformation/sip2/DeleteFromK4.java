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
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface DeleteFromK4 {

  @WebMethod
  public String executeSync(
      @WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "locality") String locality,
      @WebParam(name = "deleteEmptyParents") String deleteEmptyParents,
      @WebParam(name = "uuidKrameriusPath") String uuidKrameriusPath) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(
      @WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "locality") String locality,
      @WebParam(name = "deleteEmptyParents") String deleteEmptyParents,
      @WebParam(name = "uuidKrameriusPath") String uuidKrameriusPath) throws BusinessException, SystemException;
}
