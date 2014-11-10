package com.logica.ndk.tm.utilities.file;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.process.ParamMap;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Utility for file characterization.
 * 
 * @author Rudolf Daco
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface SmallFileCharacterization {
  /**
   * Consumes all files from sourcePath and produces characterization for these files into targetPath.
   * 
   * @param sourcePath
   * @param targetPath
   * @param parameters
   *          to pass specification for this operation
   * @return
   * @throws
   */
  @WebMethod
  public String executeSync(
      @WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "sourcePath") String sourcePath,
      @WebParam(name = "targetPath") String targetPath,
      @WebParam(name = "parameters") ParamMap parameters) throws FileCharacterizationException, BusinessException, SystemException;

  @WebMethod
  public void executeAsync(
      @WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "sourcePath") String sourcePath,
      @WebParam(name = "targetPath") String targetPath,
      @WebParam(name = "parameters") ParamMap parameters) throws FileCharacterizationException, BusinessException, SystemException;
}
