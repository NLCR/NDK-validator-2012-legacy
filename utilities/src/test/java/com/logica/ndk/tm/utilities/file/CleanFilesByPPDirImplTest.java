package com.logica.ndk.tm.utilities.file;

import org.junit.Before;
import org.junit.Test;

import com.logica.ndk.tm.utilities.CDMUtilityTest;

public class CleanFilesByPPDirImplTest extends CDMUtilityTest{
  
  private static String CDM_ID = CDM_ID_COMMON;
  
 @Before
 public void beforeTest() throws Exception{
   setUpCdmById(CDM_ID);
 }
 
 @Test
 public void test(){
   new CleanFilesByPPDirImpl().execute(CDM_ID);
 }

}
