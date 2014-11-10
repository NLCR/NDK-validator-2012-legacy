/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.logica.ndk.jbpm.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


import javax.persistence.NoResultException;

import org.drools.SessionConfiguration;
import org.drools.command.Context;
import org.drools.command.impl.CommandBasedStatefulKnowledgeSession;
import org.drools.command.impl.GenericCommand;
import org.drools.command.impl.KnowledgeCommandContext;
import org.drools.definition.KnowledgePackage;
import org.drools.definition.process.Process;
import org.drools.runtime.Environment;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkItemHandler;
import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.jbpm.core.integration.api.ProcessInstanceEndLog;
import com.logica.ndk.jbpm.core.integration.api.ProcessInstanceEndLogFilter;
import com.logica.ndk.jbpm.core.integration.impl.JPAProcessInstanceDbLogExtend;

public class CommandDelegate {
  private static final Logger LOG = LoggerFactory.getLogger(CommandDelegate.class);

  private static StatefulKnowledgeSession session;
  private static Environment environment;

  private static synchronized StatefulKnowledgeSession getSession() {
    if (session == null) {
      try {
        session = SessionFactory.getSession();

      }
      finally {
        SessionFactory.returnSession();
      }
    }
    return session;
  }

  private static synchronized Environment getEnvironment() {
    if (environment == null) {
      try {
        environment = getSession().getEnvironment();
      }
      finally {
        SessionFactory.returnSession();
      }
    }
    return environment;
  }

  /**
   * @return list of packages defined in knowledge base (guvnor repository)
   */
  public List<KnowledgePackage> getPackages() {

    Collection<KnowledgePackage> knowledgePackages = getSession().getKnowledgeBase().getKnowledgePackages();
    if (knowledgePackages != null) {
      return new ArrayList<KnowledgePackage>(knowledgePackages);
    }
    else {
      return null;
    }

  }

  /**
   * @param packageName
   * @return list of processes for required package from knowledge base (guvnor repository)
   */
  public List<Process> getProcesses(String packageName) {

    List<Process> result = null;
    if (packageName != null) {
      KnowledgePackage knowledgePackage = getSession().getKnowledgeBase().getKnowledgePackage(packageName);
      if (knowledgePackage != null) {
        Collection<Process> processes = knowledgePackage.getProcesses();
        if (processes != null) {
          result = new ArrayList<Process>(processes);
        }
      }
    }

    return result;

  }

  /**
   * This method the variables provided in the map to the instance.
   * NOTE: the map will be added not replaced
   * 
   * @param processInstanceId
   * @param variables
   */
  public void setProcessInstanceVariables(final long processInstanceId, final Map<String, Object> variables) {

    ((CommandBasedStatefulKnowledgeSession) getSession()).getCommandService().execute(new GenericCommand<Void>() {
      private static final long serialVersionUID = -2977367819902535253L;

      public Void execute(Context context) {
        StatefulKnowledgeSession ksession = ((KnowledgeCommandContext) context).getStatefulKnowledgesession();
        ProcessInstance processInstance = ksession.getProcessInstance(processInstanceId);
        if (processInstance != null) {
          VariableScopeInstance variableScope = (VariableScopeInstance)
              ((org.jbpm.process.instance.ProcessInstance) processInstance).getContextInstance(VariableScope.VARIABLE_SCOPE);
          if (variableScope == null) {
            throw new IllegalArgumentException("Could not find variable scope for process instance " + processInstanceId);
          }
          for (Map.Entry<String, Object> entry : variables.entrySet()) {
            variableScope.setVariable(entry.getKey(), entry.getValue());
          }
          ksession = null;
        }
        else {
          ksession = null;
          throw new IllegalArgumentException("Could not find process instance " + processInstanceId);
        }
        return null;
      }

    });

  }

  //Retrun processId
  public String abortProcessInstance(long processInstanceId) {
    ProcessInstance processInstance = null;

    processInstance = getSession().getProcessInstance(processInstanceId);

    if (processInstance != null) {

      getSession().abortProcessInstance(processInstanceId);
      return processInstance.getProcessId();
    }
    else {
      throw new IllegalArgumentException("Could not find process instance " + processInstanceId);
    }
  }

  public ProcessInstance getProcessInstance(long processInstanceId) {

    ProcessInstance processInstance = getSession().getProcessInstance(processInstanceId);
    return processInstance;

  }

  public List<ProcessInstanceLog> getActiveInstanceLog(String processId) {

    List<ProcessInstanceLog> findActiveProcessInstances = new JPAProcessInstanceDbLogExtend(SessionFactory.getSession().getEnvironment()).findActiveProcessInstances(processId);
    return findActiveProcessInstances;

  }

  public List<ProcessInstanceLog> getActiveInstanceLog() {

    List<ProcessInstanceLog> findActiveProcessInstances = new JPAProcessInstanceDbLogExtend(SessionFactory.getSession().getEnvironment()).findActiveProcessInstances();
    return findActiveProcessInstances;

  }

  public ProcessInstanceLog getProcessInstanceLog(long processInstanceId) {
    ProcessInstanceLog processInstanceLog = null;
    try {
      processInstanceLog = new JPAProcessInstanceDbLogExtend(SessionFactory.getSession().getEnvironment()).findProcessInstance(processInstanceId);
    }
    catch (NoResultException e) {
      LOG.info("No instance found in instanceLog for processInstanceId: " + processInstanceId + ". This can occur if process instance was created but never started.");
    }

    return processInstanceLog;
  }

  public void signalEvent(String executionId, String type, Object event) {

    LOG.debug("Signaling process instance with executionId:<" + executionId + "> for type:<" + type + "> with event data:<" + event + ">");
    // pouzime volanie signal na session objekte radsej ako na processInstance
    getSession().signalEvent(type, event, Long.parseLong(executionId));

  }

  public void signalEvent(String type, Object event) {

    LOG.debug("Signaling process instance for type:<" + type + "> with event data:<" + event + ">");
    getSession().signalEvent(type, event);

  }

  public ProcessInstance createProcessInstance(String processId, Map<String, Object> parameters) {
    LOG.info("Create process instance started!");
    ProcessInstance createProcessInstance = getSession().createProcessInstance(processId, parameters);
    LOG.info("Create process instance ended!");
    return createProcessInstance;

  }

  public ProcessInstance startProcess(String processId, Map<String, Object> parameters) {

    ProcessInstance startProcess = getSession().startProcess(processId, parameters);
    return startProcess;

  }

  public ProcessInstance startProcess(String processId) {

    ProcessInstance startProcess = getSession().startProcess(processId);
    return startProcess;

  }

  public ProcessInstance startProcessInstance(long processInstanceId) {

    ProcessInstance startProcessInstance = getSession().startProcessInstance(processInstanceId);
    return startProcessInstance;

  }

  public Map<String, Object> getProcessInstanceVariables(long processInstanceId) {

    ProcessInstance processInstance = getSession().getProcessInstance(processInstanceId);
    if (processInstance != null) {
      return ((WorkflowProcessInstanceImpl) processInstance).getVariables();
    }
    else {
      throw new IllegalArgumentException("Could not find process instance " + processInstanceId);
    }

  }

  public ProcessInstanceEndLog getProcessInstanceEndLog(long processInstanceId) {

    ProcessInstanceEndLog findProcessInstanceEndLog = new JPAProcessInstanceDbLogExtend(getEnvironment()).findProcessInstanceEndLog(processInstanceId);

    return findProcessInstanceEndLog;

  }

  public List<ProcessInstanceEndLog> findProcessInstanceEndLog(ProcessInstanceEndLogFilter filter) {

    List<ProcessInstanceEndLog> result = new ArrayList<ProcessInstanceEndLog>();
    List<ProcessInstanceEndLog> findProcessInstances = new JPAProcessInstanceDbLogExtend(getEnvironment()).findProcessInstanceEndLog(filter);
    SessionFactory.returnSession();
    for (ProcessInstanceEndLog processInstanceLog : findProcessInstances) {
      result.add(processInstanceLog);
    }

    return result;

  }

  public String abortProcessInstanceByWorkItemHandler(long processInstanceId, Map<String, Object> variables) {
    setProcessInstanceVariables(processInstanceId, variables);
    return abortProcessInstance(processInstanceId);
  }

  public void completeWorkItem(long workItemId, Map<String, Object> results) {
    LOG.info("Completing work item started! " + getSession().getWorkItemManager().getClass());
    getSession().getWorkItemManager().completeWorkItem(workItemId, results);
    LOG.info("Completing work item ended!");
  }

  public List<NodeInstanceLog> getNodeInfo(long processInstanceId) {

    List<NodeInstanceLog> result = new ArrayList<NodeInstanceLog>();
    List<NodeInstanceLog> findNodeInstances = new JPAProcessInstanceDbLogExtend(getEnvironment()).findNodeInstances(processInstanceId);
    for (NodeInstanceLog nodeInstanceLog : findNodeInstances) {
      result.add(nodeInstanceLog);
    }

    return result;

  }

  /**
   * Get class instance for WI handler name. It is loaded from initial configuration (CustomWorkItemHandlers.conf)
   * 
   * @param name
   * @return
   */
  public WorkItemHandler getWorkItemHandlerClass(String name) {

    Map<String, WorkItemHandler> workItemHandlers = ((SessionConfiguration) getSession().getSessionConfiguration()).getWorkItemHandlers();

    return workItemHandlers.get(name);

  }
}
