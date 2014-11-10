package com.logica.ndk.tm.utilities.integration.wf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.process.ParamMap;
import com.logica.ndk.tm.process.ParamMapItem;
import com.logica.ndk.tm.process.ProcessState;
import com.logica.ndk.tm.utilities.ProcessParams;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Enumerator;
import com.logica.ndk.tm.utilities.integration.wf.exception.BadRequestException;
import com.logica.ndk.tm.utilities.integration.wf.task.LTPWorkPackageTask;
import com.logica.ndk.tm.utilities.integration.wf.task.Task;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;

public class ProcessFinishedHandlerImplLTPIT {

  @Test
  @Ignore // FIXME because of WF initial checks - invalid vlaues
  public void testHandleFinishedProcess() throws TransformerException, BadRequestException, IOException {
    WFClient wfClient = new WFClient();

    // TST constants
    final String  PROCESS_ID          = "TST Process";
    final long    PROCESS_INSTANCE_ID = (long)123;
    final String  TM_USER             = "svctm";

    // Get tested task
    LTPWorkPackageTask task = new LTPWorkPackageTask();
    task.setTest(true);
    task.setProcessManual(true);
    task.setUrl("url://test");
    task.setImportType(new Enumerator((long)7374, "ACTUALIZEFROMALEPH"));
    Task newTask = wfClient.createTask(task, TM_USER, true);
    
    // Reserve task
    String processInstanceId = "123";
    wfClient.reserveSystemTask(newTask.getId(), TM_USER, processInstanceId, "tst");

    // Prepare immediate test data
    String pathId = "123";
    String uuid = "abc";
    ProcessState state = new ProcessState();
    state.setProcessId(PROCESS_ID);
    state.setInstanceId(PROCESS_INSTANCE_ID);
    state.setState(ProcessFinishedHandlerImpl.PROCESS_STATE_OK);
    ParamMap map = new ParamMap();
    // Param 1
    ParamMapItem paramItem1 = new ParamMapItem();
    paramItem1.setName(ProcessParams.PARAM_NAME_TASK_ID);
    paramItem1.setValue(String.valueOf(newTask.getId()));
    map.getItems().add(paramItem1);
    // Param 2
    ParamMapItem paramItem2 = new ParamMapItem();
    paramItem2.setName(ProcessParams.PARAM_NAME_PATH_ID);
    paramItem2.setValue(pathId);
    map.getItems().add(paramItem2);
    // Param 3
    ParamMapItem paramItem3 = new ParamMapItem();
    paramItem3.setName(ProcessParams.PARAM_NAME_UUID);
    paramItem3.setValue(uuid);
    map.getItems().add(paramItem3);
    // Param 4
    ParamMapItem paramItem4 = new ParamMapItem();
    paramItem4.setName(ProcessParams.PARAM_NAME_PROCESS_MANUAL);
    paramItem4.setValue("true");
    map.getItems().add(paramItem4);
    
    state.setParameters(map);
    
    // Run the test
    ProcessFinishedHandler u = new ProcessFinishedHandlerImpl();
    u.handleFinishedProcess(state);
    
    // Check result
    Task checkTask = wfClient.getTask(newTask.getId());
    assertNull(checkTask.getReservedDT());
    assertEquals("WPPREPROC", checkTask.getActivity().getCode());
    assertEquals(pathId, ((LTPWorkPackageTask)checkTask).getPathId());
    assertEquals(uuid, ((LTPWorkPackageTask)checkTask).getUuid());
    assertTrue(((LTPWorkPackageTask)checkTask).isProcessManual());
    
  }

}
