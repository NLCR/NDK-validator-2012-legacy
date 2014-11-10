package com.logica.ndk.tm.utilities.ocr;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.process.CheckOcrOutputResponse;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author ondrusekl
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CheckOcrOutput {

  /**
   * Wait until all files from filesList param are not finished in target.
   * 
   * @param cdmId
   *          CDM data ID
   * @return "OK" if no error occurred
   */
  @WebMethod
  public CheckOcrOutputResponse checkSync(@WebParam(name = "cdmId") String cdmId) throws BusinessException, SystemException;

  @WebMethod
  public void checkAsync(
      @WebParam(name = "cdmId") String cdmId) throws BusinessException, SystemException;
}
