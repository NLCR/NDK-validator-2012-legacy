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

import com.logica.ndk.tm.utilities.integration.wf.ScannerFinder;
import com.logica.ndk.tm.utilities.integration.wf.TaskFinder;
import com.logica.ndk.tm.utilities.integration.wf.UUIDFinder;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Codebook;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.CodebookFinder;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.DocumentLocality;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Enumerator;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Scanner;
import com.logica.ndk.tm.utilities.integration.wf.exception.BadRequestException;
import com.logica.ndk.tm.utilities.integration.wf.finishedTask.FinishedIETask;
import com.logica.ndk.tm.utilities.integration.wf.finishedTask.FinishedPackageTask;
import com.logica.ndk.tm.utilities.integration.wf.ping.PingResponse;
import com.logica.ndk.tm.utilities.integration.wf.task.IDTask;
import com.logica.ndk.tm.utilities.integration.wf.task.IETask;
import com.logica.ndk.tm.utilities.integration.wf.task.PackageTask;
import com.logica.ndk.tm.utilities.integration.wf.task.Scan;
import com.logica.ndk.tm.utilities.integration.wf.task.Signature;
import com.logica.ndk.tm.utilities.integration.wf.task.Task;
import com.logica.ndk.tm.utilities.integration.wf.task.TaskHeader;
import com.logica.ndk.tm.utilities.integration.wf.task.UUIDResult;

public class WFClientIT {
  WFClient client = new WFClient();

  private static final String TM_USER = "svctm";
  private static final String SCAN_USER = "ndki-manager";

  @Ignore
  public void testGetWaitingTasks() throws TransformerException, IOException, BadRequestException {
    List<TaskHeader> tasks = client.getTasks(new TaskFinder());
    assertNotNull(tasks);
    assertTrue(tasks.size() > 0);
  }

  @Ignore
  public void testGetTask() throws TransformerException, IOException, BadRequestException {

    TaskFinder finder = new TaskFinder();
    finder.setPackageType(WFClient.PACKAGE_TYPE_PACKAGE);
    List<TaskHeader> tasks = client.getTasks(finder);
    assertTrue("No suitable task for test", tasks.size() > 0);
    Task task = client.getTask(tasks.get(0));
    assertNotNull(task);
    assertNotNull(task.getActivity());
  }

  @Ignore
  public void testGetTaskID() throws TransformerException, IOException, BadRequestException {
    TaskFinder finder = new TaskFinder();
    finder.setPackageType(WFClient.PACKAGE_TYPE_IMPORT);
    List<TaskHeader> tasks = client.getTasks(finder);
    assertTrue("No suitable task for test", tasks.size() > 0);
    Task task = client.getTask(tasks.get(0));
    assertNotNull(task);
    assertNotNull(task.getActivity());
  }

  @Ignore
  public void testGetTaskIE() throws TransformerException, IOException, BadRequestException {
    TaskFinder finder = new TaskFinder();
    finder.setPackageType(WFClient.PACKAGE_TYPE_IE);
    List<TaskHeader> tasks = client.getTasks(finder);
    assertTrue("No suitable task for test", tasks.size() > 0);
    Task task = client.getTask(tasks.get(0));
    assertNotNull(task);
    assertNotNull(task.getActivity());
  }

  @Ignore
  public void testGetTaskById() throws TransformerException, IOException, BadRequestException {

    TaskFinder finder = new TaskFinder();
    finder.setPackageType(WFClient.PACKAGE_TYPE_PACKAGE);
    List<TaskHeader> tasks = client.getTasks(finder);
    assertTrue("No suitable task for test", tasks.size() > 0);
    Task task = client.getTask(tasks.get(0).getId());
    assertNotNull(task);
    assertNotNull(task.getActivity());
    assertTrue(task instanceof PackageTask);
  }

  @Ignore
  public void testGetTaskByIdID() throws TransformerException, IOException, BadRequestException {
    TaskFinder finder = new TaskFinder();
    finder.setPackageType(WFClient.PACKAGE_TYPE_IMPORT);
    List<TaskHeader> tasks = client.getTasks(finder);
    assertTrue("No suitable task for test", tasks.size() > 0);
    Task task = client.getTask(tasks.get(0).getId());
    assertNotNull(task);
    assertNotNull(task.getActivity());
    assertTrue(task instanceof IDTask);
  }

  @Ignore
  public void testGetTaskByIdIE() throws TransformerException, IOException, BadRequestException {
    TaskFinder finder = new TaskFinder();
    finder.setPackageType(WFClient.PACKAGE_TYPE_IE);
    List<TaskHeader> tasks = client.getTasks(finder);
    assertTrue("No suitable task for test", tasks.size() > 0);
    Task task = client.getTask(tasks.get(0).getId());
    assertNotNull(task);
    assertNotNull(task.getActivity());
    assertTrue(task instanceof IETask);
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
    client.reserveSystemTask(testedTask.getId(), SCAN_USER, processInstanceId, "tst");

    Task task = client.getTask(testedTask);
    assertNotNull("Task reservation info is null", task.getReservedDT());
    assertEquals("Incorrect reservation", processInstanceId, task.getReservedInternalId());
  }

  @Ignore
  public void testSignalFinishedTask() throws JsonParseException, JsonMappingException, IOException, BadRequestException, TransformerException {

    // Create new task
    PackageTask task = new PackageTask();
    task.setBarCode("1000748306");
    task.setDocumentLocality(new DocumentLocality("NKCR", "NKCR", "NKCR"));
    task.setLocality(new Enumerator((long) 283, "NKCR"));
    Task newTask = client.createTask(task, TM_USER, true);

    // Finish task
    TaskHeader testedTask = new TaskHeader();
    testedTask.setId(newTask.getId());
    testedTask.setPackageType(newTask.getPackageType());
    FinishedPackageTask finishedTask = new FinishedPackageTask();
    finishedTask.setId(newTask.getId());
    finishedTask.setUser(TM_USER);
    finishedTask.setOcr(new Enumerator((long) 1, "ABBY"));
    finishedTask.setProfilePP("SCANTAILORCOLOR");
    finishedTask.setProfilePPCode("SCANTAILORCOLOR");
    finishedTask.setProfileMCCode("MC1-1");
    finishedTask.setProfileUCCode("UC1-4");
    finishedTask.setProfileUC("MC1-1");
    finishedTask.setProfileMC("UC1-4");
    finishedTask.setScanAtPreparation(false);
    finishedTask.setDestructiveDigitization(false);
    finishedTask.setMinOCRRate(70);
    finishedTask.setColorCode("mixed");
    finishedTask.setScanModeCode("BASIC");
    client.signalFinishedTask(finishedTask, "NDKSigDigitPar");
    client.signalFinishedTask(finishedTask, "NDKSigDigitFinish");
    Task checkTask = client.getTask(testedTask);
    assertNull("Task reservation date time is not null", checkTask.getReservedDT());
  }

  @Ignore
  public void testGetScans() throws JsonParseException, JsonMappingException, IOException, BadRequestException, TransformerException, InterruptedException {

    // Create new task (PREPARE)
    PackageTask task = new PackageTask();
    task.setBarCode("1000748306");
    task.setDocumentLocality(new DocumentLocality("NKCR", "NKCR", "NKCR"));
    task.setLocality(new Enumerator((long) 283, "NKCR"));
    Task newTask = client.createTask(task, TM_USER, true);

    // Finish task (PREPARE)
    TaskHeader testedTask = new TaskHeader();
    testedTask.setId(newTask.getId());
    testedTask.setPackageType(newTask.getPackageType());
    FinishedPackageTask finishedTask = new FinishedPackageTask();
    finishedTask.setId(newTask.getId());
    finishedTask.setUser(TM_USER);
    finishedTask.setOcr(new Enumerator((long) 1, "ABBY"));
    finishedTask.setProfilePP("SCANTAILORCOLOR");
    finishedTask.setProfilePPCode("SCANTAILORCOLOR");
    finishedTask.setProfileMCCode("MC1-1");
    finishedTask.setProfileUCCode("UC1-4");
    finishedTask.setProfileUC("MC1-1");
    finishedTask.setProfileMC("UC1-4");
    finishedTask.setScanAtPreparation(false);
    finishedTask.setDestructiveDigitization(false);
    finishedTask.setMinOCRRate(70);
    finishedTask.setColorCode("mixed");
    finishedTask.setScanModeCode("BASIC");
    client.signalFinishedTask(finishedTask, "NDKSigDigitPar");
    client.signalFinishedTask(finishedTask, "NDKSigDigitFinish");

    TaskHeader taskHeader = new TaskHeader(newTask);
    int counter = 0;
    while (!newTask.getActivity().getCode().equals("SCAN")) {
      counter++;
      if (counter > 20) {
        fail("CREATEDIR timeout");
      }
      newTask = client.getTask(taskHeader);
      System.out.println(counter);
      System.out.println(newTask.getActivity().getCode());
      System.out.println(newTask.getReservedInternalId());
      Thread.sleep(1000);
    }

    List<Scanner> scanners = client.getScanners(new ScannerFinder());
    Scanner scanner = scanners.get(0);
    FinishedPackageTask finishedPackageTask;

    // Reserve scan 1
    client.reserveSystemTask(newTask.getId(), SCAN_USER, "123", "");
    // Finish scan 1
    finishedPackageTask = (FinishedPackageTask) finishedTask;
    finishedPackageTask.setUser(SCAN_USER);
    finishedPackageTask.setComplete(false);
    finishedPackageTask.setLocalURN("1");
    finishedPackageTask.setScanTypeCode("FREE");
    finishedPackageTask.setPages("10");
    finishedPackageTask.setScanDuration("100");
    finishedPackageTask.setScannerCode(scanner.getCode());
    finishedPackageTask.setScanCount(1);
    finishedPackageTask.setDoublePage(true);
    finishedPackageTask.setScanId((long) 1);
    finishedPackageTask.setCropTypeCode("1");
    finishedPackageTask.setProfilePPCode("SCANTAILORCOLOR");
    finishedPackageTask.setDimensionX(100);
    finishedPackageTask.setDimensionY(100);
    client.signalFinishedTask(finishedPackageTask, "NDKSigDigitFinishScan");
    newTask = client.getTask(taskHeader);
    assertEquals("SCAN", newTask.getActivity().getCode());

    // Reserve scan 2
    client.reserveSystemTask(newTask.getId(), SCAN_USER, "123", "");
    // Finish scan 2
    finishedPackageTask = (FinishedPackageTask) finishedTask;
    finishedPackageTask.setUser(SCAN_USER);
    finishedPackageTask.setComplete(true);
    finishedPackageTask.setLocalURN("2");
    finishedPackageTask.setScanTypeCode("FREE");
    finishedPackageTask.setPages("20");
    finishedPackageTask.setScanDuration("200");
    finishedPackageTask.setScannerCode(scanner.getCode());
    finishedPackageTask.setScanCount(2);
    finishedPackageTask.setDoublePage(false);
    finishedPackageTask.setScanId((long) 2);
    finishedPackageTask.setCropTypeCode("1");
    finishedPackageTask.setProfilePPCode("SCANTAILORCOLOR");
    finishedPackageTask.setDimensionX(100);
    finishedPackageTask.setDimensionY(100);
    client.signalFinishedTask(finishedPackageTask, "NDKSigDigitFinishScan");
    newTask = client.getTask(taskHeader);
    assertEquals("UPLOAD", newTask.getActivity().getCode());

    // Get scans
    List<Scan> scans = client.getScans(newTask.getId());
    System.out.println(scans);
    assertNotNull(scans);
    assertTrue(scans.size() == 2);
    assertEquals("1", scans.get(0).getLocalURN());
  }

  @Ignore
  public void testPing() {
    PingResponse response = client.ping();
    assertTrue(response != null);
    assertTrue(response.getTime() != null);
  }

  @Ignore
  public void testGetScanners() throws IOException, BadRequestException {
    ScannerFinder finder = new ScannerFinder();
    List<Scanner> scanners;
    scanners = client.getScanners(finder);
    assertNotNull(scanners);
    assertTrue(scanners.size() > 0);
  }

  @Ignore
  public void testGetScannersFinder() throws IOException, BadRequestException {
    ScannerFinder finder = new ScannerFinder();
    List<Scanner> scanners;
    scanners = client.getScanners(finder);
    assertNotNull("No suitable scanners for test", scanners);
    assertTrue("No suitable scanners for test", scanners.size() > 0);
    Scanner s = scanners.get(0);
    finder.setCode(s.getCode());
    scanners = client.getScanners(finder);
    assertTrue(scanners.size() == 1);
  }

  @Ignore
  public void testGetScannersEmpty() throws IOException, BadRequestException {
    ScannerFinder finder = new ScannerFinder();
    finder.setCode("test");
    List<Scanner> scanners;
    scanners = client.getScanners(finder);
    assertNotNull(scanners);
    assertTrue(scanners.size() == 0);
  }

  @Ignore
  public void testCreateTask() throws JsonParseException, JsonMappingException, IOException, BadRequestException {
    PackageTask task = new PackageTask();
    task.setBarCode("1000748306");
    task.setDocumentLocality(new DocumentLocality("NKCR", "NKCR", "NKCR"));
    task.setLocality(new Enumerator((long) 283, "NKCR"));
    Task newTask = client.createTask(task, TM_USER, true);
    assertNotNull(newTask);
    assertTrue(task instanceof PackageTask);
    assertNotNull(newTask.getId());
    assertEquals("1000748306", ((PackageTask) newTask).getBarCode());
  }

  @Ignore
  public void testError() {
    try {
      client.getTask((long) -1);
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
    finder.setCbType("NDKCBprofileUC");
    List<Codebook> codebooks = client.getCodebooks(finder);
    assertTrue("No suitable task for test", codebooks.size() > 0);
    assertEquals("UC1-4", codebooks.get(0).getCode());
  }

  @Ignore
  public void testGetSignatures() throws TransformerException, IOException, BadRequestException {
    Long taskId = (long) 42301;
    List<Signature> signatures = client.getSignatures(taskId);
    assertTrue("No signatures found on task", signatures.size() > 0);
    assertEquals("NDKSigDigitPar", signatures.get(0).getSignatureType());
    assertEquals(taskId, signatures.get(0).getPackageId());
    assertEquals("PREPARE", signatures.get(0).getActivityCode());
  }

  @Ignore
  public void testCreateIE() throws JsonParseException, JsonMappingException, IOException, BadRequestException, TransformerException {

    PackageTask task = new PackageTask();
    task.setBarCode("2619882844");
    task.setDocumentLocality(new DocumentLocality("NKCR", "NKCR", "NKCR"));
    task.setLocality(new Enumerator((long) 283, "MZK"));
    Task newTask = client.createTask(task, TM_USER, true);
    assertNotNull(newTask);

    IETask ieTask = new IETask();
    //task.setBarCode("1000748306");
    task.setDocumentLocality(new DocumentLocality("NKCR", "NKCR", "NKCR"));
    ieTask.setDocumentLocality(new DocumentLocality("NKCR", "NKCR", "NKCR"));
    ieTask.setPathId("pokushokus");
    ieTask.setUuid("ajdsljasldj");
    ieTask.setSourcePackage(newTask.getId());
    Task newIeTask = client.createTask(ieTask, TM_USER, true);
    assertNotNull(newIeTask);
    assertTrue(newIeTask instanceof IETask);
    assertNotNull(newIeTask.getId());
    //assertEquals("1000748306", ((PackageTask)newTask).getBarCode());

    FinishedIETask finishTask = new FinishedIETask();
    finishTask.setTitle("PĹ™Ă­liĹˇ ĹľluĹĄouÄŤkĂ˝ kĹŻĹ� ĂşpÄ›l ÄŹĂˇbleskĂ© Ăłdy");
    finishTask.setPageCount("3");
    finishTask.setTypeCode("BK");
    finishTask.setId(newIeTask.getId());
    finishTask.setUser(TM_USER);

    client.reserveSystemTask(newIeTask.getId(), TM_USER, "0", "ĹľĹľĹľĹľ");

    client.signalFinishedTask(finishTask, "NDKSigIEntityFinishBiblio");

    IETask myIeTask = (IETask) client.getTask(newIeTask.getId());
    assertEquals("PĹ™Ă­liĹˇ ĹľluĹĄouÄŤkĂ˝ kĹŻĹ� ĂşpÄ›l ÄŹĂˇbleskĂ© Ăłdy", myIeTask.getTitle());
  }

  @Ignore
  public void testGetUUIDs() throws TransformerException, IOException, BadRequestException {

    UUIDFinder finder = new UUIDFinder();
    finder.setRecordIdentifier("nkc20122430296");
    List<UUIDResult> uuids = client.getUUIDs(finder);
    assertTrue("No suitable task for test", uuids.size() > 0);
    assertEquals("Bratrstvo", uuids.get(0).getTitle());
  }

  @Ignore
  public void testCreateScan() throws JsonParseException, JsonMappingException, IOException, BadRequestException {
    long packageId = 32877l;

    Scan scan = new Scan();
    scan.setScanId(1l);
    scan.setPackageId(packageId);
    scan.setScanTypeCode("ENVELOPEDESTRUCT");
    scan.setScannerCode("TREVENTUSSCANROBOT20MDS");
    scan.setScanCount(3);
    scan.setPages("3");
    scan.setScanModeCode("BASIC");
    scan.setCropTypeCode("1");
    scan.setStatePP(1);
    scan.setProfilePPCode("SCANTAILORCOLOR");
    scan.setScanDuration(105l);
    scan.setLocalURN("xxx");

    Scan newScan = client.createScan(scan, TM_USER);
    assertNotNull(newScan);
    assertTrue(newScan instanceof Scan);
    assertNotNull(newScan.getId());
  }
}
