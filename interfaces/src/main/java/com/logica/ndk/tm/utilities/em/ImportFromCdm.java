package com.logica.ndk.tm.utilities.em;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface ImportFromCdm {

  @WebMethod
  public String importFromCdmSync(@WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "sourceCdmId") String sourceCdmId, @WebParam(name = "fileName") String fileName) throws BusinessException, SystemException;

  @WebMethod
  public void importFromCdmAsync(@WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "sourceCdmId") String sourceCdmId, @WebParam(name = "fileName") String fileName) throws BusinessException, SystemException;

}
