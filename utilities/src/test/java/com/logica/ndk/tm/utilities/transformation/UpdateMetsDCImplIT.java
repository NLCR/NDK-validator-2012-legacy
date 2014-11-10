package com.logica.ndk.tm.utilities.transformation;

import org.junit.Ignore;
import org.junit.Test;

public class UpdateMetsDCImplIT {

  @Test
  public void test() {
    String cdmId = "d2cd11f0-88e5-11e3-bd09-005056827e51";
    new UpdateMetsDCImpl().execute(cdmId);
  }
}
