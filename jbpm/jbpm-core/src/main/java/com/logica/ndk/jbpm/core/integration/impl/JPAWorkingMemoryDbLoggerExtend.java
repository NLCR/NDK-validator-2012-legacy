package com.logica.ndk.jbpm.core.integration.impl;

import org.drools.audit.event.LogEvent;
import org.drools.event.KnowledgeRuntimeEventManager;
import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;

import com.logica.ndk.jbpm.core.integration.api.ProcessInstanceEndLog;

/**
 * Use this class to implement methods to store some LOG data about process. Do not use this class to find or get these
 * data. Use class JPAProcessInstanceDbLogExtend for this.
 * 
 * @author Rudolf Daco
 */
public class JPAWorkingMemoryDbLoggerExtend extends JPAWorkingMemoryDbLogger {
  
  public JPAWorkingMemoryDbLoggerExtend(KnowledgeRuntimeEventManager session) {
    super(session);
  }

  public void logEventCreated(LogEvent logEvent) {
    switch (logEvent.getType()) {
      case LogEvent.BEFORE_RULEFLOW_CREATED:
        super.logEventCreated(logEvent);
        break;
      case LogEvent.AFTER_RULEFLOW_COMPLETED:
        super.logEventCreated(logEvent);
        break;
      case LogEvent.BEFORE_RULEFLOW_NODE_TRIGGERED:
        super.logEventCreated(logEvent);
        break;
      case LogEvent.BEFORE_RULEFLOW_NODE_EXITED:
        super.logEventCreated(logEvent);
        break;
      case LogEvent.AFTER_VARIABLE_INSTANCE_CHANGED:
        // nothing - we don't need to log variables
        break;
      default:
        // ignore all other events
    }
  }

  public void createProcessInstanceEndLog(ProcessInstanceEndLog processInstanceEndLog) {
    getEntityManager().persist(processInstanceEndLog);
  }
}
