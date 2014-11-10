package com.logica.ndk.tm.utilities.transformation.jpeg2000;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.TransformationException;

/**
 * Utility to convert input file into JPEG 2000 file.
 * 
 * @author rudi
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface ConvertToJpeg2000LTP {
  /**
   * @param cdmId
   *          - can be null
   * @param source
   *          - source target to get source files
   * @param target
   *          - target folder
   * @param profile
   *          - profile to set parameters for Kakadu conversion
   * @param sourceExt
   *          - can be null. commna sepparated list of extensions
   * @return
   * @throws TransformationException
   */
  @WebMethod
  public Integer executeSync(@WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "source") String source,
      @WebParam(name = "target") String target,
      @WebParam(name = "profile") String profile) throws TransformationException, BusinessException, SystemException;

  /**
   * @param cdmId
   *          - can be null
   * @param source
   *          - source target to get source files
   * @param target
   *          - target folder
   * @param profile
   *          - profile to set parameters for Kakadu conversion
   * @param sourceExt
   *          - can be null. commna sepparated list of extensions
   * @return
   * @throws TransformationException
   */
  @WebMethod
  public void executeAsync(@WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "source") String source,
      @WebParam(name = "target") String target,
      @WebParam(name = "profile") String profile) throws TransformationException, BusinessException, SystemException;
}
