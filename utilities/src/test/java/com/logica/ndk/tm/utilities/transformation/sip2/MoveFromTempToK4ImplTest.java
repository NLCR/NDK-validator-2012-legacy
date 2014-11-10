package com.logica.ndk.tm.utilities.transformation.sip2;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.CDMUtilityTest;

@Ignore
public class MoveFromTempToK4ImplTest extends CDMUtilityTest{

  private static String CMD_ID = "pokus";
  
  @Before
  public void before() throws Exception{
    //setUpCdmById(CMD_ID);
  }
  
  @Ignore
  public void test(){
    new MoveFromTempToK4Impl().execute(CMD_ID , "mzk");
  }

}
