/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author kovalcikm
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface UpdateFoxmlMetadata {

  @WebMethod
  @WebResult(name = "result")
  public String executeSync(
      @WebParam(name = "cdmId") final String cdmId,
      @WebParam(name = "metadataPartsString") final List<String> metadataParts,
      @WebParam(name = "locality") final String locality,
      @WebParam(name = "policyFilePath") final String policyFilePath,
      @WebParam(name = "processPages") final Boolean processPages) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(
      @WebParam(name = "cdmId") final String cdmId,
      @WebParam(name = "metadataPartsString") final List<String> metadataParts,
      @WebParam(name = "locality") final String locality,
      @WebParam(name = "policyFilePath") final String policyFilePath,
      @WebParam(name = "processPages") final Boolean processPages) throws BusinessException, SystemException;

}
