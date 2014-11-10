package com.logica.ndk.tm.jbpm.handler;

import org.junit.Ignore;

@Ignore
public class TestBean implements Bean {

  private final boolean async;

  public TestBean() {
    this.async = false;
  }

  public boolean isAsync() {
    return async;
  }

}
