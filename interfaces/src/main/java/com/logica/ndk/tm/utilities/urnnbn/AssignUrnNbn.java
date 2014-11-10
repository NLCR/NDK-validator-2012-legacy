package com.logica.ndk.tm.utilities.urnnbn;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.process.AssignUrnNbnResponse;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author ondrusekl
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface AssignUrnNbn {

  @WebMethod
  public AssignUrnNbnResponse assignSync(
      @WebParam(name = "sigla") String registrarCode,
      @WebParam(name = "cdmId") String cdmId) throws BusinessException, SystemException;

  @WebMethod
  public void assignAsync(
      @WebParam(name = "sigla") String registrarCode,
      @WebParam(name = "cdmId") String cdmId) throws BusinessException, SystemException;

}
