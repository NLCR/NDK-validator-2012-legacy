package com.logica.ndk.tm.utilities.transformation.mets;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.mets.exception.ElementNotFoundException;
import com.logica.ndk.tm.utilities.transformation.mets.exception.METSPasrsingFailedException;

/**
 * Extract uuid from given METS file (mods)
 * @author brizat
 *
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface GetUuidFromMetsFile {
  
  /**
   * Execute sync
   * @param cdmId
   * @return uuid
   */
  @WebMethod
  public String executeSync(@WebParam(name = "cdmId") String cdmId) throws BusinessException, SystemException;

  /**
   * Execute sync
   * @param cdmId
   */
  @WebMethod
  public void executeAsync(@WebParam(name = "cdmId") String cdmId) throws BusinessException, SystemException;

}
