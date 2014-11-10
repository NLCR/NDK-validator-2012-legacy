/**
 * 
 */
package com.logica.ndk.tm.utilities.validation;

import javax.transaction.SystemException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.CDMUtilityTest;

/**
 * @author kovalcikm
 *
 */
@Ignore
public class ValidateAlephBiblioMetadataImplTest extends CDMUtilityTest{
  
  private static String CDM_ID = "efbcd560-fbe8-11e1-8fc7-00505682629d";
  
  @Before
  public void beforeTest() throws Exception{
    setUpCdmById(CDM_ID);
  }
  
  @Ignore
  public void throwTest() throws SystemException{
    ValidationViolationsWrapper result = new ValidateAlephBiblioMetadataImpl().execute(CDM_ID, true);
  }
}
