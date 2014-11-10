package com.logica.ndk.tm.utilities.file;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.validation.ValidationViolationsWrapper;

/**
 * Utility for validation of PSP scan.
 * 
 * @author Rudolf Daco
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface ValidatePSPScan {
  /**
   * validate files of PSP Scan at location specified by cdmId.
   * 
   * @param cdmId
   *          id of location of source dir which will be consume dby CDM
   * @param parameters
   *          to pass specification for this operation
   * @return
   * @throws ValidateException
   */
  @WebMethod
  public ValidationViolationsWrapper executeSync(
      @WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "throwException") Boolean throwException) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(
      @WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "throwException") Boolean throwException) throws BusinessException, SystemException;
}
