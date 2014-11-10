package com.logica.ndk.tm.utilities.integration.aleph;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Utility to save aleph metadata to CDM.
 * 
 * @author rudi
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface SaveAlephMetadata {
  /**
   * @param cdmId
   * @param alephMetadata
   * @return
   */
  @WebMethod
  public String executeSync(@WebParam(name = "cdmId") String cdmId, @WebParam(name = "alephMetadata") String alephMetadata) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(@WebParam(name = "cdmId") String cdmId, @WebParam(name = "alephMetadata") String alephMetadata) throws BusinessException, SystemException;
}
