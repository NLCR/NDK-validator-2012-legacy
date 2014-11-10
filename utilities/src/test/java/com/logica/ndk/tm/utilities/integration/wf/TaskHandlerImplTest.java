package com.logica.ndk.tm.utilities.integration.wf;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.commons.utils.test.TestUtils;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Activity;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Enumerator;
import com.logica.ndk.tm.utilities.integration.wf.exception.BadRequestException;
import com.logica.ndk.tm.utilities.integration.wf.task.IDTask;
import com.logica.ndk.tm.utilities.integration.wf.task.IETask;
import com.logica.ndk.tm.utilities.integration.wf.task.PackageTask;
import com.logica.ndk.tm.utilities.integration.wf.task.Scan;
import com.logica.ndk.tm.utilities.integration.wf.task.TaskHeader;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.jbpm.FreeProcess;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.jbpm.JBPMBusinessException_Exception;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.jbpm.JBPMSystemException_Exception;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.jbpm.JBPMWSFacadeClient;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.jbpm.ProcessMap;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;

public class TaskHandlerImplTest {

  private final WFClient wfClientMock = mock(WFClient.class);
  private final JBPMWSFacadeClient jbpmClientMock = mock(JBPMWSFacadeClient.class);
  private final TaskHandlerImpl u = new TaskHandlerImpl();
  
  private static final Activity ACTIVITY_CREATEDIR = new Activity((long)1, "CREATEDIR");
  private static final Activity ACTIVITY_PREPAREPP = new Activity((long)2, "PREPAREPP");
  private static final Activity ACTIVITY_UPLOAD    = new Activity((long)3, "UPLOAD");
  private static final Activity ACTIVITY_IEPREPARE = new Activity((long)4, "IEPREPARE");
  
  private static final Enumerator PROFILEPP_SCANTAILORCOLOR = new Enumerator((long)1, "SCANTAILORCOLOR");
  private static final Enumerator PROFILEPP_SCANTAILORBW    = new Enumerator((long)2, "SCANTAILORBW");
  private static final Enumerator IMPORT_TYPE_MNS           = new Enumerator((long)3, "MNS");
  
  private static final String PACKAGE_TYPE_DIGIT_PACKAGE = "NDKDigitPackage";
  private static final String LOCALITY_NKCR = "NKCR";
  private static final String LOCALITY_MZK = "MZK";
  
  @Before
  public void setUp() throws Exception {
    TestUtils.setField(u, "wfClient", wfClientMock);
    TestUtils.setField(u, "jbpmClient", jbpmClientMock);
  }

  @After
  public void tearDown() throws Exception {
    reset(wfClientMock);
    reset(jbpmClientMock);
  }

  @SuppressWarnings("unchecked")
  @Ignore
  public void testHandleWaitingTasks() throws TransformerException, IOException, BadRequestException, JBPMBusinessException_Exception, JBPMSystemException_Exception {
    List<TaskHeader> allTasks = new ArrayList<TaskHeader>();
    List<TaskHeader> nkcrTasks = new ArrayList<TaskHeader>();
    List<TaskHeader> mzkTasks = new ArrayList<TaskHeader>();

    TaskFinder defaultFinder = new TaskFinder();
    defaultFinder.setPackageType(PACKAGE_TYPE_DIGIT_PACKAGE);
    defaultFinder.setOnlyWaiting(true);
    defaultFinder.setOnlyForSystem(true);
    defaultFinder.setError(false);
    TaskFinder nkcrFinder = new TaskFinder();
    nkcrFinder.setPackageType(PACKAGE_TYPE_DIGIT_PACKAGE);
    nkcrFinder.setLocalityCode(LOCALITY_NKCR);
    nkcrFinder.setOnlyWaiting(true);
    nkcrFinder.setOnlyForSystem(true);
    nkcrFinder.setError(false);
    TaskFinder mzkFinder = new TaskFinder();
    mzkFinder.setPackageType(PACKAGE_TYPE_DIGIT_PACKAGE);
    mzkFinder.setLocalityCode(LOCALITY_MZK);
    mzkFinder.setOnlyWaiting(true);
    mzkFinder.setOnlyForSystem(true);
    mzkFinder.setError(false);
    
    
    // package task 1
    PackageTask task1 = new PackageTask();
    task1.setId((long)1);
    task1.setActivity(ACTIVITY_CREATEDIR);
    TaskHeader taskHeader1 = new TaskHeader(task1);
    allTasks.add(taskHeader1);
    nkcrTasks.add(taskHeader1);
    // package task 2
    PackageTask task2 = new PackageTask();
    task2.setId((long)2);
    task2.setActivity(ACTIVITY_PREPAREPP);
    task2.setProfilePP(PROFILEPP_SCANTAILORBW);
    TaskHeader taskHeader2 = new TaskHeader(task2);
    allTasks.add(taskHeader2);
    nkcrTasks.add(taskHeader2);
    // package task 3
    PackageTask task3 = new PackageTask();
    task3.setId((long)3);
    task3.setActivity(ACTIVITY_PREPAREPP);
    TaskHeader taskHeader3 = new TaskHeader(task3);
    allTasks.add(taskHeader3);
    nkcrTasks.add(taskHeader3);
    // import task 4
    IDTask task4 = new IDTask();
    task4.setId((long)4);
    task4.setActivity(ACTIVITY_UPLOAD);
    task4.setImportType(IMPORT_TYPE_MNS);
    TaskHeader taskHeader4 = new TaskHeader(task4);
    allTasks.add(taskHeader4);
    mzkTasks.add(taskHeader4);
    // entity task 5
    IETask task5 = new IETask();
    task5.setId((long)5);
    task5.setActivity(ACTIVITY_IEPREPARE);
    task5.setSourcePackage(task4.getId());
    task5.setImportType(IMPORT_TYPE_MNS);
    TaskHeader taskHeader5 = new TaskHeader(task5);
    allTasks.add(taskHeader5);
    mzkTasks.add(taskHeader5);
    // entity task 6
    IETask task6 = new IETask();
    task6.setId((long)6);
    task6.setActivity(ACTIVITY_IEPREPARE);
    task6.setSourcePackage(task3.getId());
    TaskHeader taskHeader6 = new TaskHeader(task6);
    allTasks.add(taskHeader6);
    
    
    doReturn(allTasks).when(wfClientMock).getTasks(defaultFinder);
    doReturn(nkcrTasks).when(wfClientMock).getTasks(nkcrFinder);
    doReturn(mzkTasks).when(wfClientMock).getTasks(mzkFinder);
    
    doReturn(task1).when(wfClientMock).getTask(taskHeader1);
    doReturn(task2).when(wfClientMock).getTask(taskHeader2);
    doReturn(task3).when(wfClientMock).getTask(taskHeader3);
    doReturn(task3).when(wfClientMock).getTask((long)3);
    doReturn(task4).when(wfClientMock).getTask(taskHeader4);
    doReturn(task4).when(wfClientMock).getTask((long)4);
    doReturn(task5).when(wfClientMock).getTask(taskHeader5);
    doReturn(task6).when(wfClientMock).getTask(taskHeader6);
    doReturn(new ArrayList<Scan>()).when(wfClientMock).getScans(anyLong());
    
    ProcessMap map = getProcessMap();
    doReturn(map).when(jbpmClientMock).getFreeInstances(true);
    
    
    int size = allTasks.size();
    
    u.handleWaitingTasks();
    
    // Check total JBPM calls
    verify(jbpmClientMock, times(size)).createProcessInstance(anyString(), (Map<String,String>)anyObject());
    verify(jbpmClientMock, times(size)).startProcessInstance(anyLong());
    
    // Check call routing by type parameter
    verify(jbpmClientMock, times(1)).createProcessInstance(eq("import.vstup-manuscriptorium"), (Map<String,String>)anyObject());
    verify(jbpmClientMock, times(1)).createProcessInstance(eq("import.vstup-digitalizace-fix"), (Map<String,String>)anyObject());
    
  }

  @SuppressWarnings("unchecked")
  @Ignore
  public void testCache() throws TransformerException, IOException, BadRequestException, JBPMBusinessException_Exception, JBPMSystemException_Exception {
    List<TaskHeader> allTasks = new ArrayList<TaskHeader>();
    List<TaskHeader> nkcrTasks = new ArrayList<TaskHeader>();
    List<TaskHeader> mzkTasks = new ArrayList<TaskHeader>();

    TaskFinder defaultFinder = new TaskFinder();
    defaultFinder.setPackageType(PACKAGE_TYPE_DIGIT_PACKAGE);
    defaultFinder.setOnlyWaiting(true);
    defaultFinder.setOnlyForSystem(true);
    defaultFinder.setError(false);
    TaskFinder nkcrFinder = new TaskFinder();
    nkcrFinder.setPackageType(PACKAGE_TYPE_DIGIT_PACKAGE);
    nkcrFinder.setLocalityCode(LOCALITY_NKCR);
    nkcrFinder.setOnlyWaiting(true);
    nkcrFinder.setOnlyForSystem(true);
    nkcrFinder.setError(false);
    TaskFinder mzkFinder = new TaskFinder();
    mzkFinder.setPackageType(PACKAGE_TYPE_DIGIT_PACKAGE);
    mzkFinder.setLocalityCode(LOCALITY_MZK);
    mzkFinder.setOnlyWaiting(true);
    mzkFinder.setOnlyForSystem(true);
    mzkFinder.setError(false);
    
    
    // package task 1
    PackageTask task1 = new PackageTask();
    task1.setId((long)1);
    task1.setActivity(ACTIVITY_CREATEDIR);
    TaskHeader taskHeader1 = new TaskHeader(task1);
    taskHeader1.setModifyDT("2012-05-31T10:42:35.047+02:00");
    // package task 2
    PackageTask task2 = new PackageTask();
    task2.setId((long)2);
    task2.setActivity(ACTIVITY_PREPAREPP);
    task2.setProfilePP(PROFILEPP_SCANTAILORBW);
    TaskHeader taskHeader2 = new TaskHeader(task2);
    taskHeader2.setModifyDT("2012-05-31T10:42:35.047+02:00");
    // package task 3
    PackageTask task3 = new PackageTask();
    task3.setId((long)3);
    task3.setActivity(ACTIVITY_PREPAREPP);
    TaskHeader taskHeader3 = new TaskHeader(task3);
    taskHeader3.setModifyDT("2012-05-31T10:42:35.047+02:00");
    // import task 4
    IDTask task4 = new IDTask();
    task4.setId((long)4);
    task4.setActivity(ACTIVITY_UPLOAD);
    task4.setImportType(IMPORT_TYPE_MNS);
    TaskHeader taskHeader4 = new TaskHeader(task4);
    taskHeader4.setModifyDT("2012-05-31T10:42:35.047+02:00");
    // entity task 5
    IETask task5 = new IETask();
    task5.setId((long)5);
    task5.setActivity(ACTIVITY_IEPREPARE);
    task5.setSourcePackage(task4.getId());
    task5.setImportType(IMPORT_TYPE_MNS);
    task5.setModifyDT("2012-05-31T10:42:35.047+02:00");
    TaskHeader taskHeader5 = new TaskHeader(task5);    
    taskHeader5.setModifyDT("2012-05-31T10:42:35.047+02:00");
    // entity task 6
    IETask task6 = new IETask();
    task6.setId((long)6);
    task6.setActivity(ACTIVITY_IEPREPARE);
    task6.setSourcePackage(task3.getId());
    TaskHeader taskHeader6 = new TaskHeader(task6);
    taskHeader6.setModifyDT("2012-05-31T10:42:35.047+02:00");
    
    doReturn(task1).when(wfClientMock).getTask(taskHeader1);
    doReturn(task2).when(wfClientMock).getTask(taskHeader2);
    doReturn(task3).when(wfClientMock).getTask(taskHeader3);
    doReturn(task3).when(wfClientMock).getTask((long)3);
    doReturn(task4).when(wfClientMock).getTask(taskHeader4);
    doReturn(task4).when(wfClientMock).getTask((long)4);
    doReturn(task5).when(wfClientMock).getTask(taskHeader5);
    doReturn(task6).when(wfClientMock).getTask(taskHeader6);
    doReturn(new ArrayList<Scan>()).when(wfClientMock).getScans(anyLong());
    
    ProcessMap map = getProcessMap();
    doReturn(map).when(jbpmClientMock).getFreeInstances(true);
    
    
    allTasks.add(taskHeader1);
    nkcrTasks.add(taskHeader1);
    allTasks.add(taskHeader2);
    nkcrTasks.add(taskHeader2);
    allTasks.add(taskHeader3);
    nkcrTasks.add(taskHeader3);
    allTasks.add(taskHeader4);
    mzkTasks.add(taskHeader4);
    allTasks.add(taskHeader6);
    
    int origSize = allTasks.size();
    
    doReturn(new ArrayList<TaskHeader>(allTasks)).when(wfClientMock).getTasks(defaultFinder);
    doReturn(new ArrayList<TaskHeader>(nkcrTasks)).when(wfClientMock).getTasks(nkcrFinder);
    doReturn(new ArrayList<TaskHeader>(mzkTasks)).when(wfClientMock).getTasks(mzkFinder);
    
    u.handleWaitingTasks();
    
    nkcrTasks.remove(taskHeader1);
    allTasks.remove(taskHeader1);
    nkcrTasks.remove(taskHeader2);
    allTasks.remove(taskHeader2);
    
    allTasks.add(taskHeader5);
    mzkTasks.add(taskHeader5);
    
    taskHeader4.setModifyDT("2012-05-31T10:42:36.047+02:00");
    
    int newSize = allTasks.size();

    doReturn(new ArrayList<TaskHeader>(allTasks)).when(wfClientMock).getTasks(defaultFinder);
    doReturn(new ArrayList<TaskHeader>(nkcrTasks)).when(wfClientMock).getTasks(nkcrFinder);
    doReturn(new ArrayList<TaskHeader>(mzkTasks)).when(wfClientMock).getTasks(mzkFinder);

    u.handleWaitingTasks();
  
    // Check total JBPM calls
    verify(jbpmClientMock, times(origSize+newSize)).createProcessInstance(anyString(), (Map<String,String>)anyObject());
    verify(jbpmClientMock, times(origSize+newSize)).startProcessInstance(anyLong());
    verify(wfClientMock, times(5+2)).getTask((TaskHeader)anyObject());
        
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testHandleWaitingTasksLock() throws TransformerException, IOException, BadRequestException, JBPMBusinessException_Exception, JBPMSystemException_Exception {
    File lock = new File(TmConfig.instance().getString("taskHandler.lockFile"));
    if (!lock.exists()) {
      lock.createNewFile();
    }
    
    List<TaskHeader> list = new ArrayList<TaskHeader>();
    
    // package task 1
    PackageTask task1 = new PackageTask();
    task1.setId((long)1);
    task1.setActivity(ACTIVITY_CREATEDIR);
    TaskHeader taskHeader1 = new TaskHeader(task1);
    list.add(taskHeader1);

    doReturn(list).when(wfClientMock).getTasks((TaskFinder) anyObject());
    doReturn(task1).when(wfClientMock).getTask(taskHeader1);
    doReturn(new ArrayList<Scan>()).when(wfClientMock).getScans(anyLong());
    
    u.handleWaitingTasks();
    
    verify(jbpmClientMock, times(0)).createProcessInstance(anyString(), (Map<String,String>)anyObject());
    verify(jbpmClientMock, times(0)).startProcessInstance(anyLong());
    
    lock.delete();
    
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void testHandleWaitingTasksManualLock() throws TransformerException, IOException, BadRequestException, JBPMBusinessException_Exception, JBPMSystemException_Exception {
    File lock = new File(TmConfig.instance().getString("taskHandler.manualLockFile"));
    if (!lock.exists()) {
      lock.createNewFile();
    }
    
    List<TaskHeader> list = new ArrayList<TaskHeader>();
    
    // package task 1
    PackageTask task1 = new PackageTask();
    task1.setId((long)1);
    task1.setActivity(ACTIVITY_CREATEDIR);
    TaskHeader taskHeader1 = new TaskHeader(task1);
    list.add(taskHeader1);

    doReturn(list).when(wfClientMock).getTasks((TaskFinder) anyObject());
    doReturn(task1).when(wfClientMock).getTask(taskHeader1);
    doReturn(new ArrayList<Scan>()).when(wfClientMock).getScans(anyLong());
    
    u.handleWaitingTasks();
    
    verify(jbpmClientMock, times(0)).createProcessInstance(anyString(), (Map<String,String>)anyObject());
    verify(jbpmClientMock, times(0)).startProcessInstance(anyLong());
    
    lock.delete();
    
  }

  @SuppressWarnings("unchecked")
  @Ignore
  public void testProcessInstanceLimit() throws TransformerException, IOException, BadRequestException, JBPMBusinessException_Exception, JBPMSystemException_Exception {
    List<TaskHeader> list = new ArrayList<TaskHeader>();
    
    // package task 1
    PackageTask task1 = new PackageTask();
    task1.setId((long)1);
    task1.setActivity(ACTIVITY_CREATEDIR);
    TaskHeader taskHeader1 = new TaskHeader(task1);
    list.add(taskHeader1);

    // package task 2
    PackageTask task2 = new PackageTask();
    task2.setId((long)2);
    task2.setActivity(ACTIVITY_CREATEDIR);
    TaskHeader taskHeader2 = new TaskHeader(task2);
    list.add(taskHeader2);

    doReturn(list).when(wfClientMock).getTasks((TaskFinder) anyObject());
    doReturn(task1).when(wfClientMock).getTask(taskHeader1);
    doReturn(task2).when(wfClientMock).getTask(taskHeader2);
    doReturn(new ArrayList<Scan>()).when(wfClientMock).getScans(anyLong());
    
    ProcessMap map = new ProcessMap();
    FreeProcess p1 = new FreeProcess();
    p1.setProcessId("prototype.vytvoreni-cdm-digitalizace");
    p1.setCount(1);
    p1.setPriority(1);
    map.getProcess().add(p1);
    doReturn(map).when(jbpmClientMock).getFreeInstances(true);

    u.handleWaitingTasks();
    
    verify(jbpmClientMock, times(1)).createProcessInstance(anyString(), (Map<String,String>)anyObject());
    verify(jbpmClientMock, times(1)).startProcessInstance(anyLong());
    
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
    return map;
  }
}
