package com.logica.ndk.tm.utilities.integration.wf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.integration.wf.enumerator.Codebook;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Enumerator;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Scanner;
import com.logica.ndk.tm.utilities.integration.wf.exception.BadRequestException;
import com.logica.ndk.tm.utilities.integration.wf.exception.WFConnectionUnavailableException;
import com.logica.ndk.tm.utilities.integration.wf.finishedTask.FinishedLTPWorkPackageTask;
import com.logica.ndk.tm.utilities.integration.wf.finishedTask.FinishedPackageTask;
import com.logica.ndk.tm.utilities.integration.wf.task.LTPWorkPackageTask;
import com.logica.ndk.tm.utilities.integration.wf.task.PackageTask;
import com.logica.ndk.tm.utilities.integration.wf.task.Scan;
import com.logica.ndk.tm.utilities.integration.wf.task.Task;
import com.logica.ndk.tm.utilities.integration.wf.task.TaskHeader;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;

public class TaskHandlerImplLTPIT {
  TaskHandler u = new TaskHandlerImpl();
  WFClient client = new WFClient();

  private static final String TM_USER = "SvcTM";
  private static final String SCAN_USER = "ndki-manager";
  
//  @Test
//  @Ignore
//  public void testHandleWaitingTasks() throws TransformerException, IOException, WFConnectionUnavailableException, BadRequestException {
//    // FIXME majdaf - prepare tasks for test first;
//    u.handleWaitingTasks();
//  }
//  
//  @Test
//  public void testPing() {
//    boolean result = u.ping();
//    assertTrue(result);
//  }
//  
//  @Test
//  public void testReserveTask() throws JsonParseException, JsonMappingException, IOException, BadRequestException, TransformerException, InterruptedException {
//    WFClient client = new WFClient();
//    
//    // Create new task (PREPARE)
//    LTPWorkPackageTask task = new LTPWorkPackageTask();
//    task.setTest(true);
//    task.setProcessManual(true);
//    task.setUrl("url://test");
//    task.setImportType(new Enumerator((long)7374, "ACTUALIZEFROMALEPH"));
//    Task newTask = client.createTask(task, TM_USER, true);
//
//    // Reserve task for scan
//    String workerId = String.valueOf(new Date().getTime());
//    u.reserveTask(newTask.getId(), TM_USER, workerId, "");
//    
//    Task testedTask = client.getTask(newTask.getId());
//    assertEquals(newTask.getId(), testedTask.getId());
//    assertEquals(TM_USER, testedTask.getReservedBy().getName());
//  }
//
//  @Test
//  public void testReleaseReservedTask() throws JsonParseException, JsonMappingException, IOException, BadRequestException, TransformerException {
//    // Create new task (PREPARE)
//    LTPWorkPackageTask task = new LTPWorkPackageTask();
//    task.setTest(true);
//    task.setProcessManual(true);
//    task.setUrl("url://test");
//    task.setImportType(new Enumerator((long)7374, "ACTUALIZEFROMALEPH"));
//    Task newTask = client.createTask(task, TM_USER, true);
//
//    // Reserve task for scan
//    String workerId = String.valueOf(new Date().getTime());
//    u.reserveTask(newTask.getId(), TM_USER, workerId, "");
//    
//    // Release reservation
//    u.releaseReservedTask(newTask.getId(), TM_USER);
//    
//    // Check result
//    Task testedTask = client.getTask(newTask.getId());
//    assertNull(testedTask.getReservedBy());
//    assertNull(testedTask.getReservedDT());
//    assertTrue(testedTask.getReservedInternalId() == null || "".equals(testedTask.getReservedInternalId()));
//  }
//
//  @Test (expected = BadRequestException.class)
//  public void testReleaseReservedTaskWrongUser() throws JsonParseException, JsonMappingException, IOException, BadRequestException, TransformerException {
//    // Create new task (PREPARE)
//    LTPWorkPackageTask task = new LTPWorkPackageTask();
//    task.setTest(true);
//    task.setProcessManual(true);
//    task.setUrl("url://test");
//    task.setImportType(new Enumerator((long)7374, "ACTUALIZEFROMALEPH"));
//    Task newTask = client.createTask(task, TM_USER, true);
//
//    // Reserve task for scan
//    String workerId = String.valueOf(new Date().getTime());
//    u.reserveTask(newTask.getId(), TM_USER, workerId, "");
//    
//    // Release reservation
//    u.releaseReservedTask(newTask.getId(), "test");
//  }
//
//  @Test
//  public void testGetCodebooks() throws WFConnectionUnavailableException, BadRequestException, IOException {
//    String cbType = "NDKCBActivity";
//    List<Codebook> codebooks = u.getCodebooks(cbType);
//    assertNotNull(codebooks);
//    assertEquals("WPFINISHNOOK", codebooks.get(0).getCode());
//  }
  
}
