package com.logica.ndk.tm.jbpm.handler;

import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.jbpm.core.integration.api.ActiveWorkItem;
import com.logica.ndk.tm.process.JBPMSystemException;

/**
 * @author Rudolf Daco
 *
 */
public interface ActiveWorkItemManager {

  public String abortProcessInstanceByWorkItemHandler(long processInstanceId, String workItemName, Throwable t);
  
  public void abortProcessInstanceByWorkItemHandler(long processInstanceId, Map<String, Object> variables);

  public void completeWorkItem(long workItemId, Map<String, Object> results);

  public void completeWorkItem(ActiveWorkItem activeWorkItem, Map<String, Object> results);

  public void addWorkItem(WorkItem workItem, Class<? extends AbstractAsyncHandler> WorkItemHandlerClass, String correlationId) throws JBPMSystemException;

  public void deleteWorkItem(String correlationId) throws JBPMSystemException;

  public ActiveWorkItem getWorkItem(String correlationId) throws JBPMSystemException;

}
