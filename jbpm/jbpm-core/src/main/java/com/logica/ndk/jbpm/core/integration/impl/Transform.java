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

package com.logica.ndk.jbpm.core.integration.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.drools.definition.process.Process;
import org.drools.runtime.process.ProcessInstance;
import org.jboss.bpm.console.client.model.ProcessDefinitionRef;
import org.jboss.bpm.console.client.model.TaskRef;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.task.I18NText;
import org.jbpm.task.Task;
import org.jbpm.task.query.TaskSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.jbpm.core.integration.api.ProcessInstanceEndLog;
import com.logica.ndk.jbpm.core.integration.api.VariableEndLog;
import com.logica.ndk.tm.process.ParamMap;
import com.logica.ndk.tm.process.ParamMapItem;
import com.logica.ndk.tm.process.ProcessState;

public class Transform {
  private static final Logger LOG = LoggerFactory.getLogger(Transform.class);

  public static ProcessDefinitionRef processDefinition(Process process) {
    long version = 0;
    try {
      version = new Long(process.getVersion());
    }
    catch (NumberFormatException e) {
      // Do nothing, keep version 0
    }
    ProcessDefinitionRef result = new ProcessDefinitionRef(
        process.getId(), process.getName(), version);
    result.setPackageName(process.getPackageName());
    result.setDeploymentId("N/A");
    return result;
  }

  public static ProcessState processState(ProcessInstanceLog processInstanceLog) {
    if (processInstanceLog == null) {
      return null;
    }
    ProcessState processState = new ProcessState();
    processState.setInstanceId(processInstanceLog.getProcessInstanceId());
    processState.setProcessId(processInstanceLog.getProcessId());
    if (processInstanceLog.getStart() == null) {
      processState.setState(ProcessInstance.STATE_PENDING);
    }
    else {
      processState.setState(ProcessInstance.STATE_ACTIVE);
    }
    processState.setStartDate(dateToXMLGregorianCalendar(processInstanceLog.getStart()));
    processState.setEndDate(dateToXMLGregorianCalendar(processInstanceLog.getEnd()));
    return processState;
  }

  public static ProcessState processState(ProcessInstanceEndLog processInstanceEndLog) {
    if (processInstanceEndLog == null) {
      return null;
    }
    ProcessState processState = new ProcessState();
    processState.setInstanceId(processInstanceEndLog.getProcessInstanceId());
    processState.setProcessId(processInstanceEndLog.getProcessId());
    processState.setState(processInstanceEndLog.getState());
    if (processInstanceEndLog.getVariableList() != null && processInstanceEndLog.getVariableList().size() > 0) {
      ParamMap paramMap = new ParamMap();
      for (VariableEndLog variableEndLog : processInstanceEndLog.getVariableList()) {
        ParamMapItem item = new ParamMapItem();
        item.setName(variableEndLog.getName());
        item.setValue(variableEndLog.getValue());
        paramMap.getItems().add(item);
      }
      processState.setParameters(paramMap);
    }

    processState.setStartDate(dateToXMLGregorianCalendar(processInstanceEndLog.getStartDate()));
    processState.setEndDate(dateToXMLGregorianCalendar(processInstanceEndLog.getEndDate()));
    return processState;
  }

  public static ProcessInstanceEndLog processInstanceEndLog(ProcessState processState) {
    ProcessInstanceEndLog processInstanceEndLog = new ProcessInstanceEndLog();
    Date created = new Date();
    processInstanceEndLog.setCreated(created);
    processInstanceEndLog.setProcessId(processState.getProcessId());
    processInstanceEndLog.setProcessInstanceId(processState.getInstanceId());
    processInstanceEndLog.setState(processState.getState());
    if (processState.getParameters() != null && processState.getParameters().getItems() != null) {
      Collection<VariableEndLog> variableList = new ArrayList<VariableEndLog>();
      for (ParamMapItem paramMapItem : processState.getParameters().getItems()) {
        VariableEndLog variableEndLog = new VariableEndLog();
        variableEndLog.setProcessInstanceEndLog(processInstanceEndLog);
        variableEndLog.setCreated(created);
        variableEndLog.setName(paramMapItem.getName());
        variableEndLog.setValue(paramMapItem.getValue());
        variableList.add(variableEndLog);
      }
      processInstanceEndLog.setVariableList(variableList);
    }
    processInstanceEndLog.setStartDate(xmlGregorianCalendarToDate(processState.getStartDate()));
    processInstanceEndLog.setEndDate(xmlGregorianCalendarToDate(processState.getEndDate()));
    return processInstanceEndLog;
  }
  
  public static XMLGregorianCalendar dateToXMLGregorianCalendar(Date date) {
    if (date == null) {
      return null;
    }
    DatatypeFactory df = null;
    try {
      df = DatatypeFactory.newInstance();
      GregorianCalendar gc = new GregorianCalendar();
      gc.setTimeInMillis(date.getTime());
      return df.newXMLGregorianCalendar(gc);
    }
    catch (DatatypeConfigurationException e) {
      LOG.error("Error during convertion dateToXMLGregorianCalendar.", e);
    }
    return null;
  }
  
  public static Date xmlGregorianCalendarToDate(XMLGregorianCalendar xmlGregorianCalendar) {
    if (xmlGregorianCalendar == null) {
      return null;
    }
    return xmlGregorianCalendar.toGregorianCalendar().getTime();
  }

  public static TaskRef task(TaskSummary task) {
    return new TaskRef(
        task.getId(),
        Long.toString(task.getProcessInstanceId()),
        task.getProcessId() == null ? "" : task.getProcessId(),
        task.getName(),
        task.getActualOwner() == null ? null : task.getActualOwner().getId(),
        false,
        false);
  }

  public static TaskRef task(Task task) {
    String name = "";
    for (I18NText text : task.getNames()) {
      if ("en-UK".equals(text.getLanguage())) {
        name = text.getText();
      }
    }
    return new TaskRef(
        task.getId(),
        Long.toString(task.getTaskData().getProcessInstanceId()),
        task.getTaskData().getProcessId() == null ? "" : task.getTaskData().getProcessId(),
        name,
        task.getTaskData().getActualOwner() == null ? null : task.getTaskData().getActualOwner().getId(),
        false,
        false);
  }

}
