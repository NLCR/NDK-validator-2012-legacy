package com.logica.ndk.tm.slave.filter;

import java.io.File;

import org.mule.api.MuleMessage;
import org.mule.api.routing.filter.Filter;

public class ShutDownFilter implements Filter{

  private static String filePath = "C:\\NDK\\slaveShutDown";
  
  @Override
  public boolean accept(MuleMessage message) {
    return ! new File(filePath).exists();
  }

}
