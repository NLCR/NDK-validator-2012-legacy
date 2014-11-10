package com.logica.ndk.tm.utilities.integration.jbpm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.jbpm.JBPMBusinessException_Exception;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.jbpm.JBPMSystemException_Exception;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.jbpm.JBPMWSFacadeClient;

/**
 * Sends event to each process instance accoridng to event type defined in tm-config
 * @author majdaf
 *
 */
public class EventTrigerImpl {
  protected final transient Logger log = LoggerFactory.getLogger(getClass());
  JBPMWSFacadeClient jbpmClient = null;

  /**
   * Send event to processes
   * @param timer Type of timer
   * @param eventData Event data
   * @throws JBPMBusinessException_Exception
   * @throws JBPMSystemException_Exception
   */
  public void execute(String timer, String eventData) throws SystemException {
    log.debug("Generating events of type " + timer + " with data: " + eventData);
    jbpmClient = getJBPMClient();
    try {
      jbpmClient.signalEvent(timer, eventData);
    }
    catch (Exception e) {
      log.error(e.getMessage());
      throw new SystemException(e);
    }
    log.debug("Sending events finished");
    
  }
  
  JBPMWSFacadeClient getJBPMClient() {
    if (jbpmClient == null) {
      log.info("Init JBPM client");
      return new JBPMWSFacadeClient();
    } else {
      return jbpmClient;
    }
  }

}
