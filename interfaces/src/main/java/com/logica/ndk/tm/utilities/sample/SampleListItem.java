package com.logica.ndk.tm.utilities.sample;

import java.io.Serializable;

public class SampleListItem implements Serializable {
  private static final long serialVersionUID = -7220157063244701065L;
  private String name;
  private Integer value;
  
  public SampleListItem() {
    
  }
  
  public SampleListItem(String name, Integer value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getValue() {
    return value;
  }

  public void setValue(Integer value) {
    this.value = value;
  }

  public String toString() {
    return String.format("%s(%s,%s)", this.getClass().getSimpleName(),getName(), getValue());
  }
  
}
