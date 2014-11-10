/**
 * 
 */
package com.logica.ndk.tm.utilities.integration.wf;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.wf.exception.BadRequestException;

/**
 * @author kovalcikm
 *
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface DeactivateInWF{
  @WebMethod
  public List<String> executeSync (
       @WebParam(name = "cdmId") String cdmId, 
       @WebParam(name = "recordIdentifier") String recordIdentifier) throws BadRequestException, BusinessException, SystemException;


  @WebMethod
  public void executeAsync(
      @WebParam(name = "cdmId") String cdmId, 
      @WebParam(name = "recordIdentifier") String recordIdentifier) throws BadRequestException, BusinessException, SystemException;
}
