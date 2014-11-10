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
 *
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CopyOcrResult{
  
  /**
 * Copies ocr output and exception result to the cdm package
 * 
 * @param cdmId
 *          CDM data ID
 * @return "OK" if no error occurred
 */
@WebMethod
public String executeSync(@WebParam(name = "cdmId") String cdmId,
    @WebParam(name = "ocr") String ocr,
    @WebParam(name = "ocrFont") String ocrFont,
    @WebParam(name = "language") String language) throws BusinessException, SystemException;

@WebMethod
public void executeAsync(
    @WebParam(name = "cdmId") String cdmId,
    @WebParam(name = "ocr") String ocr,
    @WebParam(name = "ocrFont") String ocrFont,
    @WebParam(name = "language") String language) throws BusinessException, SystemException;
}