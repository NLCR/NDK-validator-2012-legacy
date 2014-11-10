package com.logica.ndk.tm.utilities.integration.jbpm;

import javax.jws.WebMethod;
import javax.jws.WebParam;

import com.logica.ndk.tm.utilities.SystemException;


/**
 * Sends event to each process instance accoridng to event type defined in tm-config
 * @author majdaf
 *
 */
public interface EventTriger {

  /**
   * Send event to processes
   * @param timer Type of timer
   * @param eventData Event data
   * @throws JBPMBusinessException_Exception
   * @throws JBPMSystemException_Exception
   */
  @WebMethod
  public void execute(
      @WebParam(name = "timer") String timer, 
      @WebParam(name = "eventData") String eventData
  ) throws SystemException;
  
}
