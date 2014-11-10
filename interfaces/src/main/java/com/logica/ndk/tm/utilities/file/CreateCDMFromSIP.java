package com.logica.ndk.tm.utilities.file;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;



/**
 * @author brizat
 *
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CreateCDMFromSIP {
  
  @WebMethod
  @WebResult(name = "cdmId")
  public String executeSync(
      @WebParam(name = "path") String id,
      @WebParam(name = "processType") String processType) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(
      @WebParam(name = "path") String id,
      @WebParam(name = "processType") String processType) throws BusinessException, SystemException;

}
