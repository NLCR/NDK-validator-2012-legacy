package com.logica.ndk.tm.utilities.transformation.sip1;

import org.junit.Test;

public class CreateSIP1FromCDMImplIT {
  @Test
  public void test() {
    String cdmId = "21775341-f271-11e1-b2ba-00505682629d";
    try {
      new CreateSIP1FromCDMImpl().execute(cdmId);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
