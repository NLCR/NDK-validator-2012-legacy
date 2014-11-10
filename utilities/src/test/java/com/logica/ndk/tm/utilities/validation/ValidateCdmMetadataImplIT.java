package com.logica.ndk.tm.utilities.validation;

import org.junit.Ignore;
import org.junit.Test;

public class ValidateCdmMetadataImplIT {
  @Ignore
  public void testExecute() {
    String cdmId = "61fd10d0-c3b4-11e1-adc3-00505682629d";
    new ValidateCdmMetadataImpl().validate(cdmId, false);
  }
}
