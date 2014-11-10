/**
 * 
 */
package com.logica.ndk.tm.utilities.premis;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.CDMUtilityTest;
import com.logica.ndk.tm.utilities.ResponseStatus;

/**
 * @author kovalcikm
 */
@Ignore
public class GenerateEventFormatMigrationImplTest extends CDMUtilityTest {

  private static String CDM_ID = "aabbe280-139d-11e3-831a-00505682629d";

  @Ignore
  public void test() {
    String response = new GenerateEventFormatMigrationImpl().execute(CDM_ID);
    assertThat(response).isEqualTo(ResponseStatus.RESPONSE_OK);
  }
}
