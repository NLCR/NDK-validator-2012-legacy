package com.logica.ndk.tm.utilities.integration.wf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.process.ParamMap;
import com.logica.ndk.tm.process.ParamMapItem;
import com.logica.ndk.tm.process.ProcessState;
import com.logica.ndk.tm.utilities.ProcessParams;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.DocumentLocality;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Enumerator;
import com.logica.ndk.tm.utilities.integration.wf.exception.BadRequestException;
import com.logica.ndk.tm.utilities.integration.wf.task.PackageTask;
import com.logica.ndk.tm.utilities.integration.wf.task.TaskHeader;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;

public class ProcessFinishedHandlerIT {

  @Test
  @Ignore
  // FIXME because of WF initial checks - invalid vlaues
  public void testHandleFinishedProcess() throws TransformerException, BadRequestException, IOException {
    WFClient wfClient = new WFClient();

    // TST constants
    final String PROCESS_ID = "TST Process";
    final long PROCESS_INSTANCE_ID = (long) 123;
    final String TM_USER = "svctm";

    // Get tested task
    Enumerator locality = new Enumerator();
    locality.setCode("NKCR");
    PackageTask task = new PackageTask();
    task.setBarCode("1000748306");
    task.setDocumentLocality(new DocumentLocality("NKCR", "NKCR", "NKCR"));
    task.setLocality(locality);
    task = (PackageTask) wfClient.createTask(task, TM_USER, true);
    System.out.println(task.getId());
    assertNotNull(task.getReservedDT());
    assertEquals("PREPARE", task.getActivity().getCode());

    // Prepare immediate test data
    ProcessState state = new ProcessState();
    state.setProcessId(PROCESS_ID);
    state.setInstanceId(PROCESS_INSTANCE_ID);
    state.setState(ProcessFinishedHandlerImpl.PROCESS_STATE_OK);
    ParamMapItem paramItem = new ParamMapItem();
    paramItem.setName(ProcessParams.PARAM_NAME_TASK_ID);
    paramItem.setValue(String.valueOf(task.getId()));
    ParamMap map = new ParamMap();
    map.getItems().add(paramItem);
    state.setParameters(map);

    // Run the test
    ProcessFinishedHandler u = new ProcessFinishedHandlerImpl();
    u.handleFinishedProcess(state);

    // Check result
    task = (PackageTask) wfClient.getTask(new TaskHeader(task));
    assertNull(task.getReservedDT());
    assertEquals("CREATEDIR", task.getActivity().getCode());

  }

}
