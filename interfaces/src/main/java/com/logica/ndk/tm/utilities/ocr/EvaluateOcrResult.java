/**
 * 
 */
package com.logica.ndk.tm.utilities.ocr;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.process.CheckOcrOutputResponse;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author kovalcikm
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface EvaluateOcrResult {
  @WebMethod
  public CheckOcrOutputResponse executeSync(@WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "ocrPagesExeption") Integer ocrPagesExeption) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(@WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "ocrPagesExeption") Integer ocrPagesExeption) throws BusinessException, SystemException;
}
