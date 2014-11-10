package com.logica.ndk.tm.utilities.io;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author ondrusekl
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface GrantAccess {

  @WebMethod
  public String grantSync(
      @WebParam(name = "user") String user,
      @WebParam(name = "path") String cdmId) throws BusinessException, SystemException;

  @WebMethod
  public void grantAsync(
      @WebParam(name = "user") String user,
      @WebParam(name = "path") String cdmId) throws BusinessException, SystemException;
}
