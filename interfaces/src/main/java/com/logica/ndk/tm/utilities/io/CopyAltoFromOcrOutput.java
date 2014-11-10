package com.logica.ndk.tm.utilities.io;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CopyAltoFromOcrOutput {

  @WebMethod
  public String executeSync(
      @WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "ocr") String ocr,
      @WebParam(name = "ocrFont") String ocrFont,
      @WebParam(name = "language") String language,
      @WebParam(name = "target") String targetPath,
      @WebParam(name = "wildcard") String... wildcards) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(
      @WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "ocr") String ocr,
      @WebParam(name = "ocrFont") String ocrFont,
      @WebParam(name = "language") String language,
      @WebParam(name = "target") String targetPath,
      @WebParam(name = "wildcard") String... wildcards) throws BusinessException, SystemException;

}
