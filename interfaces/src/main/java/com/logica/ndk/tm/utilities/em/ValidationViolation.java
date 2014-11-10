package com.logica.ndk.tm.utilities.em;

import java.io.Serializable;

public class ValidationViolation implements Serializable {

  private static final long serialVersionUID = 6936920273507573247L;
  private String violationCode;
  private String violationDescription;

  public ValidationViolation() {
  }

  public ValidationViolation(String violationCode, String violationDescription) {
    this.violationCode = violationCode;
    this.violationDescription = violationDescription;
  }

  public String getViolationCode() {
    return violationCode;
  }

  public void setViolationCode(String violationCode) {
    this.violationCode = violationCode;
  }

  public String getViolationDescription() {
    return violationDescription;
  }

  public void setViolationDescription(String violationDescription) {
    this.violationDescription = violationDescription;
  }
  
  public String toString(){
    return this.violationCode +" : "+ this.violationDescription;
  }
}
