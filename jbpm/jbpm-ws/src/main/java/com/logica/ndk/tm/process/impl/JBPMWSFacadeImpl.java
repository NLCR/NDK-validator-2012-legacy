package com.logica.ndk.tm.process.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebParam;
import javax.jws.WebService;

import org.drools.runtime.process.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.commons.shutdown.ShutdownException;
import com.logica.ndk.jbpm.config.ConfigLoader;
import com.logica.ndk.jbpm.config.LoadRuntimeConfigurationException;
import com.logica.ndk.jbpm.config.ProcessConfig;
import com.logica.ndk.jbpm.config.ProcessRuntimeConfig;
import com.logica.ndk.jbpm.core.integration.ManagementFactory;
import com.logica.ndk.jbpm.core.integration.ProcessManagement;
import com.logica.ndk.jbpm.core.integration.impl.MaintainanceService;
import com.logica.ndk.tm.jbpm.errortrashold.ErrorTrasholdThread;
import com.logica.ndk.tm.process.FreeProcess;
import com.logica.ndk.tm.process.JBPMBusinessException;
import com.logica.ndk.tm.process.JBPMSystemException;
import com.logica.ndk.tm.process.JBPMWSFacade;
import com.logica.ndk.tm.process.ParamMap;
import com.logica.ndk.tm.process.ParamMapItem;
import com.logica.ndk.tm.process.ProcessInstanceLimitExceededException;
import com.logica.ndk.tm.process.ProcessMap;
import com.logica.ndk.tm.process.ProcessState;
import com.logica.ndk.tm.process.outage.OutageManager;

@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public class JBPMWSFacadeImpl implements JBPMWSFacade {
  private static final Logger LOG = LoggerFactory.getLogger(JBPMWSFacadeImpl.class);
  private static final String DEFAULT_ABORT_INITIATOR = "jbpm-ws";
  private ProcessManagement processManagement;
  private MaintainanceService maintainanceService;

  private ProcessManagement getProcessManagement() {
    if (null == this.processManagement) {
      ManagementFactory factory = new ManagementFactory();
      this.processManagement = factory.createProcessManagement();
      LOG.debug("Using ManagementFactory impl:" + factory.getClass().getName());
    }
    return this.processManagement;
  }

  private MaintainanceService getMaintainanceService() {
    if (null == this.maintainanceService) {
      this.maintainanceService = new ManagementFactory().createMaintainanceService();
    }
    return this.maintainanceService;
  }

  @Override
  public Long createProcessInstance(@WebParam(name = "processId") String processId, @WebParam(name = "parameters") ParamMap parameters) throws JBPMSystemException, JBPMBusinessException {
    LOG.info("JBPMWSFacadeImpl.createProcessInstance: processId:<" + processId + ">, parameters:<" + parameters + ">");
    try {
      Map<String, Object> objectMap = new HashMap<String, Object>();
      if (parameters != null) {
        for (ParamMapItem item : parameters.getItems()) {
          objectMap.put(item.getName(), item.getValue());
        }
      }
      return getProcessManagement().createProcessInstance(processId, objectMap).getId();
    }
    catch (ShutdownException e) {
      LOG.warn("Shutdown in progress.", e);
      throw new JBPMBusinessException(e);
    }
    catch (ProcessInstanceLimitExceededException e) {
      throw new JBPMBusinessException(e);
    }
    catch (Exception e) {
      LOG.error("Error at calling createProcessInstance: ", e);
      throw new JBPMSystemException("Error at calling createProcessInstance: " + e.getMessage(), e);
    }
  }

  @Override
  public Long startProcessInstance(@WebParam(name = "processInstanceId") long processInstanceId) throws JBPMSystemException, JBPMBusinessException {
    LOG.info("JBPMWSFacadeImpl.startProcessInstance: processInstanceId=<" + processInstanceId + ">");
    ProcessInstance instance = null;
    try {
      instance = getProcessManagement().startProcessInstance(processInstanceId);
      return instance.getId();
    }
    catch (ShutdownException e) {
      LOG.warn("Shutdown in progress.", e);
      throw new JBPMBusinessException(e);
    }
    catch (Exception e) {
      LOG.error("Error at calling startProcessInstance: ", e);
      throw new JBPMSystemException("Error at calling startProcessInstance: " + e.getMessage(), e);
    }
  }

  @Override
  public Long startProcess(@WebParam(name = "processId") String processId, @WebParam(name = "parameters") ParamMap parameters) throws JBPMSystemException, JBPMBusinessException {
    LOG.info("JBPMWSFacadeImpl.startProcess: processId=<" + processId + ">, parameters=<" + parameters + ">");
    ProcessInstance instance = null;
    try {
      Map<String, Object> objectMap = new HashMap<String, Object>();
      if (parameters != null) {
        for (ParamMapItem item : parameters.getItems()) {
          objectMap.put(item.getName(), item.getValue());
        }
      }
      instance = getProcessManagement().startProcess(processId, objectMap);
      return instance.getId();
    }
    catch (ShutdownException e) {
      LOG.warn("Shutdown in progress.", e);
      throw new JBPMBusinessException(e);
    }
    catch (Exception e) {
      LOG.error("Error at calling startProcess: ", e);
      throw new JBPMSystemException("Error at calling startProcess: " + e.getMessage(), e);
    }
  }

  @Override
  public String endInstance(@WebParam(name = "instanceId") long instanceId, @WebParam(name = "initiator") String initiator) throws JBPMSystemException, JBPMBusinessException {
    LOG.info("JBPMWSFacadeImpl.endInstance: instanceId=<" + instanceId + ">");
    try {
      if (initiator == null || initiator.length() == 0) {
        initiator = DEFAULT_ABORT_INITIATOR;
      }
      getProcessManagement().endInstance(instanceId, initiator);
      return "Instance " + instanceId + " finished with result EXITED";
    }
    catch (ShutdownException e) {
      LOG.warn("Shutdown in progress.", e);
      throw new JBPMBusinessException(e);
    }
    catch (Exception e) {
      LOG.error("Error at calling endInstance: ", e);
      throw new JBPMSystemException("Error at calling endInstance: " + e.getMessage(), e);
    }
  }

  @Override
  public List<Long> activeInstances(@WebParam(name = "processId") String processId) throws JBPMSystemException, JBPMBusinessException {
    try {
      return getProcessManagement().getActiveInstances(processId);
    }
    catch (ShutdownException e) {
      LOG.warn("Shutdown in progress.", e);
      throw new JBPMBusinessException(e);
    }
    catch (Exception e) {
      LOG.error("Error at calling activeInstances: ", e);
      throw new JBPMSystemException("Error at calling activeInstances: " + e.getMessage(), e);
    }
  }

  @Override
  public String signalEventForInstance(@WebParam(name = "instanceId") long instanceId, @WebParam(name = "type") String type, @WebParam(name = "eventData") String eventData) throws JBPMSystemException, JBPMBusinessException {
    LOG.info("JBPMWSFacadeImpl.signalEvent: instanceId:<" + instanceId + ">, type:<" + type + "> eventData:<" + eventData + ">");
    try {
      getProcessManagement().signalEvent(String.valueOf(instanceId), type, eventData);
      return "Signal send OK.";
    }
    catch (ShutdownException e) {
      LOG.warn("Shutdown in progress.", e);
      throw new JBPMBusinessException(e);
    }
    catch (Exception e) {
      LOG.error("Error at calling signalEventForInstance: ", e);
      throw new JBPMSystemException("Error at calling signalEventForInstance: " + e.getMessage(), e);
    }
  }

  @Override
  public String signalEvent(@WebParam(name = "type") String type, @WebParam(name = "eventData") String eventData) throws JBPMSystemException, JBPMBusinessException {
    LOG.info("JBPMWSFacadeImpl.signalEvent: type:<" + type + ">, eventData:<" + eventData + ">");
    try {
      getProcessManagement().signalEvent(type, eventData);
      return "Signal send OK.";
    }
    catch (ShutdownException e) {
      LOG.warn("Shutdown in progress.", e);
      throw new JBPMBusinessException(e);
    }
    catch (Exception e) {
      LOG.error("Error at calling signalEventForInstance: ", e);
      throw new JBPMSystemException("Error at calling signalEventForInstance: " + e.getMessage(), e);
    }
  }

  @Override
  public ProcessState state(@WebParam(name = "instanceId") long instanceId) throws JBPMSystemException, JBPMBusinessException {
    LOG.info("JBPMWSFacadeImpl.state: instanceId:<" + instanceId + ">");
    try {
      return getProcessManagement().state(instanceId);
    }
    catch (ShutdownException e) {
      LOG.warn("Shutdown in progress.", e);
      throw new JBPMBusinessException(e);
    }
    catch (Exception e) {
      LOG.error("Error at calling state: ", e);
      throw new JBPMSystemException("Error at calling state: " + e.getMessage(), e);
    }
  }

  @Override
  public String resumeProcesses() throws JBPMSystemException, JBPMBusinessException {
    LOG.info("JBPMWSFacadeImpl.resumeProcesses.");
    try {
      getMaintainanceService().resumeProcesses();
    }
    catch (Exception e) {
      LOG.error("Error at calling resumeProcesses: ", e);
      throw new JBPMSystemException("Error at calling resumeProcesses: " + e.getMessage(), e);
    }
    return "OK";
  }

  @Override
  public String resumeProcess(long instanceId) throws JBPMSystemException, JBPMBusinessException {
    LOG.info("JBPMWSFacadeImpl.resumeProcesses.");
    try {
      getMaintainanceService().resumeProcess(instanceId);
    }
    catch (Exception e) {
      LOG.error("Error at calling resumeProcesses: ", e);
      throw new JBPMSystemException("Error at calling resumeProcesses: " + e.getMessage(), e);
    }
    return "OK";
  }

  @Override
  public List<Long> activeInstancesExceedTimeout() throws JBPMSystemException, JBPMBusinessException {
    LOG.info("JBPMWSFacadeImpl.activeInstancesExceedTimeout.");
    try {
      return getProcessManagement().getActiveInstancesExceedTimeout();
    }
    catch (ShutdownException e) {
      LOG.warn("Shutdown in progress.", e);
      throw new JBPMBusinessException(e);
    }
    catch (Exception e) {
      LOG.error("Error at calling activeInstancesExceedTimeout: ", e);
      throw new JBPMSystemException("Error at calling activeInstancesExceedTimeout: " + e.getMessage(), e);
    }
  }

  @Override
  public List<Long> endInstancesExceedTimeout(@WebParam(name = "initiator") String initiator) throws JBPMSystemException, JBPMBusinessException {
    LOG.info("JBPMWSFacadeImpl.endInstancesExceedTimeout");
    try {
      if (initiator == null || initiator.length() == 0) {
        initiator = DEFAULT_ABORT_INITIATOR;
      }
      return getProcessManagement().endInstancesExceedTimeout(initiator);
    }
    catch (ShutdownException e) {
      LOG.warn("Shutdown in progress.", e);
      throw new JBPMBusinessException(e);
    }
    catch (Exception e) {
      LOG.error("Error at calling endInstance: ", e);
      throw new JBPMSystemException("Error at calling endInstance: " + e.getMessage(), e);
    }
  }

  @Override
  public ProcessMap getFreeProcessInstances(@WebParam(name = "includeFull") Boolean includeFull) throws JBPMSystemException, JBPMBusinessException {
    LOG.info("JBPMWSFacadeImpl.getFreeProcessInstances");

    ProcessRuntimeConfig loadedConfig;
    try {
      loadedConfig = ConfigLoader.loadConfig();
    }
    catch (LoadRuntimeConfigurationException e) {
      LOG.error("Error while loading config file.", e);
      throw new JBPMSystemException("Error while loading config file.", e);
    }

    List<ProcessConfig> process = loadedConfig.getProcess();
    OutageManager outageManager = new OutageManager();
    ProcessMap result = new ProcessMap();

    for (ProcessConfig processConf : process) {
      
      List<Long> activeInstances = activeInstances(processConf.getProcessId());
      int numberOfFreeSlots = processConf.getMaxInstances() - activeInstances.size();
      
      if ((processConf.getErrorStop() || processConf.getStop())) {
        LOG.info(String.format("Process %s is stoped by stop flag: %b or error stop flag: %b", processConf.getProcessId(), processConf.getStop(), processConf.getErrorStop()));
        numberOfFreeSlots = 0;
      }
      if (outageManager.isOutage(processConf.getProcessId())) {
        LOG.info(String.format("Outage for process %s is set!", processConf.getProcessId()));
        numberOfFreeSlots = 0;
      }
      
      if (numberOfFreeSlots > 0 || includeFull) {
        LOG.info(String.format("Free %d process of type %s", numberOfFreeSlots, processConf.getProcessId()));
        FreeProcess freeProc = new FreeProcess();
        freeProc.setCount(numberOfFreeSlots);
        freeProc.setProcessId(processConf.getProcessId());
        freeProc.setPriority(processConf.getPriority());
        result.getProcess().add(freeProc);
      }
      
    }

    return result;
  }

}
