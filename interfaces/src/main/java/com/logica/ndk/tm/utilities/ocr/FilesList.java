package com.logica.ndk.tm.utilities.ocr;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author ondrusekl
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface FilesList {

  /**
   * Generate list of all files in specified directory. If directory contains any subdirectory, then file path will be
   * relative from provided directory.
   * 
   * @param cdmId
   *          CDM data ID
   * @return "OK" if no error occurred
   */
  @WebMethod
  public String generateSync(@WebParam(name = "cdmId") String cdmId) throws BusinessException, SystemException;

  @WebMethod
  public void generateAsync(@WebParam(name = "cdmId") String cdmId) throws BusinessException, SystemException;
}
