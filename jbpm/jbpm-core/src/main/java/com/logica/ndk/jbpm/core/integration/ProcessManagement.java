package com.logica.ndk.jbpm.core.integration;

import java.util.List;
import java.util.Map;

import org.drools.runtime.process.ProcessInstance;

import com.logica.ndk.jbpm.core.integration.api.ProcessInstanceEndLogFilter;
import com.logica.ndk.tm.process.ProcessInstanceLimitExceededException;
import com.logica.ndk.tm.process.ProcessState;

public interface ProcessManagement {
	public List<Long> getActiveInstances(String definitionId);
	
	public List<ProcessState> getActiveInstances();

	public ProcessState getProcessInstanceLog(long processInstanceId);

	public void endInstance(long instanceId, String initiator);

	public void signalEvent(String executionId, String type, Object event);

	public void signalEvent(String type, Object event);

	public ProcessInstance createProcessInstance(String processId, Map<String, Object> parameters) throws ProcessInstanceLimitExceededException;

	public ProcessInstance startProcessInstance(long processInstanceId);

	public ProcessInstance startProcess(String processId);

	public ProcessInstance startProcess(String processId, Map<String, Object> parameters);

	public ProcessState state(long instanceId);
	
	public String abortProcessInstanceByWorkItemHandler(long processInstanceId, Map<String, Object> variables);
	
	public void completeWorkItem(long workItemId, Map<String, Object> results);

  public List<ProcessState> findProcessInstanceEndLog(ProcessInstanceEndLogFilter filter);
  
  public List<Long> getActiveInstancesExceedTimeout();
  
  public List<Long> endInstancesExceedTimeout(String initiator);
  
  public ProcessState getProcessInstanceEndLog(long processInstanceId);
}
