package com.logica.ndk.tm.utilities.transformation.jpeg2000;

import org.junit.Ignore;
import org.junit.Test;

public class ConvertToJpeg2000ImplIT {
  @Ignore
  public void testExecute() {
    long start = System.currentTimeMillis();
     Integer execute = new ConvertToJpeg2000Impl().execute(
        "9dcfedb0-a65b-11e1-98cc-02004c4f4f50", 
        "c:\\NDK\\data_test\\CDM_9dcfedb0-a65b-11e1-98cc-02004c4f4f50\\data\\postprocessingData\\", 
        "c:\\NDK\\data_test\\CDM_9dcfedb0-a65b-11e1-98cc-02004c4f4f50\\data\\masterCopy\\", 
        "JPEG2000PRESERVEDPI", 
        "");
    System.out.println("Takes: " + ((System.currentTimeMillis() - start) / 1000) + "s.");
    System.out.println("Result:" + execute);
  }
}