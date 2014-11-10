package com.logica.ndk.tm.utilities.alto;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.alto.exception.InconsistentDataException;
import com.logica.ndk.tm.utilities.alto.exception.InvalidSourceFolderException;

/**
 * Create 2 layer PDF based on images and XML/ALTO data
 * 
 * @author majdaf
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CreatePDF {
  @WebMethod
  public void executeSync(
      @WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "isPublic") Boolean isPublic,
      @WebParam(name = "abstractionDir") String abstractionDir)
      throws InvalidSourceFolderException, InconsistentDataException, BusinessException, SystemException;

  @WebMethod
  public void executeAsync(
      @WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "isPublic") Boolean isPublic,
      @WebParam(name = "abstractionDir") String abstractionDir)
      throws InvalidSourceFolderException, InconsistentDataException, BusinessException, SystemException;
}
