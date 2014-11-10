package com.logica.ndk.tm.utilities.transformation.sip2;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.CDMUtilityTest;

@Ignore
public class CreateLogForLTPImportImplTest extends CDMUtilityTest{
  
  private String cdmId = "a161d690-6493-11e2-869e-00505682629d";
  
  @Before
  public void before() throws Exception{
    setUpCdmById(cdmId);
  }
  
  @Ignore 
  public void test(){
    new CreateLogForLTPImportImpl().execute(cdmId, "nkcr", 1523);
  };
  
}
