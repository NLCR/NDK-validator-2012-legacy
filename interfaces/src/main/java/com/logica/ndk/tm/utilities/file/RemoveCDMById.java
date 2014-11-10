package com.logica.ndk.tm.utilities.file;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;


/**
 * @author brizat
 *
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface RemoveCDMById {
  
  @WebMethod
  public String executeSync(
      @WebParam(name = "id") String id) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(
      @WebParam(name = "id") String id) throws BusinessException, SystemException;
}
