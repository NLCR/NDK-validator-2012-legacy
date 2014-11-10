package com.logica.ndk.tm.utilities.ocr;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Copy images to OCR transfer (input) directory according list. The list of images is read
 * from file created in FilesListImpl.
 * 
 * @author Petr Palous
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CopyToOcrInput {

  @WebMethod
  public String copySync(
      @WebParam(name = "cdmId") String cdmId
      ) throws BusinessException, SystemException;

  @WebMethod
  public void copyAsync(
      @WebParam(name = "cdmId") String cdmId
      ) throws BusinessException, SystemException;
}
