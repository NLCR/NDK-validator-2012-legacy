package com.logica.ndk.tm.utilities.em;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface ValidateCdmSip2 {

  @WebMethod
  public List<ValidationViolation> validateSync(@WebParam(name = "cdmId") String cdmId) throws BusinessException, SystemException;

  @WebMethod
  public void validateAsync(@WebParam(name = "cdmId") String cdmId) throws BusinessException, SystemException;

}
