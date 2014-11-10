package com.logica.ndk.jbpm.core.listener;

import java.util.Date;
import java.util.Map;

import org.drools.event.process.ProcessCompletedEvent;
import org.drools.event.process.ProcessEventListener;
import org.drools.event.process.ProcessNodeLeftEvent;
import org.drools.event.process.ProcessNodeTriggeredEvent;
import org.drools.event.process.ProcessStartedEvent;
import org.drools.event.process.ProcessVariableChangedEvent;
import org.drools.runtime.process.ProcessInstance;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.jbpm.core.SessionFactory;
import com.logica.ndk.jbpm.core.SpringContextHolder;
import com.logica.ndk.jbpm.core.integration.ManagementFactory;
import com.logica.ndk.jbpm.core.integration.api.ProcessInstanceEndLog;
import com.logica.ndk.jbpm.core.integration.impl.JPAWorkingMemoryDbLoggerExtend;
import com.logica.ndk.jbpm.core.integration.impl.Transform;
import com.logica.ndk.tm.process.Notify;
import com.logica.ndk.tm.process.ParamMap;
import com.logica.ndk.tm.process.ParamMapItem;
import com.logica.ndk.tm.process.ProcessState;

public class ProcessEventListenerImpl implements ProcessEventListener {
  private static final Logger LOG = LoggerFactory.getLogger(ProcessEventListenerImpl.class);

  public ProcessEventListenerImpl(){
    LOG.info("Process event listener created");
  }
  
  @Override
  public void beforeProcessStarted(ProcessStartedEvent event) {
  }

  @Override
  public void afterProcessStarted(ProcessStartedEvent event) {
  }

  @Override
  public void beforeProcessCompleted(ProcessCompletedEvent event) {
  }

  @Override
  public void afterProcessCompleted(ProcessCompletedEvent event) {
    try {      
      ProcessInstance processInstance = event.getProcessInstance();
      LOG.info("Listerner: afterProcessCompleted. processId:<" + processInstance.getProcessId()
          + "> instanceId:<" + processInstance.getId() + ">.");
      Map<String, Object> variables = null;
      if (processInstance != null) {
        variables = ((WorkflowProcessInstanceImpl) processInstance).getVariables();
      }
      
      ProcessState processState = new ProcessState();
      processState.setProcessName(processInstance.getProcessName());
      processState.setProcessId(processInstance.getProcessId());
      processState.setInstanceId(processInstance.getId());
      processState.setState(processInstance.getState());
      ParamMap paramMap = new ParamMap();
      if (variables != null) {
        for (String key : variables.keySet()) {
          ParamMapItem item = new ParamMapItem();
          item.setName(key);
          Object value = variables.get(key);
          if (value != null) {
            item.setValue(value.toString());
          }
          paramMap.getItems().add(item);
        }
      }
      processState.setParameters(paramMap);
      // start date and en dis not in PI - must be loaded from LOG
      ProcessState processStateFromLog = new ManagementFactory().createProcessManagement().getProcessInstanceLog(processInstance.getId());
      processState.setStartDate(processStateFromLog.getStartDate());
      if (processStateFromLog.getEndDate() != null) {
        processState.setEndDate(processStateFromLog.getEndDate());
      }
      else {
        processState.setEndDate(Transform.dateToXMLGregorianCalendar(new Date()));
      }
      logProcessState(processState);
      persistVariablesEndLog(processState);
      sendNotify(processState);
      LOG.info("Listerner: afterProcessCompleted. finish");
    }
    catch (Exception e) {
      LOG.error("error", e);
    }
  }

  @Override
  public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
  }

  @Override
  public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
  }

  @Override
  public void beforeNodeLeft(ProcessNodeLeftEvent event) {
  }

  @Override
  public void afterNodeLeft(ProcessNodeLeftEvent event) {
  }

  @Override
  public void beforeVariableChanged(ProcessVariableChangedEvent event) {
  }

  @Override
  public void afterVariableChanged(ProcessVariableChangedEvent event) {
  }

  private void sendNotify(ProcessState processState) {
    Notify client = (Notify) SpringContextHolder.getInstance().getContext().getBean("notificationClient");
    try {
      LOG.info("Calling ws notify.");
      client.notify(processState);
      LOG.info("Calling ws notify was OK.");
    }
    catch (Exception e) {
      LOG.error("Some expception during WS call: " + e.getMessage());
    }
  }

  /**
   * We need to persist only end state of variables. State of process is counted from other values (see
   * ProcessManagementImpl.state)
   * 
   * @param processState
   */
  private void persistVariablesEndLog(ProcessState processState) {
    ProcessInstanceEndLog processInstanceEndLog = Transform.processInstanceEndLog(processState);
    JPAWorkingMemoryDbLoggerExtend dbLogger = SessionFactory.getDbLogger();
    dbLogger.createProcessInstanceEndLog(processInstanceEndLog);
  }

  private void logProcessState(ProcessState processState) {
    LOG.info("--- ProcessState at the end of process execution is (START of log) ---");
    LOG.info("ProcessName: " + processState.getProcessName());
    LOG.info("ProcessId: " + processState.getProcessId());
    LOG.info("InstanceId: " + processState.getInstanceId());
    LOG.info("State: " + processState.getState());
    LOG.info("Parameters:");
    ParamMap parameters = processState.getParameters();
    if (parameters != null && parameters.getItems() != null) {
      for (ParamMapItem paramMapItem : parameters.getItems()) {
        LOG.info(paramMapItem.getName() + " : " + paramMapItem.getValue());
      }
    }
    LOG.info("--- ProcessState at the end of process execution is (END of log) ---");
  }
}
