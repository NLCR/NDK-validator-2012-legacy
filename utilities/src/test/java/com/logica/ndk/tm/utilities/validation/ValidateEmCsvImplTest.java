package com.logica.ndk.tm.utilities.validation;

import org.junit.Before;
import org.junit.Test;

import com.logica.ndk.tm.utilities.CDMUtilityTest;

public class ValidateEmCsvImplTest extends CDMUtilityTest{
  
  private static String CDM_ID = "split";
  
  @Before
  public void before() throws Exception{
    setUpCdmById(CDM_ID);
  }
  
  @Test
  public void test(){
    new ValidateEmCsvImpl().validate(CDM_ID, false);
  }
  
}
