/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

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
public interface TransformToRawData {
  /**
   * @param sourceDirPath
   *          - source target to get source files
   * @param targetDirPath
   *          - target folder
   * @param outputDpi
   *          - outputDpi which will be used for all output images
   */
  @WebMethod
  public String executeSync(
      @WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "sourceDirPath") String sourceDirPath,
      @WebParam(name = "targetDirPath") String targetDirPath) throws SystemException;

  /**
   * @param sourceDirPath
   *          - source target to get source files
   * @param targetDirPath
   *          - target folder
   * @param outputDpi
   *          - outputDpi which will be used for all output images
   */
  @WebMethod
  public void executeAsync(
      @WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "sourceDirPath") String sourceDirPath,
      @WebParam(name = "targetDirPath") String targetDirPath) throws SystemException;
}
