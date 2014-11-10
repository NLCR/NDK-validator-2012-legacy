package com.logica.ndk.tm.utilities.transformation.sip2;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author brizat
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CreateLogForLTPImport {

  /**
   * Sync execution
   * 
   * @param cdmId
   *          CDM ID
   */
  @WebMethod
  public Integer executeSync(
      @WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "locality") String locality,
      @WebParam(name = "taskId") Integer taskId) throws BusinessException, SystemException;

  /**
   * Async execution
   * 
   * @param cdmId
   *          CDM ID
   */
  @WebMethod
  public void executeAsync(
      @WebParam(name = "cdmId") String cdmId,
      @WebParam(name = "locality") String locality,
      @WebParam(name = "taskId") Integer taskId ) throws BusinessException, SystemException;

}
