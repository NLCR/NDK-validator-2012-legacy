package com.logica.ndk.tm.utilities.transformation.em;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * For all scans set ocr profile to value from WF
 * 
 * @author Kovalcikm
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface ExtendEmCsv {
  @WebMethod
  public Integer executeSync(
      @WebParam(name = "cdmId") final String cdmId,
      @WebParam(name = "ocr") final String ocr,
      @WebParam(name = "taskId") final String taskId) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(
      @WebParam(name = "cdmId") final String cdmId,
      @WebParam(name = "ocr") final String ocr,
      @WebParam(name = "taskId") final String taskId) throws BusinessException, SystemException;
}
