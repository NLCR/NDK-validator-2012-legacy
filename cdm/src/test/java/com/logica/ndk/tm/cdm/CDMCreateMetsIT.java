package com.logica.ndk.tm.cdm;

import org.junit.Ignore;
import org.junit.Test;

public class CDMCreateMetsIT {

  @Ignore
  public void testCreate() throws Exception {
    // musi uz existovat${java.io.tmpdir}/CDM_test.createMetsIT.0001
    final String cdmId = "d2f40eb0-a960-11e1-9b54-02004c4f4f50";
    final CDM cdm = new CDM();
    cdm.createMetsFromContent(cdmId, true);
  }

}
