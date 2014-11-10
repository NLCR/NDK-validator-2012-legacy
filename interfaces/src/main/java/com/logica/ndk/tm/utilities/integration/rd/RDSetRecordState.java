package com.logica.ndk.tm.utilities.integration.rd;

import java.util.Date;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Update record state in Digitization register.
 * 
 * @author ondrusekl
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface RDSetRecordState {

  /**
   * Update record state in RD.<br>
   * If newState == FINISHED and date == null, then date will be current date.
   * 
   * @param recordId
   *          RD internal identifier
   * @param newState
   *          Target record state
   * @param oldState
   *          Former record state
   * @param user
   *          User performing the change in WF
   * @param date
   *          Date of change in WF.
   * @return Result of the operation in RD. True means success.
   */
  @WebMethod
  public boolean setRecordStateSync(
      @WebParam(name = "recordId") Integer recordId,
      @WebParam(name = "newState") String newState,
      @WebParam(name = "oldState") String oldState,
      @WebParam(name = "user") String user,
      @WebParam(name = "date") Date date) throws BusinessException, SystemException;

  @WebMethod
  public void setRecordStateAsync(
      @WebParam(name = "recordId") Integer recordId,
      @WebParam(name = "newState") String newState,
      @WebParam(name = "oldState") String oldState,
      @WebParam(name = "user") String user,
      @WebParam(name = "date") Date date) throws BusinessException, SystemException;
}
