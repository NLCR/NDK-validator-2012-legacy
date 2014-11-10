package com.logica.ndk.tm.utilities.integration.wf.ws.client.jbpm;

import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;

public class JBPMWSFacadeClient {

  private JBPMWSFacade port;

  public JBPMWSFacadeClient() {
    try {
      port = new JBPMWSFacadeImplService(new URL(TmConfig.instance().getString("jbpmws.wsdlLocation")),
          new QName(TmConfig.instance().getString("jbpmws.qnameUri"), TmConfig.instance().getString("jbpmws.qnameLocalService"))).
          getJBPMWSFacadeImplPort();
    }
    catch (Exception e) {
      throw new SystemException("Cannot initialize conenction to jbpmws", ErrorCodes.JBPM_CONNECTION_ERROR);
    }

    String userName = TmConfig.instance().getString("jbpmws.username");

    if (userName != null && !userName.isEmpty()) {
      BindingProvider bp = (BindingProvider) port;
      Map<String, Object> rc = bp.getRequestContext();
      rc.put(BindingProvider.USERNAME_PROPERTY, userName);
      rc.put(BindingProvider.PASSWORD_PROPERTY, TmConfig.instance().getString("jbpmws.password"));
    }

  }

  public Long createProcessInstance(String processId, Map<String, String> parameters) throws JBPMBusinessException_Exception, JBPMSystemException_Exception {
    return port.createProcessInstance(processId, mapToHashMmap(parameters));
  }

  public void startProcessInstance(long processInstanceId) throws JBPMBusinessException_Exception, JBPMSystemException_Exception {
    port.startProcessInstance(processInstanceId);
  }

  public StateResponse.Return getProcessState(long processInstanceId) throws JBPMBusinessException_Exception, JBPMSystemException_Exception {
    return port.state(processInstanceId);
  }

  private ParamMap mapToHashMmap(Map<String, String> map) {
    ParamMap paramMap = new ParamMap();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      ParamMapItem item = new ParamMapItem();
      item.setName(entry.getKey());
      item.setValue(entry.getValue());
      paramMap.getItems().add(item);
    }
    return paramMap;

  }

  public void signalEvent(String type, String eventData) throws JBPMBusinessException_Exception, JBPMSystemException_Exception {
    port.signalEvent(type, eventData);
  }

  public void signalEventForInstance(long processId, String type, String eventData) throws JBPMBusinessException_Exception, JBPMSystemException_Exception {
    port.signalEventForInstance(processId, type, eventData);
  }

  public List<Long> getActiveInstances(String processId) throws JBPMBusinessException_Exception, JBPMSystemException_Exception{
    return port.activeInstances(processId);
  }

  public List<Long> endInstancesExceedTimeout(String initiator) throws JBPMBusinessException_Exception, JBPMSystemException_Exception {
    return port.endInstancesExceedTimeout(initiator);
  }
  
 public ProcessMap getFreeInstances(Boolean includeFull) throws JBPMBusinessException_Exception, JBPMSystemException_Exception{
   return port.getFreeProcessInstances(includeFull);
 }

}
