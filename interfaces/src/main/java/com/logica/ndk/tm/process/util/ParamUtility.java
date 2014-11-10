package com.logica.ndk.tm.process.util;

public abstract class ParamUtility {
  private String value;

  public ParamUtility(String value) {
    this.value = value;
  }

  public abstract String getNamespace();

  public abstract String getName();

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

}
