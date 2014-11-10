package com.logica.ndk.tm.process.jmx;

import com.logica.ndk.jbpm.core.SessionFactory;
import com.logica.ndk.jbpm.core.integration.ManagementFactory;
import com.logica.ndk.jbpm.core.integration.ProcessManagement;
import com.logica.ndk.jbpm.core.integration.impl.MaintainanceService;
import com.logica.ndk.tm.process.ParamMap;
import com.logica.ndk.tm.process.ParamMapItem;
import com.logica.ndk.tm.process.ProcessState;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;
import java.util.Properties;

/**
 * Implementation of JMX MBean.
 * 
 * @author Rudolf Daco
 */
// TODO [rda] - do not return stack trace but throw correct JMX exception
public class JBPMJmx implements JBPMJmxMBean {
  private static final Logger LOG = LoggerFactory.getLogger(JBPMJmxMBean.class);
  private static final String DEFAULT_ABORT_INITIATOR = "jbpm-jmx";
  private ProcessManagement processManagement;
  private MaintainanceService maintainanceService;

  private ProcessManagement getProcessManagement() throws IOException {
    if (null == this.processManagement) {
      loadContextByServiceCall();
      this.processManagement = new ManagementFactory().createProcessManagement();
    }
    return this.processManagement;
  }

  private MaintainanceService getMaintainanceService() throws IOException {
    if (null == this.maintainanceService) {
      loadContextByServiceCall();
      this.maintainanceService = new ManagementFactory().createMaintainanceService();
    }
    return this.maintainanceService;
  }

  /**
   * WA pre nacitanie Session (JTA, ...). Nie je mozne ziskat pristup k JNDI objektom vytvorenymi pocas inicializacie
   * JTA v JMX MBean. JTA sa inicializuje v jbpm-core. JMX MBean je pusteny v inom threade a teda
   * nema pristup k tymto JNDI objektom. Ma pristup k JBoss JNDI objektom, ale jbpm-core pouziva JTA (potrebujeme
   * pristup k java:comp/UserTransaction nestaci k java:jboss/UserTransaction). Z toho
   * dovodu je potrebne zabezpecit aby Session bola uz inicializovana ak sa ide volat nejaka sluzba cez JMX. Preto pri
   * prvom pouziti JMX sluzby pre istotu zavolame niektoru sluzby jbpm-ws aby sa
   * inicializovala Session vramci jbpm-ws ak este nie je inicializovana. JBoss 7.1.x ma novy pristup k JMX a je mozne
   * ze byt to tam fungovalo bez tohto WA, ale TM je urceny na beh na JBoss 7.0.x
   * 
   * @throws IOException
   */
  // TODO [rda] - tento WA v JBoss 7.0.x nevieme vyriesit inak jedine externalizovanim inicializacnej sluzby do EJB aby sa nevolala nejaka sluzba ale EJB.
  // ak by sme mali EJB uz by bolo vhodne vsetky JMX sluzby upravit tak aby volali EJB sluzby. Pripadne pouzit JBoss JNDI pre userTransaction ktory je k
  // dispozicii, ale je potrebne prepisat jadro od jbpm
  private void loadContextByServiceCall() throws IOException {
    Properties jbpmProperties = new Properties();
    InputStream resourceAsStream = null;
    try {
      resourceAsStream = SessionFactory.class.getResourceAsStream("/jbpm.console.properties");
      jbpmProperties.load(resourceAsStream);
    }
    catch (IOException e) {
      LOG.error("Could not load jbpm.console.properties", e);
      throw new RuntimeException("Could not load jbpm.console.properties", e);
    }
    finally {
      IOUtils.closeQuietly(resourceAsStream);
    }

    URL url = new URL(jbpmProperties.getProperty("jmx.init.service.url"));
    LOG.debug("Initializing session by calling service at:" + url);
    InputStream is = null;
    try {
      is = url.openStream();
    }
    finally {
      if (is != null) {
        is.close();
      }
    }
  }

  @Override
  public String createProcessInstance(String processId) {
    LOG.info("JBPMJmx.createProcessInstance: processId:<" + processId + ">");
    try {
      return new Long(getProcessManagement().createProcessInstance(processId, null).getId()).toString();
    }
    catch (Exception e) {
      return getStackTrace(e);
    }
  }

  @Override
  public String startProcessInstance(long processInstanceId) {
    LOG.info("JBPMJmx.startProcessInstance: processInstanceId:<" + processInstanceId + ">");
    try {
      return new Long(getProcessManagement().startProcessInstance(processInstanceId).getId()).toString();
    }
    catch (Exception e) {
      return getStackTrace(e);
    }
  }

  @Override
  public String startProcess(String processId) {
    LOG.info("JBPMJmx.startProcess: processId:<" + processId + ">");
    try {
      return new Long(getProcessManagement().startProcess(processId, null).getId()).toString();
    }
    catch (Exception e) {
      return getStackTrace(e);
    }
  }

  @Override
  public String endInstance(long instanceId, String initiator) {
    LOG.info("JBPMJmx.endInstance: instanceId:<" + instanceId + ">" + " initiator:<" + initiator + ">");
    try {
      if (initiator == null || initiator.length() == 0) {
        initiator = DEFAULT_ABORT_INITIATOR;
      }
      getProcessManagement().endInstance(instanceId, initiator);
      return "Instance " + instanceId + " finished with result EXITED";
    }
    catch (Exception e) {
      return getStackTrace(e);
    }
  }

  @Override
  public String activeInstances(String processId) {
    LOG.info("JBPMJmx.activeInstances: processId:<" + processId + ">");
    try {
      List<Long> activeInstances = getProcessManagement().getActiveInstances(processId);
      if (activeInstances != null && activeInstances.size() > 0) {
        StringBuffer buffer = new StringBuffer();
        for (Long instanceId : activeInstances) {
          buffer.append(instanceId + ",");
        }
        return buffer.toString().substring(0, buffer.length() - 1);
      }
      return "";
    }
    catch (Exception e) {
      return getStackTrace(e);
    }
  }

  @Override
  public String signalEventForInstance(long instanceId, String type, String eventData) {
    LOG.info("JBPMJmx.signalEventForInstance: instanceId:<" + instanceId + ">" + " type:<" + type + ">" + " eventData:<" + eventData + ">");
    try {
      getProcessManagement().signalEvent(new Long(instanceId).toString(), type, eventData);
      return "Signal send OK.";
    }
    catch (Exception e) {
      return getStackTrace(e);
    }
  }

  @Override
  public String signalEvent(String type, String eventData) {
    try {
      LOG.info("JBPMJmx.signalEvent: type:<" + type + ">" + " eventData:<" + eventData + ">");
      getProcessManagement().signalEvent(type, eventData);
      return "Signal send OK.";
    }
    catch (Exception e) {
      return getStackTrace(e);
    }
  }

  @Override
  public String state(long instanceId) {
    LOG.info("JBPMJmx.state: instanceId:<" + instanceId + ">");
    try {
      ProcessState state = getProcessManagement().state(instanceId);
      String result = "instanceId:" + state.getInstanceId() + " processId:" + state.getProcessId() + " state:" + state.getState();
      result += " parameters:";
      ParamMap paramMap = state.getParameters();
      if (paramMap != null) {
        List<ParamMapItem> items = paramMap.getItems();
        for (ParamMapItem paramMapItem : items) {
          result += "<" + paramMapItem.getName() + "," + paramMapItem.getValue() + ">";
        }
      }
      return result;
    }
    catch (Exception e) {
      return getStackTrace(e);
    }
  }

  @Override
  public String resumeProcesses() {
    LOG.info("JBPMJmx.resumeProcesses");
    try {
      getMaintainanceService().resumeProcesses();
      return "Resume OK.";
    }
    catch (Exception e) {
      return getStackTrace(e);
    }
  }

  @Override
  public String resumeProcess(long instanceId) {
    LOG.info("JBPMJmx.resumeProcess: instanceId:<" + instanceId + ">");
    try {
      getMaintainanceService().resumeProcess(instanceId);
      return "Resume OK.";
    }
    catch (Exception e) {
      return getStackTrace(e);
    }
  }

  @Override
  public String activeInstancesExceedTimeout() {
    LOG.info("JBPMJmx.activeInstancesExceedTimeout");
    try {
      List<Long> list = getProcessManagement().getActiveInstancesExceedTimeout();
      if (list != null && list.size() > 0) {
        StringBuffer buffer = new StringBuffer();
        for (Long instanceId : list) {
          buffer.append(instanceId + ",");
        }
        return buffer.toString().substring(0, buffer.length() - 1);
      }
      return "";
    }
    catch (Exception e) {
      return getStackTrace(e);
    }
  }

  @Override
  public String endInstancesExceedTimeout(String initiator) {
    LOG.info("JBPMJmx.endInstancesExceedTimeout: initiator:<" + initiator + ">");
    try {
      List<Long> list = getProcessManagement().endInstancesExceedTimeout(initiator);
      if (list != null && list.size() > 0) {
        StringBuffer buffer = new StringBuffer();
        for (Long instanceId : list) {
          buffer.append(instanceId + ",");
        }
        return buffer.toString().substring(0, buffer.length() - 1);
      }
      return "";
    }
    catch (Exception e) {
      return getStackTrace(e);
    }
  }

  private String getStackTrace(Exception e) {
    StringWriter stringWriter = null;
    PrintWriter printWriter = null;
    stringWriter = new StringWriter();
    printWriter = new PrintWriter(stringWriter);
    e.printStackTrace(printWriter);
    String result = stringWriter.toString();
    if (stringWriter != null) {
      try {
        stringWriter.close();
      }
      catch (IOException e1) {
      }
    }
    if (printWriter != null) {
      printWriter.close();
    }
    return result;
  }
}
