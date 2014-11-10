package com.logica.ndk.tm.utilities.integration.wf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.integration.wf.exception.UnknownActivityException;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;

@Ignore
public class TaskHandlerConfigTest {
  
  private static final String PROCESS_ID_OCR = "prototype.ocr";
  private static final String PROCESS_ID_VSTUP_MNS = "import.vstup-manuscriptorium";
  private static final String PROCESS_ID_VSTUP_ANL = "import.vstup-externi";
  private static final String PROCESS_ID_PP_DEFAULT = "prototype.priprav-data-pro-scantailor";
  private static final String PROCESS_ID_VYTVORENI_SIP2 = "ientity.import-dat-do-k4-nkcr";

  @Ignore
  public void testGetParams() {
    assertTrue(TaskHandlerConfig.getParams().size() > 0);
  }

  @Ignore
  public void testGetInputParams() {
    List<String> params = TaskHandlerConfig.getInputParams(PROCESS_ID_OCR);
    assertTrue(params.contains("cdmId"));
  }

  @Ignore
  public void testGetInputParamsComment() {
    List<String> params = TaskHandlerConfig.getInputParams("test");
    assertTrue(params.contains("cdmId"));
  }

  @Ignore
  public void testGetOutputParams() {
    List<String> params = TaskHandlerConfig.getOutputParams(PROCESS_ID_OCR);
    assertTrue(params.contains("ocrRate") && params.contains("taskId"));
  }

  @Ignore
  public void testGetOutputParamsCommon() {
    List<String> params = TaskHandlerConfig.getOutputParams("test");
    assertTrue(params.contains("taskId"));
  }

  @Ignore
  public void testGetFinishSignal() {
    String signal = TaskHandlerConfig.getFinishSignal(PROCESS_ID_OCR);
    assertEquals("NDKSigDigitFinishOCR", signal);
  }

  @Ignore
  public void testGetFinishSignalCommon() {
    String signal = TaskHandlerConfig.getFinishSignal("test");
    assertEquals("NDKSigDigitFinish", signal);
  }

  @Ignore
  public void testGetProcessIdByActivityTypeMns() throws UnknownActivityException {
      String processId = TaskHandlerConfig.getProcessIdByActivity("IEPREPARE", "MNS");
      assertEquals(PROCESS_ID_VSTUP_MNS, processId);
  }
  
  @Ignore
  public void testGetProcessIdByActivityTypeAnl() throws UnknownActivityException {
      String processId = TaskHandlerConfig.getProcessIdByActivity("IEPREPARE", "PICTURES");
      assertEquals(PROCESS_ID_VSTUP_ANL, processId);
  }

  @Ignore
  public void testGetProcessIdByActivity() throws UnknownActivityException {
      String processId = TaskHandlerConfig.getProcessIdByActivity("OCR", null);
      assertEquals(PROCESS_ID_OCR, processId);
  }

  @Ignore
  public void testGetProcessIdByActivityTypeNull() throws UnknownActivityException {
      String processId = TaskHandlerConfig.getProcessIdByActivity("PREPAREPP", null);
      assertEquals(PROCESS_ID_PP_DEFAULT, processId);
  }

  @Ignore
  public void testGetProcessIdByActivityNonExisting() throws UnknownActivityException {
    String processId = TaskHandlerConfig.getProcessIdByActivity("PREPAREPP", "test");
    assertEquals(PROCESS_ID_PP_DEFAULT, processId);
  }

  @Ignore
  public void testGetPakcageType() {
    String packageType = TaskHandlerConfig.getPackageType(PROCESS_ID_OCR);
    assertEquals(WFClient.PACKAGE_TYPE_PACKAGE, packageType);
  }

  @Ignore
  public void testGetPakcageTypeCommon() {
    String packageType = TaskHandlerConfig.getPackageType("test");
    assertEquals(WFClient.PACKAGE_TYPE_PACKAGE, packageType);
  }
  
  @Ignore
  public void testInstanceLimit() {
    String instances = TaskHandlerConfig.getInstanceLimit(PROCESS_ID_VYTVORENI_SIP2);
    assertEquals("1", instances);
  }

  @Ignore
  public void testInstanceLimitCommon() {
    String instances = TaskHandlerConfig.getInstanceLimit("test");
    assertEquals("-1", instances);
  }

  @Ignore
  public void testTimeout() {
    String timeout = TaskHandlerConfig.getTimeout(PROCESS_ID_VYTVORENI_SIP2);
    assertEquals("3600000", timeout);
  }

  @Ignore
  public void testTimeoutCommon() {
    String instances = TaskHandlerConfig.getTimeout("test");
    assertEquals("0", instances);
  }

//  @Ignore
//  public void testGetProperty() {
//    String value = TaskHandlerConfig.getProperty("consts/activity-ie-import");
//    assertEquals("IEPREPARE", value);
//  }

  @Ignore
  public void testGetActivityTypeDefinition() throws UnknownActivityException {
      String typeDef = TaskHandlerConfig.getActivityTypeDefinition("IEPREPARE");
      assertEquals("importType.code", typeDef);
  }

  @Ignore
  public void testGetActivityTypeDefinitionNull() throws UnknownActivityException {
      String typeDef = TaskHandlerConfig.getActivityTypeDefinition("CREATEDIR");
      assertNull(typeDef);
  }

  @Ignore
  public void testGetIgnoredTasks() {
    List<String> ignored = TaskHandlerConfig.getIgnoredTasks();
    assertNotNull(ignored);
    assertTrue(ignored.size() > 0);
  }

}
