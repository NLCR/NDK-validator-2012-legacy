package com.logica.ndk.tm.utilities.file;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.CDMUtilityTest;
import com.logica.ndk.tm.utilities.validation.ValidationViolationsWrapper;
import com.logica.ndk.tm.utilities.file.ValidatePSPScanImpl;

public class ValidatePSPScanImplIT extends CDMUtilityTest{
  
  private static File tmpDir;
  
//  @BeforeClass
//  public static void setUpBeforeClass() {
//    tmpDir = new File(FileUtils.getTempDirectory()+File.separator+"cdm"+File.separator, "CDM_"+ValidatePSPScanImplIT.class.getSimpleName());
//    tmpDir.mkdirs();
//  }
//  
//  @Before
//  public void setUp() throws Exception{
//    setUpCdmById("common");
//  }
  
  @Ignore
  public void testExecute() {
    ValidationViolationsWrapper response = new ValidatePSPScanImpl().execute("916304d0-1541-11e3-84b9-00505682629d", true);
    assertThat(response.getViolationsList()).isEmpty();
    
  }
  
//  @Test (expected=BusinessException.class)
//  public void testDifferentMD5() throws Exception{
//    CDM cdm = new CDM();
//    FileUtils.copyDirectory(cdm.getCdmDir("common"), tmpDir);
//    System.out.println(cdm.getRawDataDir(ValidatePSPScanImplIT.class.getSimpleName())+File.separator+"1"+File.separator+"1000350721_00_0004.tif");
//    FileUtils.writeByteArrayToFile(new File(cdm.getRawDataDir(ValidatePSPScanImplIT.class.getSimpleName())+File.separator+"1"+File.separator+"1000350721_00_0004.tif"), new byte[]{4,4,4,4,4}, true);
//    
//    ValidationViolationsWrapper response = new ValidatePSPScanImpl().execute(ValidatePSPScanImplIT.class.getSimpleName(),true);
//    assertThat(response.getViolationsList()).isEmpty();
//  }
  
}
