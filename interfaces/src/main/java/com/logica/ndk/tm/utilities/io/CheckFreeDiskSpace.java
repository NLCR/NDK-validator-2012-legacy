package com.logica.ndk.tm.utilities.io;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Check free disk space for process running. If given required minimal space or size of 
 * given directory is bigger than actual free disk space exception is raised.
 *  	 
 * @author Petr Palous
 *
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CheckFreeDiskSpace {

  @WebMethod
  public String executeSync(
  		@WebParam(name = "cdmId") String cdmId,
  		@WebParam(name = "requiredMinFreeSpaceMB") String requiredMinFreeSpaceMB,
      @WebParam(name = "cdmReferenceDir") String cdmReferenceDir,
      @WebParam(name = "growCoef") String growCoef) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(
  		@WebParam(name = "cdmId") String cdmId,
  		@WebParam(name = "requiredMinFreeSpaceMB") String requiredMinFreeSpaceMB,
      @WebParam(name = "cdmReferenceDir") String cdmReferenceDir,
      @WebParam(name = "growCoef") String growCoef) throws BusinessException, SystemException;

}
