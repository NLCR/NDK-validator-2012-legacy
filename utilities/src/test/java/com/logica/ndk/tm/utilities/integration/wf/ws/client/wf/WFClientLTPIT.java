package com.logica.ndk.tm.utilities.integration.wf.ws.client.wf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.integration.wf.TaskFinder;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Codebook;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.CodebookFinder;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Enumerator;
import com.logica.ndk.tm.utilities.integration.wf.exception.BadRequestException;
import com.logica.ndk.tm.utilities.integration.wf.finishedTask.FinishedLTPWorkPackageTask;
import com.logica.ndk.tm.utilities.integration.wf.finishedTask.FinishedPackageTask;
import com.logica.ndk.tm.utilities.integration.wf.ping.PingResponse;
import com.logica.ndk.tm.utilities.integration.wf.task.LTPWorkPackageTask;
import com.logica.ndk.tm.utilities.integration.wf.task.PackageTask;
import com.logica.ndk.tm.utilities.integration.wf.task.Task;
import com.logica.ndk.tm.utilities.integration.wf.task.TaskHeader;

public class WFClientLTPIT {
  WFClient client = new WFClient();
  
  private static final String TM_USER = "svctm";
  
  @Ignore
  public void testGetWaitingTasks() throws TransformerException, IOException, BadRequestException {
    List<TaskHeader> tasks = client.getTasks(new TaskFinder());
    assertNotNull(tasks);
    assertTrue(tasks.size() > 0);
  }

  @Ignore
  public void testGetTask() throws TransformerException, IOException, BadRequestException {

    TaskFinder finder = new TaskFinder();
    finder.setPackageType(WFClient.PACKAGE_TYPE_LTP_WORK_PACKAGE);
    List<TaskHeader> tasks = client.getTasks(finder);
    assertTrue("No suitable task for test", tasks.size() > 0);
    Task task = client.getTask(tasks.get(0));
    assertNotNull(task);
    assertNotNull(task.getActivity());
  }

  @Ignore
  public void testGetTaskById() throws TransformerException, IOException, BadRequestException {

    TaskFinder finder = new TaskFinder();
    finder.setPackageType(WFClient.PACKAGE_TYPE_LTP_WORK_PACKAGE);
    List<TaskHeader> tasks = client.getTasks(finder);
    assertTrue("No suitable task for test", tasks.size() > 0);
    Task task = client.getTask(tasks.get(0).getId());
    assertNotNull(task);
    assertNotNull(task.getActivity());
    assertTrue(task instanceof PackageTask);
  }

  @Ignore
  public void testReserveTask() throws TransformerException, IOException, BadRequestException {

    TaskFinder finder = new TaskFinder();
    finder.setOnlyWaiting(true);
    finder.setError(false);
    List<TaskHeader> tasks = client.getTasks(finder);
    String processInstanceId = "123";
    assertTrue("No task sutiable task for test", tasks.size() > 0);
    TaskHeader testedTask = tasks.get(0);
    client.reserveSystemTask(testedTask.getId(), TM_USER, processInstanceId, "tst");
    
    Task task = client.getTask(testedTask);
    assertNotNull("Task reservation info is null", task.getReservedDT());
    assertEquals("Incorrect reservation", processInstanceId, task.getReservedInternalId());
  }

  @Ignore
  public void testSignalFinishedTask() throws JsonParseException, JsonMappingException, IOException, BadRequestException, TransformerException {

    // Create new task
    LTPWorkPackageTask task = new LTPWorkPackageTask();
    task.setTest(true);
    task.setProcessManual(true);
    task.setUrl("url://test");
    task.setImportType(new Enumerator((long)7374, "ACTUALIZEFROMALEPH"));
    Task newTask = client.createTask(task, TM_USER, true);

    // Reserve task
    String processInstanceId = "123";
    client.reserveSystemTask(newTask.getId(), TM_USER, processInstanceId, "tst");
    
    // Finish task
    String pathId = "123";
    String uuid = "abc";
    TaskHeader testedTask = new TaskHeader();
    testedTask.setId(newTask.getId());
    testedTask.setPackageType(WFClient.PACKAGE_TYPE_LTP_WORK_PACKAGE); // FIXME hack because of bug in WF API - shoud use value from newTask
    FinishedLTPWorkPackageTask finishedTask = new FinishedLTPWorkPackageTask();
    finishedTask.setId(newTask.getId());
    finishedTask.setUser(TM_USER);
    finishedTask.setProcessManual(true);
    finishedTask.setPathId(pathId);
    finishedTask.setUuid(uuid);
    client.signalFinishedTask(finishedTask, "LWFSigFinishInput");
    
    // Check result
    Task checkTask = client.getTask(testedTask);
    assertNull("Task reservation date time is not null", checkTask.getReservedDT());
    assertEquals(true, ((LTPWorkPackageTask)checkTask).isProcessManual());
    assertEquals(pathId, ((LTPWorkPackageTask)checkTask).getPathId());
    assertEquals(uuid, ((LTPWorkPackageTask)checkTask).getUuid());
  }
  
  @Ignore
  public void testPing() {
    PingResponse response = client.ping();
    assertTrue(response != null);
    assertTrue(response.getTime()!= null);
  }
  
  @Ignore
  public void testCreateTask() throws JsonParseException, JsonMappingException, IOException, BadRequestException {
    LTPWorkPackageTask task = new LTPWorkPackageTask();
    task.setTest(true);
    task.setProcessManual(true);
    task.setUrl("url://test");
    task.setImportType(new Enumerator((long)7374, "ACTUALIZEFROMALEPH"));
    Task newTask = client.createTask(task, TM_USER, true);
    assertNotNull(newTask);
    assertTrue(task instanceof LTPWorkPackageTask);
    assertNotNull(newTask.getId());
    assertEquals("url://test", ((LTPWorkPackageTask)newTask).getUrl());
    assertTrue(((LTPWorkPackageTask)newTask).isTest());
  }

  @Ignore
  public void testError() {
    try {
      client.getTask((long)-1);
      fail("BadRequestException expected");
    }
    catch (BadRequestException e) {
      assertEquals("404 ", e.getMessage());
      e.printStackTrace();
    }
    catch (Exception e) {
      fail("Unexcpected excetipn occured: " + e.getMessage());
    }
  }

  @Ignore
  public void testGetCodebooks() throws TransformerException, IOException, BadRequestException {

    CodebookFinder finder = new CodebookFinder();
    finder.setCbType("NDKCBActivity");
    List<Codebook> codebooks = client.getCodebooks(finder);
    assertTrue("No suitable task for test", codebooks.size() > 0);
    assertEquals("WPFINISHNOOK", codebooks.get(0).getCode());
  }
  
}
