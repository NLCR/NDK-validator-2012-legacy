package com.logica.ndk.tm.utilities.file;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Utility to create empty CDM file srtucture.
 * 
 * @author Rudolf Daco
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CreateEmptyCdm {
  /**
   * Creates empty file structure for specified cdmId.
   * 
   * @param barCode
   * @return cdmId
   * @throws
   */
  @WebMethod
  public String executeSync(@WebParam(name = "barCode") String barCode,
      @WebParam(name = "taskId") String taskId) throws BusinessException, SystemException;

  @WebMethod
  public void executeAsync(@WebParam(name = "barCode") String barCode,
      @WebParam(name = "taskId") String taskId) throws BusinessException, SystemException;
}
