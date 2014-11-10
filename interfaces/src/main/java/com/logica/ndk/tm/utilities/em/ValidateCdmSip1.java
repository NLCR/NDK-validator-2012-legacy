package com.logica.ndk.tm.utilities.em;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.validation.ValidationViolationsWrapper;

@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface ValidateCdmSip1 {

  @WebMethod
  public ValidationViolationsWrapper validateSync(
      @WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "throwException") Boolean throwException) throws BusinessException, SystemException;

  @WebMethod
  public void validateAsync(
      @WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "throwException") Boolean throwException) throws BusinessException, SystemException;
}
