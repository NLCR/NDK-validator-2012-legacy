/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.CDMUtilityTest;

/**
 * @author kovalcikm
 */
@Ignore
public class CreateOriginalDataImplTest extends CDMUtilityTest {
  CDM cdm = new CDM();
  @Before
  public void setUp() throws Exception {
    setUpCdmById(CDM_ID_COMMON);
  }

  @After
  public void after() throws Exception{
    deleteCdmById(CDM_ID_COMMON);
  }
  
  @Ignore
  public void test() {
    String response = new CreateOriginalDataImpl().execute(CDM_ID_COMMON);
    assertThat(response).isEqualTo("OK");
    File file = new File(cdm.getOriginalDataDir(CDM_ID_COMMON)+File.separator+"originalData.zip");
    assertThat(file).exists();
    assertThat(file.length()>0);
  }
  
 

}
