package com.logica.ndk.tm.jbpm.handler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.drools.runtime.process.WorkItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.jbpm.core.integration.api.ActiveWorkItem;

/**
 * @author Rudolf Daco
 *
 */
public class ActiveWorkItemManagerTest implements ActiveWorkItemManager {
  protected final Logger log = LoggerFactory.getLogger(ActiveWorkItemManagerTest.class);

  private static HashMap<String, ActiveWorkItem> activeItems = new HashMap<String, ActiveWorkItem>();
  
  @Override
  public String abortProcessInstanceByWorkItemHandler(long processInstanceId, String workItemName, Throwable t) {
    log.info("Aborting process instance {} by handler {}", processInstanceId, workItemName);
    return "";
  }
  
  @Override
  public void abortProcessInstanceByWorkItemHandler(long processInstanceId, Map<String, Object> variables) {
    log.info("Aborting process instance {}", processInstanceId);
  }

  @Override
  public void completeWorkItem(long workItemId, Map<String, Object> results) {
    log.info("Commpleting work item with id {}", workItemId);
  }

  @Override
  public void completeWorkItem(ActiveWorkItem activeWorkItem, Map<String, Object> results) {
    log.info("Commpleting work item {}", activeWorkItem);
  }

  @Override
  public void addWorkItem(WorkItem workItem, Class<? extends AbstractAsyncHandler> WorkItemHandlerClass, String correlationId) {
    ActiveWorkItem activeWorkItem = new ActiveWorkItem();
    activeWorkItem.setCorrelationId(correlationId);
    activeWorkItem.setProcessInstanceId(workItem.getProcessInstanceId());
    activeWorkItem.setWorkItemHandlerClass(WorkItemHandlerClass.getName());
    activeWorkItem.setWorkItemId(workItem.getId());
    activeWorkItem.setCreated(new Date());
    synchronized (activeItems) {
      activeItems.put(correlationId, activeWorkItem);
    }
  }

  @Override
  public void deleteWorkItem(String correlationId) {
    synchronized (activeItems) {
      activeItems.remove(correlationId);
    }
  }

  @Override
  public ActiveWorkItem getWorkItem(String correlationId) {
    synchronized (activeItems) {
      return activeItems.get(correlationId);
    }
  }
}
