package com.logica.ndk.tm.utilities.validation;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Validate dir with files whether all files are valid TIFF uncompressed files according to 
 * color mode and with expected resolution (DPI).
 * 
 * @author Petr Palous
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface ValidateTiffinfo {
  @WebMethod
  public ValidationViolationsWrapper executeSync(
      @WebParam(name = "imageDirName") String imageDirName,
      @WebParam(name = "colorMode") String colorMode,
      @WebParam(name = "xRes") String xRes,
      @WebParam(name = "yRes") String yRes,
      @WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "throwException") Boolean throwException) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(
	      @WebParam(name = "imageDirName") String imageDirName,
	      @WebParam(name = "colorMode") String colorMode,
	      @WebParam(name = "xRes") String xRes,
	      @WebParam(name = "yRes") String yRes,
	      @WebParam(name = "cdmId") String cdmId,
	      @WebParam(name = "throwException") Boolean throwException) throws BusinessException, SystemException;
}
