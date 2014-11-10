package com.logica.ndk.tm.utilities.integration.wf;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.process.ProcessState;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.wf.exception.BadRequestException;
import com.logica.ndk.tm.utilities.integration.wf.exception.WFConnectionUnavailableException;

/**
 * Handles processes finshed by JBPM and notifies WF of the finished task
 * @author majdaf
 *
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface ProcessFinishedHandler {

  /**
   * Trasnform the finished process information to the finished task infromation for WF
   * @param state State of th finished process
   * @throws BadRequestException 
   */
  @WebMethod
  public void handleFinishedProcess(@WebParam(name = "processState") ProcessState state)
  throws WFConnectionUnavailableException, BadRequestException, BusinessException, SystemException;
}
