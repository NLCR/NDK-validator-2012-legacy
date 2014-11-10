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
import com.logica.ndk.tm.utilities.integration.wf.finishedTask.FinishedPackageTask;
import com.logica.ndk.tm.utilities.integration.wf.task.PackageTask;
import com.logica.ndk.tm.utilities.integration.wf.task.Scan;
import com.logica.ndk.tm.utilities.integration.wf.task.Signature;
import com.logica.ndk.tm.utilities.integration.wf.task.Task;
import com.logica.ndk.tm.utilities.integration.wf.task.TaskHeader;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;

public class TaskHandlerIT {
  TaskHandler u = new TaskHandlerImpl();
  WFClient client = new WFClient();

  private static final String TM_USER = "svctm";
  private static final String SCAN_USER = "ndki-manager";
  
  @Test
  public void testHandleWaitingTasks() throws TransformerException, IOException, WFConnectionUnavailableException, BadRequestException {
    // FIXME majdaf - prepare tasks for test first;
    u.handleWaitingTasks();
  }
  
//  @Test
//  public void testGetTasksByBarCode() throws WFConnectionUnavailableException, BadRequestException {
//    String barCode = "1000748306";
//    List<PackageTask> tasks = u.getTasksByBarCode(barCode);
//    assertNotNull(tasks);
//    assertTrue(tasks.size() > 0);
//  }
  
//  @Test
//  @Ignore
//  public void testFinishTask() throws JsonParseException, JsonMappingException, IOException, BadRequestException, TransformerException {
//    WFClient client = new WFClient();
//    PackageTask task = new PackageTask();
//    task.setBarCode("1000748306");
//    Enumerator locality = new Enumerator();
//    locality.setCode("NKCR");
//    task.setDocumentLocality(locality);
//    task.setLocality(new Enumerator((long)283,"NKCR"));
//    Task newTask = client.createTask(task, TM_USER, true);
//    
//    // Finish scan
//    FinishedPackageTask finishedTask = new FinishedPackageTask();
//    finishedTask.setId(newTask.getId());
//    finishedTask.setUser(TM_USER);
//    finishedTask.setOcr(new Enumerator((long)1,"ABBY"));
//    finishedTask.setProfilePP("SCANTAILORCOLOR");
//    finishedTask.setProfilePPCode("SCANTAILORCOLOR");
//    finishedTask.setProfileMCCode("JPEG2000DPI300");
//    finishedTask.setProfileUCCode("JPEG2000HALFDPIMCLOSSY");
//    finishedTask.setProfileUC("JPEG2000PRESERVEDPIMCLOSSY");
//    finishedTask.setProfileMC("JPEG2000PRESERVEDPI");
//    finishedTask.setScanAtPreparation(false);
//    finishedTask.setDestructiveDigitization(false);
//    finishedTask.setMinOCRRate(70);
//    finishedTask.setColorCode("mixed");
//    finishedTask.setScanModeCode("BASIC");
//    client.signalFinishedTask(finishedTask, "NDKSigDigitPar");
//    u.finishTask(finishedTask, "NDKSigDigitFinish");
//    
//    task = (PackageTask)client.getTask(new TaskHeader(newTask));
//    assertTrue("CREATEDIR".equals(task.getActivity().getCode()) || "SCAN".equals(task.getActivity().getCode()));
//  }
//  
//  @Test
//  public void testPing() {
//    boolean result = u.ping();
//    assertTrue(result);
//  }
//  
//  @Test
//  @Ignore
//  public void testReserveTask() throws JsonParseException, JsonMappingException, IOException, BadRequestException, TransformerException, InterruptedException {
//    WFClient client = new WFClient();
//    String barCode = "1000748306";
//    
//    // Create new task (PREPARE)
//    PackageTask task = new PackageTask();
//    task.setBarCode(barCode);
//    Enumerator documentLocality = new Enumerator();
//    documentLocality.setCode("NKCR");
//    task.setDocumentLocality(documentLocality);
//    task.setLocality(new Enumerator((long)283,"NKCR"));
//    Task newTask = client.createTask(task, TM_USER, true);
//
//    // Finish task (PREPARE)
//    FinishedPackageTask finishedTask = new FinishedPackageTask();
//    finishedTask.setId(newTask.getId());
//    finishedTask.setUser(TM_USER);
//    finishedTask.setOcr(new Enumerator((long)1,"ABBY"));
//    finishedTask.setProfilePP("SCANTAILORCOLOR");
//    finishedTask.setProfilePPCode("SCANTAILORCOLOR");
//    finishedTask.setProfileMCCode("JPEG2000DPI300");
//    finishedTask.setProfileUCCode("JPEG2000HALFDPIMCLOSSY");
//    finishedTask.setProfileUC("JPEG2000PRESERVEDPIMCLOSSY");
//    finishedTask.setProfileMC("JPEG2000PRESERVEDPI");
//    finishedTask.setScanAtPreparation(false);
//    finishedTask.setDestructiveDigitization(false);
//    finishedTask.setMinOCRRate(70);
//    finishedTask.setColorCode("mixed");
//    finishedTask.setScanModeCode("BASIC");
//    client.signalFinishedTask(finishedTask, "NDKSigDigitPar");
//    client.signalFinishedTask(finishedTask, "NDKSigDigitFinish");
//
//    TaskHeader taskHeader = new TaskHeader(newTask);
//    int counter = 0;
//    while(!newTask.getActivity().getCode().equals("SCAN")) {
//      counter++;
//      if (counter > 20) {
//        fail("CREATEDIR timeout");
//      }
//      newTask = client.getTask(taskHeader);
//      System.out.println(counter);
//      System.out.println(newTask.getActivity().getCode());
//      System.out.println(newTask.getReservedInternalId());
//      Thread.sleep(1000);
//    }
//    
//    // Reserve task for scan
//    String workerId = String.valueOf(new Date().getTime());
//    u.reserveTask(newTask.getId(), SCAN_USER, workerId, "");
//    
//    // Check
//    TaskFinder finder = new TaskFinder();
//    finder.setBarCode(barCode);
//    finder.setReservedInternalId(workerId);
//    
//    List<TaskHeader> tasks = client.getTasks(finder);
//    assertEquals(newTask.getId(), tasks.get(0).getId());
//  }
//
//  @Test
//  public void testGetScanners() {
//    List<Scanner> scanners;
//    try {
//      scanners = u.getScanners();
//      assertNotNull(scanners);
//      assertTrue(scanners.size() > 0);
//    }
//    catch (Exception e) {
//      fail(e.getMessage());
//      e.printStackTrace();
//    }
//  }
//  
//  @Test
//  public void testGetSignatures() throws IOException, BadRequestException {
//    List<Signature> signatures;
//    Long taskId = (long)42301;
//    signatures = u.getSignatures(taskId);
//    assertNotNull(signatures);
//    assertTrue(signatures.size() > 0);
//  }

//  @Test
//  @Ignore
//  public void testGetScans() throws JsonParseException, JsonMappingException, IOException, BadRequestException, TransformerException, InterruptedException {
//    WFClient client = new WFClient();
//    
//    // Create new task (PREPARE)
//    PackageTask task = new PackageTask();
//    task.setBarCode("1000748306");
//    Enumerator documentLocality = new Enumerator();
//    documentLocality.setCode("NKCR");
//    task.setDocumentLocality(documentLocality);
//    task.setLocality(new Enumerator((long)283,"NKCR"));
//    Task newTask = client.createTask(task, TM_USER, true);
//
//    // Finish task (PREPARE)
//    TaskHeader testedTask = new TaskHeader();
//    testedTask.setId(newTask.getId());
//    testedTask.setPackageType(newTask.getPackageType());
//    FinishedPackageTask finishedTask = new FinishedPackageTask();
//    finishedTask.setId(newTask.getId());
//    finishedTask.setUser(TM_USER);
//    finishedTask.setOcr(new Enumerator((long)1,"ABBY"));
//    finishedTask.setProfilePP("SCANTAILORCOLOR");
//    finishedTask.setProfilePPCode("SCANTAILORCOLOR");
//    finishedTask.setProfileMCCode("JPEG2000DPI300");
//    finishedTask.setProfileUCCode("JPEG2000HALFDPIMCLOSSY");
//    finishedTask.setProfileUC("JPEG2000PRESERVEDPIMCLOSSY");
//    finishedTask.setProfileMC("JPEG2000PRESERVEDPI");
//    finishedTask.setScanAtPreparation(false);
//    finishedTask.setDestructiveDigitization(false);
//    finishedTask.setMinOCRRate(70);
//    finishedTask.setColorCode("mixed");
//    finishedTask.setScanModeCode("BASIC");
//    client.signalFinishedTask(finishedTask, "NDKSigDigitPar");
//    client.signalFinishedTask(finishedTask, "NDKSigDigitFinish");
//
//    TaskHeader taskHeader = new TaskHeader(newTask);
//    int counter = 0;
//    while(!newTask.getActivity().getCode().equals("SCAN")) {
//      counter++;
//      if (counter > 20) {
//        fail("CREATEDIR timeout");
//      }
//      newTask = client.getTask(taskHeader);
//      System.out.println(counter);
//      System.out.println(newTask.getActivity().getCode());
//      System.out.println(newTask.getReservedInternalId());
//      Thread.sleep(1000);
//    }
//    
//    List<Scanner> scanners = client.getScanners(new ScannerFinder());
//    Scanner scanner = scanners.get(0);
//    FinishedPackageTask finishedPackageTask;
//    
//    // Reserve scan 1
//    client.reserveSystemTask(newTask.getId(), SCAN_USER, "123", "");
//    // Finish scan 1
//    finishedPackageTask = (FinishedPackageTask)finishedTask;
//    finishedPackageTask.setUser(SCAN_USER);
//    finishedPackageTask.setComplete(false);
//    finishedPackageTask.setLocalURN("1");
//    finishedPackageTask.setScanTypeCode("FREE");
//    finishedPackageTask.setPages("10");
//    finishedPackageTask.setScanDuration("100");
//    finishedPackageTask.setScannerCode(scanner.getCode());
//    finishedPackageTask.setScanCount(1);
//    finishedPackageTask.setCropTypeCode("1");
//    finishedPackageTask.setProfilePPCode("SCANTAILORCOLOR");
//    finishedPackageTask.setDimensionX(100);
//    finishedPackageTask.setDimensionY(100);
//    finishedPackageTask.setDoublePage(true);
//    finishedPackageTask.setScanId((long)1);
//    client.signalFinishedTask(finishedPackageTask, "NDKSigDigitFinishScan");
//    newTask = client.getTask(taskHeader);
//    assertEquals("SCAN", newTask.getActivity().getCode());
//    
//    // Reserve scan 2
//    client.reserveSystemTask(newTask.getId(), SCAN_USER, "123", "");
//    // Finish scan 2
//    finishedPackageTask = (FinishedPackageTask)finishedTask;
//    finishedPackageTask.setUser(SCAN_USER);
//    finishedPackageTask.setComplete(true);
//    finishedPackageTask.setLocalURN("2");
//    finishedPackageTask.setScanTypeCode("FREE");
//    finishedPackageTask.setPages("20");
//    finishedPackageTask.setScanDuration("200");
//    finishedPackageTask.setScannerCode(scanner.getCode());
//    finishedPackageTask.setScanCount(2);
//    finishedPackageTask.setCropTypeCode("1");
//    finishedPackageTask.setProfilePPCode("SCANTAILORCOLOR");
//    finishedPackageTask.setDimensionX(100);
//    finishedPackageTask.setDimensionY(100);
//    finishedPackageTask.setDoublePage(false);
//    finishedPackageTask.setScanId((long)2);
//    client.signalFinishedTask(finishedPackageTask, "NDKSigDigitFinishScan");
//    newTask = client.getTask(taskHeader);
//    assertEquals("UPLOAD", newTask.getActivity().getCode());
//
//    // Get scans
//    List<Scan> scans = u.getScans(newTask.getId());
//    System.out.println(scans);
//    assertNotNull(scans);
//    assertTrue(scans.size() == 2);
//    assertEquals("1", scans.get(0).getLocalURN());
//  }
//
//  @Test
//  @Ignore
//  public void testReleaseReservedTask() throws JsonParseException, JsonMappingException, IOException, BadRequestException, TransformerException {
//    WFClient client = new WFClient();
//    PackageTask task = new PackageTask();
//    task.setBarCode("1000748306");
//    Enumerator locality = new Enumerator();
//    locality.setCode("NKCR");
//    task.setDocumentLocality(locality);
//    task.setLocality(new Enumerator((long)283,"NKCR"));
//    Task newTask = client.createTask(task, TM_USER, true);
//    
//    // Release reservation
//    u.releaseReservedTask(newTask.getId(), TM_USER);
//    
//    // Check result
//    task = (PackageTask)client.getTask(new TaskHeader(newTask));
//    assertNull(task.getReservedBy());
//    assertNull(task.getReservedDT());
//    assertTrue(task.getReservedInternalId() == null || "".equals(task.getReservedInternalId()));
//  }
//
//  @Test (expected = BadRequestException.class)
//  public void testReleaseReservedTaskWrongUser() throws JsonParseException, JsonMappingException, IOException, BadRequestException, TransformerException {
//    WFClient client = new WFClient();
//    PackageTask task = new PackageTask();
//    task.setBarCode("1000748306");
//    Enumerator locality = new Enumerator();
//    locality.setCode("NKCR");
//    task.setDocumentLocality(locality);
//    task.setLocality(new Enumerator((long)283,"NKCR"));
//    Task newTask = client.createTask(task, TM_USER, true);
//    
//    // Release reservation
//    u.releaseReservedTask(newTask.getId(), "test");
//  }
//
//  @Test
//  public void testGetPackageTask() throws TransformerException, IOException, BadRequestException {
//    // Prepare data
//    TaskFinder finder = new TaskFinder();
//    finder.setPackageType(WFClient.PACKAGE_TYPE_PACKAGE);
//    List<TaskHeader> tasks = client.getTasks(finder);
//    assertTrue("No tasks to test", tasks.size() > 0);
//    TaskHeader testedTask = tasks.get(0);
//    
//    PackageTask task = u.getPackageTask(testedTask.getId());
//    assertNotNull(task);
//    assertEquals(testedTask.getId(), task.getId());
//  }
//
//  @Test
//  public void testGetCodebooks() throws WFConnectionUnavailableException, BadRequestException, IOException {
//    String cbType = "NDKCBprofileUC";
//    List<Codebook> codebooks = u.getCodebooks(cbType);
//    assertNotNull(codebooks);
//    assertEquals("UC1-4", codebooks.get(0).getCode());
//  }
  
}
