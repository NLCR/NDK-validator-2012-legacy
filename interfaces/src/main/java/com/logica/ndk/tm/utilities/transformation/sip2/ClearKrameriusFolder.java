package com.logica.ndk.tm.utilities.transformation.sip2;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;


@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface ClearKrameriusFolder {

  @WebMethod
  @WebResult(name = "result")
  public String executeSync(
      @WebParam(name = "cdmId") final String cdmId,
      @WebParam(name = "locality") String locality,
      @WebParam(name = "removePath") String removePath  
  ) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(
      @WebParam(name = "cdmId") final String cdmId,
      @WebParam(name = "locality") String locality,
      @WebParam(name = "removePath") String removePath  
  ) throws BusinessException, SystemException;
}