package com.logica.ndk.tm.utilities.validation;

import org.junit.Ignore;
import org.junit.Test;

public class ValidateImagesForPostprocImplIT {
  @Ignore
  public void test() {
    String cdmId = "05483750-9fa3-11e1-b13b-00505682629d";
    new ValidateImagesForPostprocImpl().validate(cdmId, true);
  }
}
