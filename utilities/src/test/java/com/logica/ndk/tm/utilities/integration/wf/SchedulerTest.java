package com.logica.ndk.tm.utilities.integration.wf;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.logica.ndk.commons.utils.test.TestUtils;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Activity;
import com.logica.ndk.tm.utilities.integration.wf.task.PackageTask;
import com.logica.ndk.tm.utilities.integration.wf.task.Signature;
import com.logica.ndk.tm.utilities.integration.wf.task.Task;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.jbpm.FreeProcess;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.jbpm.ProcessMap;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;

public class SchedulerTest {
  
  private final WFClient wfClientMock = mock(WFClient.class);
  private final Scheduler scheduler = new Scheduler();
  
  @Before
  public void setUp() throws Exception {
    TestUtils.setField(scheduler, "wfClient", wfClientMock);
  }

  @After
  public void tearDown() throws Exception {
    reset(wfClientMock);
  }
  
  @Test
  public void testSchedule() throws Exception {
   
    
    List<Task> allTasks = new ArrayList<Task>();
    // package task 1
    PackageTask task1 = new PackageTask();
    task1.setId((long)1);
    task1.setActivity(new Activity((long)5, "CDMIMPORT"));
    allTasks.add(task1);
    
    List<Signature> allSignatures = new ArrayList<Signature>();
    // package signature 1
    Signature signature1 = new Signature();
    signature1.setId(new Long(1327415));
    signature1.setSignatureType("NDKSigReserve");
    signature1.setPackageId(new Long(1327397));
    signature1.setCreateDT(new Date());
    signature1.setCreateUserName("SvcTM");
    signature1.setActivityCode("CDMIMPORT");
    signature1.setError(false);
    signature1.setNote("System task reservation");
    allSignatures.add(signature1);    
    Signature signature2 = new Signature();
    signature2.setId(new Long(1327415));
    signature2.setSignatureType("NDKSigDigitFinish");
    signature2.setPackageId(new Long(1327397));
    signature2.setCreateDT(new Date());
    signature2.setCreateUserName("SvcTM");
    signature2.setActivityCode("CDMIMPORT");
    signature2.setError(false);
    signature2.setNote("");
    allSignatures.add(signature2);    
    Signature signature3 = new Signature();
    signature3.setId(new Long(1327415));
    signature3.setSignatureType("NDKSigDigitFinish");
    signature3.setPackageId(new Long(1327397));
    signature3.setCreateDT(new Date());
    signature3.setCreateUserName("SvcTM");
    signature3.setActivityCode("CDMIMPORT");
    signature3.setError(false);
    signature3.setNote("");
    allSignatures.add(signature3);
    
    doReturn(allSignatures).when(wfClientMock).getSignatures(task1.getId());
    
    ProcessMap freeInstances = getProcessMap();
    Map<String, FreeProcess> freeProcessMap = transformFreeProcessListToMap(freeInstances.getProcess());
    List<Task> plan = scheduler.schedule(allTasks, freeProcessMap);
    System.out.println("Planned: " + Arrays.deepToString(plan.toArray()));
    
  }
  
  private static ProcessMap getProcessMap() {
    ProcessMap map = new ProcessMap();
    FreeProcess p1 = new FreeProcess();
    p1.setProcessId("import.vstup-manuscriptorium");
    p1.setCount(10);
    p1.setPriority(1);
    map.getProcess().add(p1);
    FreeProcess p2 = new FreeProcess();
    p2.setProcessId("import.vstup-digitalizace-fix");
    p2.setCount(10);
    p2.setPriority(1);
    map.getProcess().add(p2);
    FreeProcess p3 = new FreeProcess();
    p3.setProcessId("prototype.priprav-data-pro-scantailor");
    p3.setCount(10);
    p3.setPriority(1);
    map.getProcess().add(p3);
    FreeProcess p4 = new FreeProcess();
    p4.setProcessId("prototype.vstup-z-digitalizace");
    p4.setCount(10);
    p4.setPriority(1);
    map.getProcess().add(p4);
    FreeProcess p5 = new FreeProcess();
    p5.setProcessId("prototype.vytvoreni-cdm-digitalizace");
    p5.setCount(10);
    p5.setPriority(1);
    map.getProcess().add(p5);
    FreeProcess p6 = new FreeProcess();
    p6.setProcessId("prototype.import-dat-do-cdm");
    p6.setCount(10);
    p6.setPriority(1);
    map.getProcess().add(p6);
    FreeProcess p7 = new FreeProcess();
    p7.setProcessId("prototype.import-dat-do-cdm-format-migration");
    p7.setCount(10);
    p7.setPriority(1);
    map.getProcess().add(p7);
    return map;
  }
  
  private Map<String, FreeProcess> transformFreeProcessListToMap(List<FreeProcess> freeProcessList) {
    Map<String, FreeProcess> freeProcessMap = new HashMap<String, FreeProcess>();

    for (FreeProcess freeProcess : freeProcessList) {
      freeProcessMap.put(freeProcess.getProcessId(), freeProcess);
    }

    return freeProcessMap;
  }

}
