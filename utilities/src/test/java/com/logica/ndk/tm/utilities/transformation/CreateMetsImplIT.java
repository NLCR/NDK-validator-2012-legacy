package com.logica.ndk.tm.utilities.transformation;

import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.cdm.CDM;

public class CreateMetsImplIT {
  @Test
  public void test() {
    CDM cdm = new CDM();
    System.out.println(cdm.getCdmDataDir("11f3ac10-ab67-11e3-a283-00505682629d").getAbsolutePath());
    
    String cdmId = "11f3ac10-ab67-11e3-a283-00505682629d";
    new CreateMetsImpl().execute(cdmId);
  }
}
