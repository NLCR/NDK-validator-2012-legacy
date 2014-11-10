package com.logica.ndk.tm.jbpm.handler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.drools.runtime.process.WorkItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.jbpm.core.integration.ManagementFactory;
import com.logica.ndk.jbpm.core.integration.api.ActiveWorkItem;
import com.logica.ndk.jbpm.core.integration.impl.ServiceException;
import com.logica.ndk.tm.process.JBPMSystemException;
import com.logica.ndk.tm.process.ProcessSystemVariables;
import com.logica.ndk.tm.utilities.ErrorHelper;
import com.logica.ndk.tm.utilities.ExceptionMessage;
import com.logica.ndk.tm.utilities.UtilityException;
import com.logica.ndk.tm.utilities.validation.ValidationException;

/**
 * @author Rudolf Daco
 *
 */
public class ActiveWorkItemManagerImpl implements ActiveWorkItemManager {
  protected final Logger log = LoggerFactory.getLogger(ActiveWorkItemManagerImpl.class);

  @Override
  public String abortProcessInstanceByWorkItemHandler(long processInstanceId, String workItemName, Throwable t) {
    log.info("Aborting process instance {} by handler {}", processInstanceId, workItemName);
    Map<String, Object> variables = null;
    if (t instanceof ExceptionMessage) {
      variables = prepareVariables(workItemName, t);
    }
    else {
      variables = prepareVariables(workItemName, t);
    }
    return new ManagementFactory().createProcessManagement().abortProcessInstanceByWorkItemHandler(processInstanceId, variables);
  }
  
  private Map<String, Object> prepareVariables(String workItemName, Throwable t) {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put(ProcessSystemVariables.SYS_001_EX_HANDLER_NAME.toString(), workItemName);
    variables.put(ProcessSystemVariables.SYS_002_EX_HANDLER_EX_MSG.toString(), ErrorHelper.getStackTrace(t));
    variables.put(ProcessSystemVariables.SYS_005_EX_HANDLER_ERROR_CODE.toString(), ErrorHelper.getErrorCode(t));
    variables.put(ProcessSystemVariables.SYS_006_EX_HANDLER_EX_MSG_LOCAL.toString(), ErrorHelper.getLocalizedMessage(t));
    if(t instanceof ExceptionMessage) {
      ExceptionMessage exMessage = (ExceptionMessage) t;
      variables.put(ProcessSystemVariables.SYS_003_EX_HANDLER_EX_CLASS.toString(), exMessage.getExceptionName());
      variables.put(ProcessSystemVariables.SYS_007_EX_NODE_ID.toString(), exMessage.getNodeId());
    } else if (t instanceof UtilityException) {
      UtilityException utilEx = (UtilityException) t;
      variables.put(ProcessSystemVariables.SYS_003_EX_HANDLER_EX_CLASS.toString(), t.getClass().getName());
      variables.put(ProcessSystemVariables.SYS_007_EX_NODE_ID.toString(), utilEx.getNodeId());
    } else {
      variables.put(ProcessSystemVariables.SYS_003_EX_HANDLER_EX_CLASS.toString(), t.getClass().getName());
    }
    return variables;
  }

  @Override
  public void abortProcessInstanceByWorkItemHandler(long processInstanceId, Map<String, Object> variables) {
    log.info("Aborting process instance {}", processInstanceId);
    new ManagementFactory().createProcessManagement().abortProcessInstanceByWorkItemHandler(processInstanceId, variables);
  }

  @Override
  public void completeWorkItem(long workItemId, Map<String, Object> results) {
    log.info("Commpleting work item with id {}", workItemId);
    new ManagementFactory().createProcessManagement().completeWorkItem(workItemId, results);
  }

  @Override
  public void completeWorkItem(ActiveWorkItem activeWorkItem, Map<String, Object> results) {
    log.info("Commpleting work item {}", activeWorkItem);
    new ManagementFactory().createProcessManagement().completeWorkItem(activeWorkItem.getWorkItemId(), results);
  }

  @Override
  public void addWorkItem(WorkItem workItem, Class<? extends AbstractAsyncHandler> WorkItemHandlerClass, String correlationId) throws JBPMSystemException {
    ActiveWorkItem activeWorkItem = new ActiveWorkItem();
    activeWorkItem.setCorrelationId(correlationId);
    activeWorkItem.setProcessInstanceId(workItem.getProcessInstanceId());
    activeWorkItem.setWorkItemHandlerClass(WorkItemHandlerClass.getName());
    activeWorkItem.setWorkItemId(workItem.getId());
    activeWorkItem.setCreated(new Date());
    try {
      new ManagementFactory().createActiveWorkItemService().add(activeWorkItem);
    }
    catch (ServiceException e) {
      throw new JBPMSystemException(e);
    }
  }

  @Override
  public void deleteWorkItem(String correlationId) throws JBPMSystemException {
    try {
      new ManagementFactory().createActiveWorkItemService().delete(correlationId);
    }
    catch (ServiceException e) {
      throw new JBPMSystemException(e);
    }
  }

  @Override
  public ActiveWorkItem getWorkItem(String correlationId) throws JBPMSystemException {
    try {
      ActiveWorkItem activeWorkItem = new ManagementFactory().createActiveWorkItemService().get(correlationId);
      if (activeWorkItem != null) {
        log.info("Found activeWorkItem  for correlationId: " + correlationId + "is\n workItemId: " + activeWorkItem.getWorkItemId() + " class: " + activeWorkItem.getWorkItemHandlerClass() + 
            " processInstanceId: " + activeWorkItem.getProcessInstanceId());
      }
      return activeWorkItem;
    }
    catch (ServiceException e) {
      throw new JBPMSystemException(e);
    }
  }
}
