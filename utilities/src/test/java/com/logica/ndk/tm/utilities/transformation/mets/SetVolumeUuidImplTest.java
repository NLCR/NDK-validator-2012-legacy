/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.mets;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.CDMUtilityTest;
import com.logica.ndk.tm.utilities.ResponseStatus;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author kovalcikm
 *
 */
@Ignore
public class SetVolumeUuidImplTest extends CDMUtilityTest{
  
  @Before
  public void setUpCdmById() throws Exception {
      setUpCdmById("validatemets_periodical");
      setUpCdmById("common");
  }
  
  @Ignore
  public void test(){
    String response = new SetVolumeUuidImpl().execute("empty");
    assertThat(response).isEqualTo(ResponseStatus.RESPONSE_OK);
  }
  
  @Ignore//(expected=BusinessException.class)
  public void testWrongMets(){
    String response = new SetVolumeUuidImpl().execute("common");
  }

}
