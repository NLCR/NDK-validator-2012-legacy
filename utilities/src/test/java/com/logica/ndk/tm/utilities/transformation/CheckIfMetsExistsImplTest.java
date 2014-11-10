/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

import static org.fest.assertions.Assertions.assertThat;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import com.logica.ndk.tm.utilities.CDMUtilityTest;

/**
 * @author kovalcikm
 *
 */
public class CheckIfMetsExistsImplTest extends CDMUtilityTest{

  @Before
  public void setUp() throws Exception{
    setUpCdmById(CDM_ID_COMMON);
  }
  
  @Test
  public void test(){
    String response = new CheckIfMetsExistsImpl().execute(CDM_ID_COMMON);
    assertThat(response).isEqualTo("OK");
  }
  
  @Test 
  public void testNoMets(){
    FileUtils.deleteQuietly(cdm.getMetsFile(CDM_ID_COMMON));
    String response = new CheckIfMetsExistsImpl().execute(CDM_ID_COMMON);
    assertThat(response).isEqualTo("OK");
  }
  
}
