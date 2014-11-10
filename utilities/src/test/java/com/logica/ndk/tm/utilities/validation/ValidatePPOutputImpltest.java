/**
 * 
 */
package com.logica.ndk.tm.utilities.validation;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.CDMUtilityTest;


/**
 * @author kovalcikm
 *
 */
public class ValidatePPOutputImpltest extends CDMUtilityTest{
  
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
  

//TODO kovalcik  -test not working. CDM in test-data are old. Create and commit new CDM cointaining EM csv.
  @Ignore //(expected=BusinessException.class)
  public void testExecute() {
    ValidationViolationsWrapper response = new ValidatePPOutputImpl().execute("6fcebbb0-dce0-11e1-a45f-00505682629d", true);
    assertThat(response.getViolationsList()).isEmpty();
    
  }
  
}


