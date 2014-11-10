/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMBagItHelper;
import com.logica.ndk.tm.utilities.CDMUtilityTest;

/**
 * @author kovalcikm
 */
public class CreateMD5FileImplTest extends CDMUtilityTest {

  @Before
  public void setUp() throws Exception {
    //setUpCdmById("common");
  }

  @Test
  public void testExecute() {
    CreateMD5FileImpl createMd5 = new CreateMD5FileImpl();
    createMd5.execute("common");
  }

}
