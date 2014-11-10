package com.logica.ndk.jbpm.core;

import java.lang.instrument.Instrumentation;

public class MemeryCalculator {

  private static volatile Instrumentation globalInstr;

  public static void premain(String args, Instrumentation inst) {
    globalInstr = inst;
  }

  public static long getObjectSize(Object obj) {
    if (globalInstr == null)
      throw new IllegalStateException("Agent not initted");
    return globalInstr.getObjectSize(obj);
  }

}
