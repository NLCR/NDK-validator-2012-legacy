/**
 * 
 */
package com.logica.ndk.tm.utilities.validation;

import org.dom4j.DocumentException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.utilities.CDMUtilityTest;

/**
 * @author kovalcikm
 *
 */
@Ignore
public class ValidateMixImplTest extends CDMUtilityTest {
  
  private static String CDM_ID = "7d599d90-9220-11e2-9a08-005056827e52";
//   @Before
//   public void setUp() throws Exception{
  // will be implemented after update of test-data
//    setUpCdmById(CDM_ID);
//   }
   
  @Ignore
   public void test() throws CDMException, DocumentException{
     ValidateMixImpl validateMix = new ValidateMixImpl();
     validateMix.execute(CDM_ID, "flatData", "mix-ps", true);
     validateMix.execute(CDM_ID, "masterCopy", "mix-mc", true);
   }

}
