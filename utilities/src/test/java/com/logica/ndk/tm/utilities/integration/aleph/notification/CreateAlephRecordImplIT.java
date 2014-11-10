package com.logica.ndk.tm.utilities.integration.aleph.notification;

import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.CDMUtilityTest;

public class CreateAlephRecordImplIT extends CDMUtilityTest{
  
 // private String cdm_id = "009497f0-1c4b-11e3-917c-00505682629d-13793206922341402373802";
  
 // @Before
//  public void before() throws Exception{
//    setUpCdmById(cdm_id);
//  }

  @Ignore
  public void test() {
    String docNumber = "1001905695";

    final String multipartMonography = "69f2f630-19f1-11e3-8c37-00505682629d";
    final String monography = "0aa4c2e0-1a2c-11e3-8679-00505682629d";
    final String periodic = "009497f0-1c4b-11e3-917c-00505682629d";
  //  new CreateAlephRecordImpl().execute(cdm_id, docNumber, "mzk");
 //   docNumber = "000358464";
    //String cdmId = "69f2f630-19f1-11e3-8c37-00505682629d";
   // String cdmId = "009497f0-1c4b-11e3-917c-00505682629d";
   // String cdmId = "0aa4c2e0-1a2c-11e3-8679-00505682629d";
    new CreateAlephRecordImpl().execute(multipartMonography, docNumber, "mzk");
  }
}
