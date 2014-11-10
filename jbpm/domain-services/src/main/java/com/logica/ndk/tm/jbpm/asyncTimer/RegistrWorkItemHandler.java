package com.logica.ndk.tm.jbpm.asyncTimer;

import org.drools.process.instance.WorkItemHandler;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.jbpm.core.integration.impl.AsyncTimerServiceImpl;
import com.logica.ndk.jbpm.core.integration.impl.DAOException;

public class RegistrWorkItemHandler implements WorkItemHandler{  
  
  protected final Logger log = LoggerFactory.getLogger(getClass());
  
  public RegistrWorkItemHandler() {
    // TODO Auto-generated constructor stub
  }

  @Override
  public void abortWorkItem(WorkItem workItem, WorkItemManager workItemManager) {    

    
  }

  @Override
  public void executeWorkItem(WorkItem workItem, WorkItemManager workItemManager) {
    log.info("Executing workItem for processInstanceId: " + workItem.getProcessInstanceId());
    
    Integer secondsToWait = Integer.parseInt((String) workItem.getParameters().get("waitTime"));    
     
    try {
      new AsyncTimerServiceImpl().register( workItem.getId(), workItem.getProcessInstanceId(), secondsToWait);
    }
    catch (DAOException e) {
      log.error("Error while registring utility!" , e);
    }    
  }

}
