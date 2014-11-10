package com.logica.ndk.tm.utilities.transformation.sip1;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.transformation.sip1.exception.SIP1ImportFailedException;

@Ignore
public class CheckSIP1ImportResultTest {
  File importDir;
  private static final String CDM_ID1 = "sip1test";
  private static final String CDM_ID2 = "sip1test_2";
  
  @Before
  public void setUp() {
    
    importDir = new File(TmConfig.instance().getString("utility.sip1.import-dir"));
    try {
      FileUtils.forceMkdir(importDir);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  @After
  public void tearDown() {
    try {
      FileUtils.deleteDirectory(importDir);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    
  }
  
  
  @Test(expected = SIP1ImportFailedException.class)
  public void testExecuteMissing() {
    CheckSIP1ImportResultImpl u = new CheckSIP1ImportResultImpl();
    u.execute(CDM_ID1);
  }

  @Test
  public void testExecuteComplete() throws IOException {
    CheckSIP1ImportResultImpl u = new CheckSIP1ImportResultImpl();
    File itemDir = new File(importDir.getAbsolutePath() + "/" + SIP1ImportConsts.SIP_STATUS_COMPLETE + "_" + SIP1ImportConsts.SIP_CDM_PREFIX + CDM_ID1);
    FileUtils.forceMkdir(itemDir);
    String status = u.execute(CDM_ID1);
    assertEquals(SIP1ImportConsts.SIP_STATUS_COMPLETE, status);
    FileUtils.deleteDirectory(itemDir);
  }

  @Test
  public void testExecuteComplete2() throws IOException {
    CheckSIP1ImportResultImpl u = new CheckSIP1ImportResultImpl();
    File itemDir = new File(importDir.getAbsolutePath() + "/" + SIP1ImportConsts.SIP_STATUS_COMPLETE + "_" + SIP1ImportConsts.SIP_CDM_PREFIX + CDM_ID2);
    FileUtils.forceMkdir(itemDir);
    String status = u.execute(CDM_ID2);
    assertEquals(SIP1ImportConsts.SIP_STATUS_COMPLETE, status);
    FileUtils.deleteDirectory(itemDir);
  }

  @Test
  public void testExecutePending() throws IOException {
    CheckSIP1ImportResultImpl u = new CheckSIP1ImportResultImpl();
    File itemDir = new File(importDir.getAbsolutePath() + "/" + SIP1ImportConsts.SIP_STATUS_PENDING + "_" + SIP1ImportConsts.SIP_CDM_PREFIX + CDM_ID1);
    FileUtils.forceMkdir(itemDir);
    String status = u.execute(CDM_ID1);
    assertEquals(SIP1ImportConsts.SIP_STATUS_PENDING, status);
    FileUtils.deleteDirectory(itemDir);
  }

  @Test
  public void testExecuteArbitrary() throws IOException {
    String whatever = "whatever";
    CheckSIP1ImportResultImpl u = new CheckSIP1ImportResultImpl();
    File itemDir = new File(importDir.getAbsolutePath() + "/" + whatever + "_" + SIP1ImportConsts.SIP_CDM_PREFIX + CDM_ID1);
    FileUtils.forceMkdir(itemDir);
    String status = u.execute(CDM_ID1);
    assertEquals(whatever, status);
    FileUtils.deleteDirectory(itemDir);
  }

  @Test(expected = SIP1ImportFailedException.class)
  public void testExecuteError() throws IOException {
    CheckSIP1ImportResultImpl u = new CheckSIP1ImportResultImpl();
    File itemDir = new File(importDir.getAbsolutePath() + "/" + SIP1ImportConsts.SIP_STATUS_ERROR + "_" + SIP1ImportConsts.SIP_CDM_PREFIX + CDM_ID1);
    FileUtils.forceMkdir(itemDir);
    String status = u.execute(CDM_ID1);
    assertEquals(SIP1ImportConsts.SIP_STATUS_ERROR, status);
    FileUtils.deleteDirectory(itemDir);
  }
  
}
