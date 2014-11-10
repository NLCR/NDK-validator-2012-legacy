package com.logica.ndk.tm.utilities.transformation;

import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.transformation.MergeCDMImpl;

public class MergeCDMImplIT {
  @Ignore
  public void test() {
    String cdmIdMaster = "29aba5d0-cdc2-11e1-91a2-00505682629d-master";
    String cdmIdSlave = "29aba5d0-cdc2-11e1-91a2-00505682629d-slave";
    new MergeCDMImpl().execute(cdmIdMaster, cdmIdSlave);
  }
}
