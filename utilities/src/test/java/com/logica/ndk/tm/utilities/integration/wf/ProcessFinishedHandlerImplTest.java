package com.logica.ndk.tm.utilities.integration.wf;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.commons.utils.test.TestUtils;
import com.logica.ndk.tm.process.ParamMap;
import com.logica.ndk.tm.process.ParamMapItem;
import com.logica.ndk.tm.process.ProcessState;
import com.logica.ndk.tm.utilities.ProcessParams;
import com.logica.ndk.tm.utilities.integration.wf.exception.BadRequestException;
import com.logica.ndk.tm.utilities.integration.wf.exception.WFConnectionUnavailableException;
import com.logica.ndk.tm.utilities.integration.wf.finishedTask.FinishedTask;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;

@Ignore
public class ProcessFinishedHandlerImplTest {

  private final WFClient wfClientMock = mock(WFClient.class);
  private final ProcessFinishedHandler u = new ProcessFinishedHandlerImpl();
  private final String runtimeConfigFile = "test-data/process-finished-handler.properties";

  @Before
  public void setUp() throws Exception {
    TestUtils.setField(u, "wfClient", wfClientMock);
    TestUtils.setField(u, "runtimeConfigFile", runtimeConfigFile);
    Map<Long,Integer> runCounter = new HashMap<Long,Integer>();
    runCounter.put(1l, 1);
    TestUtils.setField(u, "runCounter", runCounter);
  }

  @After
  public void tearDown() throws Exception {
    reset(wfClientMock);
  }

  @Test
  public void testHandleFinishedProcessBusinessException() throws WFConnectionUnavailableException, BadRequestException, TransformerException, IOException {
    ProcessState state = new ProcessState();
    state.setProcessId("test");
    state.setInstanceId(1);
    state.setState(ProcessFinishedHandlerImpl.PROCESS_STATE_ERROR);
    ParamMap map = new ParamMap();
    // Localized message
    ParamMapItem param1 = new ParamMapItem();
    param1.setName(ProcessParams.PARAM_NAME_EX_HANDLER_EX_MESSAGE_LOCAL);
    param1.setValue("Poznamka");
    map.getItems().add(param1);
    
    state.setParameters(map);
    
    u.handleFinishedProcess(state);
    FinishedTask expectedFT = new FinishedTask();
    List<String> errorMessages = new ArrayList<String>();
    errorMessages.add("Poznamka\r\n");
    expectedFT.setErrorMessages(errorMessages);
    expectedFT.setError(true);
    
    verify(wfClientMock, times(1)).signalFinishedTask((FinishedTask)argThat(new FinishedTaskMatcher(expectedFT)), anyString());

  }

  // Rerun disabled by config
  @Test
  public void testHandleFinishedProcessRerunNotAllowed1() throws WFConnectionUnavailableException, BadRequestException, TransformerException, IOException {
    ProcessState state = new ProcessState();
    state.setProcessId("test1");
    state.setInstanceId(1);
    state.setState(ProcessFinishedHandlerImpl.PROCESS_STATE_ERROR);
    ParamMap map = new ParamMap();
    // Localized message
    ParamMapItem param1 = new ParamMapItem();
    param1.setName(ProcessParams.PARAM_NAME_EX_HANDLER_EX_MESSAGE_LOCAL);
    param1.setValue("Poznamka");
    map.getItems().add(param1);
    // TaskId
    ParamMapItem param2 = new ParamMapItem();
    param2.setName(ProcessParams.PARAM_NAME_TASK_ID);
    param2.setValue("0");
    map.getItems().add(param2);
    
    state.setParameters(map);
    
    u.handleFinishedProcess(state);
    FinishedTask expectedFT = new FinishedTask();
    List<String> errorMessages = new ArrayList<String>();
    errorMessages.add("Poznamka\r\n");
    expectedFT.setErrorMessages(errorMessages);
    expectedFT.setError(true);
    
    verify(wfClientMock, times(1)).signalFinishedTask((FinishedTask)argThat(new FinishedTaskMatcher(expectedFT)), anyString());
  }

  // Rerun disabled due to parsing
  @Test
  public void testHandleFinishedProcessRerunNotAllowed2() throws WFConnectionUnavailableException, BadRequestException, TransformerException, IOException {
    ProcessState state = new ProcessState();
    state.setProcessId("test3");
    state.setInstanceId(1);
    state.setState(ProcessFinishedHandlerImpl.PROCESS_STATE_ERROR);
    ParamMap map = new ParamMap();
    // Localized message
    ParamMapItem param1 = new ParamMapItem();
    param1.setName(ProcessParams.PARAM_NAME_EX_HANDLER_EX_MESSAGE_LOCAL);
    param1.setValue("Poznamka");
    map.getItems().add(param1);
    // TaskId
    ParamMapItem param2 = new ParamMapItem();
    param2.setName(ProcessParams.PARAM_NAME_TASK_ID);
    param2.setValue("0");
    map.getItems().add(param2);
    
    state.setParameters(map);
    
    u.handleFinishedProcess(state);
    FinishedTask expectedFT = new FinishedTask();
    List<String> errorMessages = new ArrayList<String>();
    errorMessages.add("Poznamka\r\n");
    expectedFT.setErrorMessages(errorMessages);
    expectedFT.setError(true);
    
    verify(wfClientMock, times(1)).signalFinishedTask((FinishedTask)argThat(new FinishedTaskMatcher(expectedFT)), anyString());
  }

  // Rerun disabled due to limit
  @Test
  public void testHandleFinishedProcessRerunNotAllowed3() throws WFConnectionUnavailableException, BadRequestException, TransformerException, IOException {
    ProcessState state = new ProcessState();
    state.setProcessId("test2");
    state.setInstanceId(1);
    state.setState(ProcessFinishedHandlerImpl.PROCESS_STATE_ERROR);
    ParamMap map = new ParamMap();
    // Localized message
    ParamMapItem param1 = new ParamMapItem();
    param1.setName(ProcessParams.PARAM_NAME_EX_HANDLER_EX_MESSAGE_LOCAL);
    param1.setValue("Poznamka");
    map.getItems().add(param1);
    // TaskId
    ParamMapItem param2 = new ParamMapItem();
    param2.setName(ProcessParams.PARAM_NAME_TASK_ID);
    param2.setValue("1");
    map.getItems().add(param2);
    
    state.setParameters(map);
    
    u.handleFinishedProcess(state);
    FinishedTask expectedFT = new FinishedTask();
    List<String> errorMessages = new ArrayList<String>();
    errorMessages.add("Poznamka\r\n");
    expectedFT.setErrorMessages(errorMessages);
    expectedFT.setError(true);
    
    verify(wfClientMock, times(1)).signalFinishedTask((FinishedTask)argThat(new FinishedTaskMatcher(expectedFT)), anyString());
  }

  // Rerun enabled by limit - no rerun yet
  @Test
  public void testHandleFinishedProcessRerunAllowed1() throws WFConnectionUnavailableException, BadRequestException, TransformerException, IOException {
    ProcessState state = new ProcessState();
    state.setProcessId("test2");
    state.setInstanceId(1);
    state.setState(ProcessFinishedHandlerImpl.PROCESS_STATE_ERROR);
    ParamMap map = new ParamMap();
    // Localized message
    ParamMapItem param1 = new ParamMapItem();
    param1.setName(ProcessParams.PARAM_NAME_EX_HANDLER_EX_MESSAGE_LOCAL);
    param1.setValue("Poznamka");
    map.getItems().add(param1);
    // TaskId
    ParamMapItem param2 = new ParamMapItem();
    param2.setName(ProcessParams.PARAM_NAME_TASK_ID);
    param2.setValue("2");
    map.getItems().add(param2);
    
    state.setParameters(map);
    
    u.handleFinishedProcess(state);
    FinishedTask expectedFT = new FinishedTask();
    expectedFT.setError(false);
    
    verify(wfClientMock, times(1)).signalFinishedTask((FinishedTask)argThat(new FinishedTaskMatcher(expectedFT)), anyString());
  }

  // Rerun enabled by limit - 1 rerun already
  @Test
  public void testHandleFinishedProcessRerunAllowed2() throws WFConnectionUnavailableException, BadRequestException, TransformerException, IOException {
    ProcessState state = new ProcessState();
    state.setProcessId("test4");
    state.setInstanceId(1);
    state.setState(ProcessFinishedHandlerImpl.PROCESS_STATE_ERROR);
    ParamMap map = new ParamMap();
    // Localized message
    ParamMapItem param1 = new ParamMapItem();
    param1.setName(ProcessParams.PARAM_NAME_EX_HANDLER_EX_MESSAGE_LOCAL);
    param1.setValue("Poznamka");
    map.getItems().add(param1);
    // TaskId
    ParamMapItem param2 = new ParamMapItem();
    param2.setName(ProcessParams.PARAM_NAME_TASK_ID);
    param2.setValue("1");
    map.getItems().add(param2);
    
    state.setParameters(map);
    
    u.handleFinishedProcess(state);
    FinishedTask expectedFT = new FinishedTask();
    expectedFT.setError(false);
    
    verify(wfClientMock, times(1)).signalFinishedTask((FinishedTask)argThat(new FinishedTaskMatcher(expectedFT)), anyString());
  }

  // Rerun enabled by default - 1 rerun already
  @Test
  public void testHandleFinishedProcessRerunAllowed3() throws WFConnectionUnavailableException, BadRequestException, TransformerException, IOException {
    ProcessState state = new ProcessState();
    state.setProcessId("testX");
    state.setInstanceId(1);
    state.setState(ProcessFinishedHandlerImpl.PROCESS_STATE_ERROR);
    ParamMap map = new ParamMap();
    // Localized message
    ParamMapItem param1 = new ParamMapItem();
    param1.setName(ProcessParams.PARAM_NAME_EX_HANDLER_EX_MESSAGE_LOCAL);
    param1.setValue("Poznamka");
    map.getItems().add(param1);
    // TaskId
    ParamMapItem param2 = new ParamMapItem();
    param2.setName(ProcessParams.PARAM_NAME_TASK_ID);
    param2.setValue("1");
    map.getItems().add(param2);
    
    state.setParameters(map);
    
    u.handleFinishedProcess(state);
    FinishedTask expectedFT = new FinishedTask();
    expectedFT.setError(false);
    
    verify(wfClientMock, times(1)).signalFinishedTask((FinishedTask)argThat(new FinishedTaskMatcher(expectedFT)), anyString());
  }

  // Rerun denied due to missing property
  @Test
  public void testHandleFinishedProcessRerunNotAllowed4() throws WFConnectionUnavailableException, BadRequestException, TransformerException, IOException {
    TestUtils.setField(u, "runtimeConfigFile", "xxx");

    ProcessState state = new ProcessState();
    state.setProcessId("testX");
    state.setInstanceId(1);
    state.setState(ProcessFinishedHandlerImpl.PROCESS_STATE_ERROR);
    ParamMap map = new ParamMap();
    // Localized message
    ParamMapItem param1 = new ParamMapItem();
    param1.setName(ProcessParams.PARAM_NAME_EX_HANDLER_EX_MESSAGE_LOCAL);
    param1.setValue("Poznamka");
    map.getItems().add(param1);
    // TaskId
    ParamMapItem param2 = new ParamMapItem();
    param2.setName(ProcessParams.PARAM_NAME_TASK_ID);
    param2.setValue("1");
    map.getItems().add(param2);
    
    state.setParameters(map);
    
    u.handleFinishedProcess(state);
    FinishedTask expectedFT = new FinishedTask();
    List<String> errorMessages = new ArrayList<String>();
    errorMessages.add("Poznamka\r\n");
    expectedFT.setErrorMessages(errorMessages);
    expectedFT.setError(true);
    
    verify(wfClientMock, times(1)).signalFinishedTask((FinishedTask)argThat(new FinishedTaskMatcher(expectedFT)), anyString());
  }
}
