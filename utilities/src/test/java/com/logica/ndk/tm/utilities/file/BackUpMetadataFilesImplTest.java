package com.logica.ndk.tm.utilities.file;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.CDMUtilityTest;

@Ignore
public class BackUpMetadataFilesImplTest extends CDMUtilityTest{

  private static final String CDM_ID = "b4fc2eb0-2e30-11e2-b9cc-00505682629d";
  
  @Before
  public void beforeTest() throws Exception{
    setUpCdmById(CDM_ID);
  }
  
  @Ignore
  public void test(){
    new BackUpMetadataFilesImpl().execute(CDM_ID);
  }

}
