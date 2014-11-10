package com.logica.ndk.tm.utilities.transformation;

import org.junit.Test;

public class UpdateMetsFilesImplIT {
  @Test
  public void test() {
    String cdmId = "2cdcb840-1ce4-11e2-b2bd-00505682629d";
    new UpdateMetsFilesImpl().execute(cdmId);
  }
}
