package com.logica.ndk.tm.utilities.integration.wf;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.wf.exception.BadRequestException;

/**
 * Create intellectual entities based on CDM
 * @author majdaf
 *
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CreateIntEntities {

  /**
   * Execute sync
   * @param taskId WF package task ID
   * @param cdmId Main CDM ID
   * @param childCdmIds Child CDM IDs
   * @return List of taskIds
   */
  @WebMethod
  @WebResult(name = "taskIds")
  public List<String> executeSync (
       @WebParam(name = "taskId") Long taskId, 
       @WebParam(name = "cdmId") String cdmId, 
       @WebParam(name = "childCdmIds") List<String> childCdmIds) throws BadRequestException, BusinessException, SystemException;

  /**
   * Execute async 
   * @param cdmId Main CDM ID
   * @param childCdmIds Child CDM IDs
   */
  @WebMethod
  public void executeAsync(
      @WebParam(name = "taskId") Long taskId, 
      @WebParam(name = "cdmId") String cdmId, 
      @WebParam(name = "childCdmIds") List<String> childCdmIds) throws BadRequestException, BusinessException, SystemException;

}
