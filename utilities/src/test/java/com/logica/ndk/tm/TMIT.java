package com.logica.ndk.tm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
import com.logica.ndk.tm.utilities.integration.wf.enumerator.DocumentLocality;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Enumerator;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Scanner;
import com.logica.ndk.tm.utilities.integration.wf.exception.BadRequestException;
import com.logica.ndk.tm.utilities.integration.wf.finishedTask.FinishedPackageTask;
import com.logica.ndk.tm.utilities.integration.wf.finishedTask.FinishedTask;
import com.logica.ndk.tm.utilities.integration.wf.task.PackageTask;
import com.logica.ndk.tm.utilities.integration.wf.task.Scan;
import com.logica.ndk.tm.utilities.integration.wf.task.Task;
import com.logica.ndk.tm.utilities.integration.wf.task.TaskHeader;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;

public class TMIT {
  WFClient client = new WFClient();

  protected static final String USER_WF = "svctm";
  protected static final String USER_TM = "svctm";
  protected static final String USER_LSA = "majdaf";

  @Ignore
  public void testDigitizationWF() throws JsonParseException, JsonMappingException, IOException, BadRequestException, TransformerException, InterruptedException {
    
    // Create new task (PREPARE)
    PackageTask task = (PackageTask)createTask("1000748306", "NKCR", USER_WF);
    assertEquals("PREPARE", task.getActivity().getCode());
    
    // Finish task (PREPARE)
    task = (PackageTask)finishTask(task, "NDKSigDigitFinish", USER_WF);
    assertTrue("SCAN".equals(task.getActivity().getCode()) || "CREATEDIR".equals(task.getActivity().getCode()));
    
    // Wait until CREATEDIR finished
    task = (PackageTask)waitForActivity(task, "SCAN", 20, 1);
    assertEquals("SCAN", task.getActivity().getCode());
    
    List<Scan> scans;
    
    // Scan 1
    task = (PackageTask)createScan(task, USER_LSA, false);
    assertEquals("SCAN", task.getActivity().getCode());
    scans = client.getScans(task.getId());
    assertNotNull(scans);
    assertTrue(scans.size() == 1);
    
    // Scan 1
    task = (PackageTask)createScan(task, USER_LSA, true);
    assertEquals("UPLOAD", task.getActivity().getCode());
    scans = client.getScans(task.getId());
    assertNotNull(scans);
    assertTrue(scans.size() == 2);

    
  }
  
  protected Task createTask(String barCode, String localityCode, String user) throws JsonParseException, JsonMappingException, IOException, BadRequestException {
    PackageTask task = new PackageTask();
    task.setBarCode(barCode);
    task.setDocumentLocality(new DocumentLocality(localityCode, localityCode, localityCode));
    return client.createTask(task, user, true);
  }
  
  protected Task finishTask(Task task, String signal, String user) throws TransformerException, IOException, BadRequestException {
    FinishedTask finishedTask = new FinishedPackageTask();
    finishedTask.setId(task.getId());
    finishedTask.setUser(user);
    client.signalFinishedTask(finishedTask, signal);
    TaskHeader header = new TaskHeader(task);
    task = client.getTask(header);
    return task;
  }
  
  protected Task waitForActivity(Task task, String activityCode, int timeout, int interval) throws JsonParseException, JsonMappingException, IOException, TransformerException, BadRequestException, InterruptedException {
    TaskHeader taskHeader = new TaskHeader(task);
    int counter = 0;
    while(!task.getActivity().getCode().equals(activityCode)) {
      counter++;
      if (counter > timeout) {
        fail("Activity timeout");
      }
      task = client.getTask(taskHeader);
      System.out.println(counter);
      System.out.println(task.getActivity().getCode());
      System.out.println(task.getReservedInternalId());
      Thread.sleep(interval * 1000);
    }
    return task;
  }
  
  protected Task createScan(PackageTask task, String user, boolean complete) throws IOException, BadRequestException, TransformerException {
    List<Scanner> scanners = client.getScanners(new ScannerFinder());
    Scanner scanner = scanners.get(0);
    TaskHeader taskHeader = new TaskHeader(task);

    client.reserveSystemTask(task.getId(), user, "123", "");
    // Finish scan 1
    FinishedPackageTask finishedPackageTask = new FinishedPackageTask();
    finishedPackageTask.setId(task.getId());
    finishedPackageTask.setUser(user);
    finishedPackageTask.setComplete(complete);
    finishedPackageTask.setLocalURN("1");
    finishedPackageTask.setScanTypeCode("FREE");
    finishedPackageTask.setPages("10");
    finishedPackageTask.setScanDuration("100");
    finishedPackageTask.setScannerCode(scanner.getCode());
    finishedPackageTask.setScanCount(1);
    finishedPackageTask.setCropTypeCode("ONEPAGE");
    finishedPackageTask.setProfilePPCode("SCANTAILORCOLOR");
    finishedPackageTask.setDimensionX(100);
    finishedPackageTask.setDimensionY(100);
    finishedPackageTask.setDoublePage(true);
    finishedPackageTask.setScanId(task.getNextScanId());
    client.signalFinishedTask(finishedPackageTask, "NDKSigDigitFinishScan");
    task = (PackageTask)client.getTask(taskHeader);
    return task;
  }
  
}
