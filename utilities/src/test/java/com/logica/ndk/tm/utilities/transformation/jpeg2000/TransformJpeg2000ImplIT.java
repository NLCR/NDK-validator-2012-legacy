package com.logica.ndk.tm.utilities.transformation.jpeg2000;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.CDMUtilityTest;

public class TransformJpeg2000ImplIT extends CDMUtilityTest {
  String cdmId = "validatemets";
  CDM cdm = new CDM();
  
  @Before
  public void setUp() throws Exception {
    setUpCdmById(cdmId);
  }
  
  @After
  public void tearDown() throws Exception {
    deleteCdmById(cdmId);
  }

  @Ignore
  public void testExecute() {
    Integer result = new TransformJpeg2000Impl().execute(
      cdmId, 
      cdm.getMasterCopyDir(cdmId).getAbsolutePath(), 
      cdm.getMasterCopyDir(cdmId).getAbsolutePath(), 
      "reduced", 
      "");
    
    assertEquals(new Integer(2), result);
  }
}