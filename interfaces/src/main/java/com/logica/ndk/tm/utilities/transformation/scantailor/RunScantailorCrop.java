package com.logica.ndk.tm.utilities.transformation.scantailor;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface RunScantailorCrop {

  @WebMethod
  public Integer executeSync(@WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "profile") String profile,
      @WebParam(name = "colorMode") String colorMode,
      @WebParam(name = "cropType") String cropType,
      @WebParam(name = "dimensionX") Integer dimensionX,
      @WebParam(name = "dimensionY") Integer dimensionY,
      @WebParam(name = "outputDpi") Integer outputDpi
      ) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(@WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "profile") String profile,
      @WebParam(name = "colorMode") String colorMode,
      @WebParam(name = "cropType") String cropType,
      @WebParam(name = "dimensionX") Integer dimensionX,
      @WebParam(name = "dimensionY") Integer dimensionY,
      @WebParam(name = "outputDpi") Integer outputDpi
      ) throws BusinessException, SystemException;
}
