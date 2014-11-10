package com.logica.ndk.tm.mule.jmx;

import org.junit.Ignore;
import org.junit.Test;

public class StopSlaveConnectorsIT {

  private final static String host = "localhost";
  private final static String port = "1097";

  @Ignore
  public void execute() {
    try {
      new StopSlaveConnectors(host, port).execute();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
