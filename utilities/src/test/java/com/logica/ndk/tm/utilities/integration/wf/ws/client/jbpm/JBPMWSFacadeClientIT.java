package com.logica.ndk.tm.utilities.integration.wf.ws.client.jbpm;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

public class JBPMWSFacadeClientIT {

  JBPMWSFacadeClient client = new JBPMWSFacadeClient();
  
  @Ignore
  public void testCreateProcessInstance1() throws JBPMBusinessException_Exception, JBPMSystemException_Exception {
    Map<String, String> parameters = new HashMap<String, String>();
    String processId = "xxx";
    Long instanceId = client.createProcessInstance(processId, parameters);
    assertNull(instanceId);
  }

  @Ignore
  public void testCreateProcessInstance2() throws JBPMBusinessException_Exception, JBPMSystemException_Exception {
    Map<String, String> parameters = new HashMap<String, String>();
    String processId = "prototype.ping";
    //parameters.put("barCode", "1000957006");
    Long instanceId = client.createProcessInstance(processId, parameters);
    assertNotNull(instanceId);
  }

  @Ignore
  public void testStartProcessInstance() throws JBPMBusinessException_Exception, JBPMSystemException_Exception {
    Map<String, String> parameters = new HashMap<String, String>();

    // Create process and get isntance ID
    String processId = "prototype.GetAlephDataSynch";
    parameters.put("libraryId", "NKC");
    parameters.put("barCode", "1000957006");
    Long instanceId = client.createProcessInstance(processId, parameters);
    assertNotNull(instanceId);

    // Run it
    client.startProcessInstance(instanceId);
    System.out.println(instanceId);
    assertTrue(true);
  }

}
