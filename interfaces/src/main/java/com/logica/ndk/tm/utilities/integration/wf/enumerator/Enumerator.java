package com.logica.ndk.tm.utilities.integration.wf.enumerator;

public class Enumerator {
  Long id;
  String name;
  String code;
  String cbType;
  boolean trashed;
  
  public Enumerator() {
  }

  public Enumerator(Long id, String code) {
    this.id = id;
    this.code = code;
  }
  
  public String toString() {
    return name;
  }
  
  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getCode() {
    return code;
  }
  public void setCode(String code) {
    this.code = code;
  }

  public String getCbType() {
    return cbType;
  }

  public void setCbType(String cbType) {
    this.cbType = cbType;
  }

  public boolean isTrashed() {
    return trashed;
  }

  public void setTrashed(boolean trashed) {
    this.trashed = trashed;
  }
    
}
