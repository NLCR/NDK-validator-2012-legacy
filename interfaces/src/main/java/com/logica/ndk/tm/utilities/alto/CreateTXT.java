package com.logica.ndk.tm.utilities.alto;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.alto.exception.InvalidSourceFolderException;

/**
 * Create single TXT file based on ALTO
 * 
 * @author majdaf
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CreateTXT {
  /**
   * Execute sync
   * 
   * @param cdmId
   * @throws InvalidSourceFolderException
   */
  @WebMethod
  public void executeSync(
      @WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "abstractionDir") String abstractionDir)
      throws InvalidSourceFolderException, BusinessException, SystemException;

  /**
   * Execute async
   * 
   * @param cdmId
   * @throws InvalidSourceFolderException
   */
  @WebMethod
  public void executeAsync(
      @WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "abstractionDir") String abstractionDir)
      throws InvalidSourceFolderException, BusinessException, SystemException;
}
