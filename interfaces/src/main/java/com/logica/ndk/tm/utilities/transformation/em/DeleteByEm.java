package com.logica.ndk.tm.utilities.transformation.em;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Delete fils by em csv file (if record contains EmPageType.forDeletion). Also remove references to this deleted files.
 * 
 * @author Rudolf Daco
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface DeleteByEm {

  @WebMethod
  public Integer executeSync(@WebParam(name = "cdmId") final String cdmId) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(@WebParam(name = "cdmId") final String cdmId) throws BusinessException, SystemException;
}