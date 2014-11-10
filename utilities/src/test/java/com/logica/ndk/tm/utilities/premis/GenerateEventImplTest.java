/**
 * 
 */
package com.logica.ndk.tm.utilities.premis;

import org.junit.Before;
import org.junit.Test;
import static org.fest.assertions.Assertions.assertThat;

import com.logica.ndk.tm.utilities.CDMUtilityTest;
import com.logica.ndk.tm.utilities.ResponseStatus;

/**
 * @author kovalcikm
 */
public class GenerateEventImplTest extends CDMUtilityTest {

  @Before
  public void setUp() throws Exception {
    setUpCdmById(CDM_ID_COMMON);
  }

  @Test
  public void test(){
    String response = new GenerateEventImpl().execute(CDM_ID_COMMON, cdm.getPostprocessingDataDir(CDM_ID_COMMON).getAbsolutePath(), "deletion_ps_deletion", "testAgent", "testVersion", "testRole", "testDesignationName", "testRegisterKey", "testPreservationLevel", "*.tif");
    assertThat(response).isEqualTo(ResponseStatus.RESPONSE_OK);
  }
}
