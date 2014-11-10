/**
 * 
 */
package com.logica.ndk.tm.utilities.premis;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author kovalcikm
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface GenerateEvent {

  @WebMethod
  public String executeSync(
      @WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "sourceDirPath") String sourceDirPath,
      @WebParam(name = "operation") String operation,
      @WebParam(name = "agentName") String agentName,
      @WebParam(name = "agentVersion") String agentVersion,
      @WebParam(name = "agentRole") String agentRole,
      @WebParam(name = "formatDesignationName") String formatDesignationName,
      @WebParam(name = "formatRegKey") String formatRegKey,
      @WebParam(name = "preservationLevel") String preservationLevel,
      @WebParam(name = "extension") String extension) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(
      @WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "sourceDirPath") String sourceDirPath,
      @WebParam(name = "operation") String operation,
      @WebParam(name = "agentName") String agentName,
      @WebParam(name = "agentVersion") String agentVersion,
      @WebParam(name = "agentRole") String agentRole,
      @WebParam(name = "formatDesignationName") String formatDesignationName,
      @WebParam(name = "formatRegKey") String formatRegKey,
      @WebParam(name = "preservationLevel") String preservationLevel,
      @WebParam(name = "extension") String extension) throws BusinessException, SystemException;

}
