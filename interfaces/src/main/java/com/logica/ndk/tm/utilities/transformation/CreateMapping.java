package com.logica.ndk.tm.utilities.transformation;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Create mappgin between postprocessingData and flatData files (pages split)
 * @author majdaf
 *
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CreateMapping {
  /**
   * Create mapping for CDM
   * @param cdmId CDM ID
   * @return Status
   */
  @WebMethod
  public String executeSync(@WebParam(name = "cdmId") String cdmId) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(@WebParam(name = "cdmId") String cdmId) throws BusinessException, SystemException;
}
