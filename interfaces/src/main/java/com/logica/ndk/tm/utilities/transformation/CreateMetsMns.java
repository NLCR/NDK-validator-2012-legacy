package com.logica.ndk.tm.utilities.transformation;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CreateMetsMns {
  /**
   * Vytvori METS dle obsahu CDM
   * 
   * @param cdmId
   *          id of CDM package
   * @return OK
   */
  @WebMethod
  public String executeSync(@WebParam(name = "cdmId") String cdmId) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(@WebParam(name = "cdmId") String cdmId) throws BusinessException, SystemException;

}
