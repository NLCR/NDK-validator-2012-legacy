package com.logica.ndk.tm.utilities.transformation.sip1;

import java.io.IOException;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Create SIP1 from CDM a copy it to folder for LTP
 * 
 * @author majdaf
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CreateSIP1FromCDM {

  /**
   * Sync execution
   * 
   * @param cdmId
   *          CDM ID
   */
  @WebMethod
  public Integer executeSync(
      @WebParam(name = "cdmId") String cdmId) throws IOException, BusinessException, SystemException;

  /**
   * Async execution
   * 
   * @param cdmId
   *          CDM ID
   */
  @WebMethod
  public void executeAsync(
      @WebParam(name = "cdmId") String cdmId) throws IOException, BusinessException, SystemException;

}
