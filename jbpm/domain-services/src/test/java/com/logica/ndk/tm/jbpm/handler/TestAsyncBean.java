package com.logica.ndk.tm.jbpm.handler;

import org.junit.Ignore;

@Ignore
public class TestAsyncBean implements Bean {

  private final boolean async;

  public TestAsyncBean() {
    this.async = true;
  }

  public boolean isAsync() {
    return async;
  }

}
