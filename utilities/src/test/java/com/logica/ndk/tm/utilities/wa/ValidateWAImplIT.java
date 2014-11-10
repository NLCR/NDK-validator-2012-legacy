package com.logica.ndk.tm.utilities.wa;

import org.junit.Ignore;
import org.junit.Test;

public class ValidateWAImplIT {
  @Ignore
  public void testExecute() {
    String sourceDir = "c:\\NDK\\data_test\\_wa\\all";
    Boolean execute = new ValidateWAImpl().execute(sourceDir);
    System.out.println(execute);
  }
}
