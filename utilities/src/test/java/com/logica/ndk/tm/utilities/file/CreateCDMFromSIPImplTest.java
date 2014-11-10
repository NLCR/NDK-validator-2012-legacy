package com.logica.ndk.tm.utilities.file;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.cdm.CDM;


public class CreateCDMFromSIPImplTest {
  
  private static final String cdmId = "08e687d0-dc6c-11e1-8357-00505682629d";
  private static final String pathToSIP1 = "test-data\\ltp\\done_CDM_" + cdmId;
  private static CDM cdm = new CDM();
  
  
  @Before
  public void setUp() throws IOException {
    File cdmDir = cdm.getCdmDir(cdmId);
    FileUtils.deleteDirectory(cdmDir);
  }
  
  @After
  public void tearDown() throws IOException {
    File cdmDir = cdm.getCdmDir(cdmId);
    FileUtils.deleteDirectory(cdmDir);
  }
  
  @Ignore
  public void test() {
    CreateCDMFromSIPImpl cdmFromSIP1Impl = new CreateCDMFromSIPImpl();
    String newCdmId = cdmFromSIP1Impl.execute(pathToSIP1,"test");
    assertThat(newCdmId).isEqualTo(cdmId);
    assertThat(cdm.getMasterCopyDir(newCdmId).listFiles().length).isGreaterThan(0);
    assertThat(cdm.getUserCopyDir(newCdmId).listFiles().length).isGreaterThan(0);
  }
  
}
