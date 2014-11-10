package com.logica.ndk.jbpm.core.integration.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.ProcessInstance;
import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.commons.shutdown.ShutdownAttribute;
import com.logica.ndk.commons.shutdown.ShutdownException;
import com.logica.ndk.jbpm.core.CommandDelegate;
import com.logica.ndk.jbpm.core.integration.ProcessManagement;
import com.logica.ndk.jbpm.core.integration.api.ProcessInstanceEndLog;
import com.logica.ndk.jbpm.core.integration.api.ProcessInstanceEndLogFilter;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.process.Node;
import com.logica.ndk.tm.process.NodeList;
import com.logica.ndk.tm.process.ParamMap;
import com.logica.ndk.tm.process.ParamMapItem;
import com.logica.ndk.tm.process.ProcessInstanceLimit;
import com.logica.ndk.tm.process.ProcessInstanceLimitExceededException;
import com.logica.ndk.tm.process.ProcessState;
import com.logica.ndk.tm.process.ProcessSystemVariables;
import com.logica.ndk.tm.process.ProcessTimeout;

public class ProcessManagementImpl implements ProcessManagement {
  private static final Logger LOG = LoggerFactory.getLogger(ProcessManagementImpl.class);

  private CommandDelegate delegate;

  public ProcessManagementImpl() {
    delegate = new CommandDelegate();
  }

  @Override
  public List<Long> getActiveInstances(String definitionId) {
    List<ProcessInstanceLog> processInstances = delegate.getActiveInstanceLog(definitionId);
    List<Long> result = new ArrayList<Long>();
    for (ProcessInstanceLog processInstance : processInstances) {
      result.add(processInstance.getProcessInstanceId());
    }
    return result;
  }

  @Override
  public List<ProcessState> getActiveInstances() {
    List<ProcessInstanceLog> processInstances = delegate.getActiveInstanceLog();
    List<ProcessState> list = new ArrayList<ProcessState>();
    for (ProcessInstanceLog processInstanceLog : processInstances) {
      list.add(Transform.processState(processInstanceLog));
    }
    return list;
  }

  @Override
  public ProcessState getProcessInstanceLog(long processInstanceId) {
    ProcessInstanceLog processInstanceLog = delegate.getProcessInstanceLog(processInstanceId);
    if (processInstanceLog != null) {
      return Transform.processState(processInstanceLog);
    }
    return null;
  }

  @Override
  public void endInstance(long instanceId, String initiator) {
    checkShutdownAttribute();
    if (initiator != null && initiator.length() > 0) {
      Map<String, Object> variables = new HashMap<String, Object>();
      variables.put(ProcessSystemVariables.SYS_004_ABORT_INITIATOR.toString(), initiator);
      delegate.setProcessInstanceVariables(instanceId, variables);
    }
    delegate.abortProcessInstance(instanceId);
  }

  @Override
  public void signalEvent(String executionId, String type, Object event) {
    checkShutdownAttribute();
    delegate.signalEvent(executionId, type, event);
  }

  @Override
  public void signalEvent(String type, Object event) {
    checkShutdownAttribute();
    //delegate.signalEvent(type, event); Bug in JBPM - does not work
    LOG.info("Generating events of type " + type + " with data: " + event);
    List<Object> processIds = TmConfig.instance().getList("jbpmTimer.timer." + type);
    LOG.info("Process Ids: " + processIds);
    for (Object processId : processIds) {
      List<Long> instanceIds = getActiveInstances((String)processId);
      for (Long instanceId: instanceIds) {
        LOG.info("Sendign evet to instanceId " + instanceId);
        signalEvent(Long.toString(instanceId), type, event);
      }
    }
    LOG.info("Sending events finished");
  }

  @Override
  public ProcessInstance createProcessInstance(String processId, Map<String, Object> parameters) throws ProcessInstanceLimitExceededException {
    checkShutdownAttribute();
    if (parameters == null || !parameters.containsKey(ProcessTimeout.PARAMETER_NAME)) {
      parameters.put(ProcessTimeout.PARAMETER_NAME, ProcessTimeout.DEFAULT_VALUE);
    }
    checkProcessInstanceLimitExceeded(processId, parameters);
    return delegate.createProcessInstance(processId, parameters);
  }

  @Override
  public ProcessInstance startProcessInstance(long processInstanceId) {
    checkShutdownAttribute();
    return delegate.startProcessInstance(processInstanceId);
  }

  @Override
  public ProcessInstance startProcess(String processId) {
    checkShutdownAttribute();
    return delegate.startProcess(processId);
  }

  @Override
  public ProcessInstance startProcess(String processId, Map<String, Object> parameters) {
    checkShutdownAttribute();
    if (parameters == null || !parameters.containsKey(ProcessTimeout.PARAMETER_NAME)) {
      parameters.put(ProcessTimeout.PARAMETER_NAME, ProcessTimeout.DEFAULT_VALUE);
    }
    return delegate.startProcess(processId, parameters);
  }

  @Override
  public ProcessState state(long instanceId) {
    // if only created - PROCESSINSTANCEINFO - created instance not in
    // PROCESSINSTANCELOG
    // if running - PROCESSINSTANCEINFO
    // if finished - PROCESSINSTANCEENDLOG
    // not exists
    LOG.info("get state for instanceId: " + instanceId);
    ProcessState processState = null;
    ProcessInstanceEndLog processInstanceEndLog = delegate.getProcessInstanceEndLog(instanceId);
    if (processInstanceEndLog != null) {
      processState = stateFromLog(processInstanceEndLog);
      LOG.info("processInstance was loaded from endLog data!");
    }
    else {
      LOG.info("Process instance not found in LOG. We are going to search in runtime");
      try {
        // TODO [rda]: bezpecnejsie je citat info z LOG, ale tam nie su vsetky informacie
        ProcessInstance processInstance = delegate.getProcessInstance(instanceId);
        processState = stateFromRuntime(processInstance, instanceId);
        LOG.info("processInstance was loaded from runnig data!");
      }
      catch (Exception e) {
        LOG.info("Process instance not found in runtime.");
      }
    }
    if (processState == null) {
      LOG.info("processInstance doesn't exist!");
    }
    return processState;
  }

  @Override
  public List<ProcessState> findProcessInstanceEndLog(ProcessInstanceEndLogFilter filter) {
    List<ProcessInstanceEndLog> list = delegate.findProcessInstanceEndLog(filter);
    List<ProcessState> result = new ArrayList<ProcessState>();
    if (list != null) {
      for (ProcessInstanceEndLog processInstanceEndLog : list) {
        result.add(Transform.processState(processInstanceEndLog));
      }
    }
    return result;
  }

  private ProcessState stateFromRuntime(ProcessInstance processInstance, long instanceId) {
    // main info
    ProcessState processState = new ProcessState();
    processState.setProcessId(processInstance.getProcessId());
    processState.setInstanceId(processInstance.getId());
    processState.setState(processInstance.getState());
    // variables
    ParamMap paramMap = new ParamMap();
    Map<String, Object> processInstanceVariables = delegate.getProcessInstanceVariables(instanceId);
    for (String key : processInstanceVariables.keySet()) {
      ParamMapItem item = new ParamMapItem();
      item.setName(key);
      item.setValue(processInstanceVariables.get(key).toString());
      paramMap.getItems().add(item);
    }
    processState.setParameters(paramMap);
    // nodes
    processState.setNodes(getNodeList(instanceId));
    return processState;
  }
  
  private ProcessState stateFromLog(ProcessInstanceEndLog processInstanceEndLog) {
    ProcessState processState = Transform.processState(processInstanceEndLog);
    processState.setNodes(getNodeList(processInstanceEndLog.getProcessInstanceId()));
    return processState;
  }
  
  private NodeList getNodeList(long instanceId) {
    List<NodeInstanceLog> activeNodeInfo = delegate.getNodeInfo(instanceId);
    NodeList nodeList = new NodeList();
    Map<Long, Node> nodes = new HashMap<Long, Node>();
    for (NodeInstanceLog nodeInstanceLog : activeNodeInfo) {
      if (nodeInstanceLog.getNodeName() != null && nodeInstanceLog.getNodeName().length() > 0) {
        Node node = nodes.get(Long.parseLong(nodeInstanceLog.getNodeInstanceId()));
        if (node == null) {
          node = new Node();
          node.setName(nodeInstanceLog.getNodeName());
          nodes.put(Long.parseLong(nodeInstanceLog.getNodeInstanceId()), node);
        }
        if (nodeInstanceLog.getType() == NodeInstanceLog.TYPE_ENTER) {
          node.setStartDate(Transform.dateToXMLGregorianCalendar(nodeInstanceLog.getDate()));
        }
        else if (nodeInstanceLog.getType() == NodeInstanceLog.TYPE_EXIT) {
          node.setEndDate(Transform.dateToXMLGregorianCalendar(nodeInstanceLog.getDate()));
        }
        else {
          LOG.warn("Uknown type of nodeInstanceLog. Id:" + nodeInstanceLog.getId());
        }
      }
    }
    // sort by nodeInstanceId
    Object[] keys = nodes.keySet().toArray();
    Arrays.sort(keys);
    for (Object key : keys) {
      nodeList.getItems().add(nodes.get((Long) key));
    }
    return nodeList;
  }

  @Override
  public String abortProcessInstanceByWorkItemHandler(long processInstanceId, Map<String, Object> variables) {
    return delegate.abortProcessInstanceByWorkItemHandler(processInstanceId, variables);
  }

  @Override
  public void completeWorkItem(long workItemId, Map<String, Object> results) {
    delegate.completeWorkItem(workItemId, results);
  }

  @Override
  public List<Long> getActiveInstancesExceedTimeout() {
    LOG.info("getActiveInstancesExceedTimeout started");
    List<Long> result = new ArrayList<Long>();
    List<ProcessInstanceLog> processInstances = delegate.getActiveInstanceLog();
    LOG.info("have list<ProcessInstanceLog>");
    if (processInstances != null) {
      long currentTime = System.currentTimeMillis();
      for (ProcessInstanceLog processInstanceLog : processInstances) {
        LOG.info("start handling process id: " + processInstanceLog.getProcessInstanceId());
        Map<String, Object> processInstanceVariables = delegate.getProcessInstanceVariables(processInstanceLog.getProcessInstanceId());
        
        if (processInstanceVariables != null && processInstanceVariables.containsKey(ProcessTimeout.PARAMETER_NAME)) {
          Long timeout = null;
          LOG.info("start handling process id: " + processInstanceLog.getProcessInstanceId() + " have process variables");
          
          if (processInstanceVariables.get(ProcessTimeout.PARAMETER_NAME) instanceof Long) {
            timeout = (Long) processInstanceVariables.get(ProcessTimeout.PARAMETER_NAME);
          }
          else {
            String s = processInstanceVariables.get(ProcessTimeout.PARAMETER_NAME).toString();
            try {
              timeout = Long.valueOf(s);
            }
            catch (Exception e) {
              LOG.warn("Incorrect format of timeout parameter. Number is expected but value is: " + s);
            }
          }
          LOG.info("check if process id: " + processInstanceLog.getProcessInstanceId() + " reach timeout");
          if (timeout != null) {
            long startTime = processInstanceLog.getStart().getTime();
            if (startTime + timeout.longValue() < currentTime) {
              result.add(processInstanceLog.getProcessInstanceId());
            }
          }
        }
      }
    }
    return result;
  }

  @Override
  public List<Long> endInstancesExceedTimeout(String initiator) {
    checkShutdownAttribute();
    List<Long> instances = getActiveInstancesExceedTimeout();
    if (instances != null) {
      LOG.info("Ending instance exceed timeout");
      for (Long instanceId : instances) {
        endInstance(instanceId, initiator);
      }
      LOG.info("Ending instance exceed timeout finis");
    }
    return instances;
  }
  
  private void checkShutdownAttribute() throws ShutdownException {
    ShutdownAttribute.checkShutdownAttribute();
  }

  @Override
  public ProcessState getProcessInstanceEndLog(long processInstanceId) {
    return Transform.processState(delegate.getProcessInstanceEndLog(processInstanceId));
  }
  
  private void checkProcessInstanceLimitExceeded(String processId, Map<String, Object> parameters) throws ProcessInstanceLimitExceededException {
    if (processId == null || parameters == null || parameters.size() == 0 || parameters.containsKey(ProcessInstanceLimit.NAME) == false) {
      return;
    }
    Integer limitParameter = null;
    try {
      limitParameter = Integer.parseInt((String) parameters.get(ProcessInstanceLimit.NAME));
    }
    catch (NumberFormatException e) {
      throw new IllegalArgumentException("ProcessInstanceLimit parameter has incorrect number format.");
    }
    if (limitParameter == null || limitParameter.intValue() < 0) {
      return;
    }
    List<ProcessInstanceLog> processInstances = delegate.getActiveInstanceLog();
    int countInstances = 0;
    for (ProcessInstanceLog processInstanceLog : processInstances) {
      if (processInstanceLog.getProcessId().equals(processId)) {
        countInstances++;
      }
    }
    if (countInstances >= limitParameter.intValue()) {
      LOG.error("ProcessInstanceLimitExceeded for process: " + processId + " Active instances: " + countInstances + ". Limit: " + limitParameter.intValue());
      throw new ProcessInstanceLimitExceededException("ProcessInstanceLimitExceeded for process: " + processId + " Active instances: " + countInstances + ". Limit: " + limitParameter.intValue());
    }
  }
}
