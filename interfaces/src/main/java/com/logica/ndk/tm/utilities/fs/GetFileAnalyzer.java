/**
 * 
 */
package com.logica.ndk.tm.utilities.fs;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author kovalcikm
 *
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface GetFileAnalyzer {

  @WebMethod
  public String executeSync(
      @WebParam(name = "sourceDir") String processDir,
      @WebParam(name = "targetDir") String sourceFolderName) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(
      @WebParam(name = "sourceDir") String processDir,
      @WebParam(name = "targetDir") String sourceFolderName) throws BusinessException, SystemException;

}
