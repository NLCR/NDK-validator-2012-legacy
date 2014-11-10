package com.logica.ndk.tm.utilities.transformation.mets;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.mets.exception.ElementNotFoundException;
import com.logica.ndk.tm.utilities.transformation.mets.exception.METSPasrsingFailedException;

/**
 * Extract uuid from given METS file
 * @author majdaf
 *
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface GetUUIDFromMets {
  
  /**
   * Execute sync
   * @param metsFilePath Path to the METS file
   * @return uuid
   */
  @WebMethod
  public String executeSync(@WebParam(name = "metsFilePath") String metsFilePath) throws METSPasrsingFailedException, ElementNotFoundException, BusinessException, SystemException;

  /**
   * Execute sync
   * @param metsFilePath Path to the METS file
   */
  @WebMethod
  public void executeAsync(@WebParam(name = "metsFilePath") String metsFilePath) throws METSPasrsingFailedException, ElementNotFoundException, BusinessException, SystemException;

}
