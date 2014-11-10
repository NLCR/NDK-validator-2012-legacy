package com.logica.ndk.tm.utilities.integration.jbpm;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.logica.ndk.commons.utils.test.TestUtils;
import com.logica.ndk.tm.utilities.integration.wf.exception.BadRequestException;
import com.logica.ndk.tm.utilities.integration.wf.task.Scan;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.jbpm.JBPMBusinessException_Exception;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.jbpm.JBPMSystemException_Exception;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.jbpm.JBPMWSFacadeClient;

public class EventTrigerImplTest {

  private final JBPMWSFacadeClient jbpmClientMock = mock(JBPMWSFacadeClient.class);
  private final EventTrigerImpl u = new EventTrigerImpl();
  
  private static final String PROCESS_ID_OCR = "prototype.ocr";
  private static final String PROCESS_ID_K4_NKCR = "ientity.import-dat-do-k4-nkcr";
  private static final String PROCESS_ID_K4_MZK = "ientity.import-dat-do-k4-mzk";
  
  private static final String TIMER_OCR = "ocr-timer";
  private static final String TIMER_SIP2_IMPORT = "sip2-import-timer";
  private static final String TIMER_SIP2_INDEX = "sip2-index-timer";
  
  
  
  @Before
  public void setUp() throws Exception {
    TestUtils.setField(u, "jbpmClient", jbpmClientMock);
  }

  @After
  public void tearDown() throws Exception {
    reset(jbpmClientMock);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testHandleWaitingTasks() throws TransformerException, IOException, BadRequestException, JBPMBusinessException_Exception, JBPMSystemException_Exception {
    /*
    List<Long> ocrInstances = new ArrayList<Long>();
    ocrInstances.add((long)1);
    ocrInstances.add((long)2);
    
    List<Long> k4NKCInstances = new ArrayList<Long>();
    k4NKCInstances.add((long)3);
    k4NKCInstances.add((long)4);
    
    List<Long> k4MZKInstances = new ArrayList<Long>();
    k4MZKInstances.add((long)5);
    k4MZKInstances.add((long)6);
    
    List<Long> k4Instances = new ArrayList<Long>();
    k4Instances.addAll(k4NKCInstances);
    k4Instances.addAll(k4MZKInstances);    
    
    doReturn(ocrInstances).when(jbpmClientMock).getActiveInstances(PROCESS_ID_OCR);
    doReturn(k4NKCInstances).when(jbpmClientMock).getActiveInstances(PROCESS_ID_K4_NKCR);
    doReturn(k4MZKInstances).when(jbpmClientMock).getActiveInstances(PROCESS_ID_K4_MZK);
    
    u.execute(TIMER_OCR, null);
    u.execute(TIMER_SIP2_IMPORT, null);
    u.execute(TIMER_SIP2_INDEX, null);
    
    // Check total JBPM calls
    verify(jbpmClientMock, times(ocrInstances.size())).signalEventForInstance(anyLong(), eq(TIMER_OCR), anyString());
    verify(jbpmClientMock, times(k4Instances.size())).signalEventForInstance(anyLong(), eq(TIMER_SIP2_IMPORT), anyString());
    verify(jbpmClientMock, times(k4Instances.size())).signalEventForInstance(anyLong(), eq(TIMER_SIP2_INDEX), anyString());
    */
  }

}
