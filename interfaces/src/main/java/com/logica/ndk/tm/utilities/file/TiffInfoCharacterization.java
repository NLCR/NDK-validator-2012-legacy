/**
 * 
 */
package com.logica.ndk.tm.utilities.file;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author kovalcikm
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface TiffInfoCharacterization {
  @WebMethod
  public String executeSync(
      @WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "dir") String sourcePath) throws FileCharacterizationException, BusinessException, SystemException;

  @WebMethod
  public void executeAsync(
      @WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "dir") String dir) throws FileCharacterizationException, BusinessException, SystemException;

}
