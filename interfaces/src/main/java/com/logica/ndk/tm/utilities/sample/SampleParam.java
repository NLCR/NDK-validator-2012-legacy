package com.logica.ndk.tm.utilities.sample;

import java.io.Serializable;

public class SampleParam implements Serializable {

  private static final long serialVersionUID = -7652397071658822003L;
  private String firstname;
  private String lastname;

  public SampleParam(String firstname, String lastname) {
    this.firstname = firstname;
    this.lastname = lastname;
  }

  public SampleParam() {
  }

  public String getFirstname() {
    return firstname;
  }

  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  public String getLastname() {
    return lastname;
  }

  public void setLastname(String lastname) {
    this.lastname = lastname;
  }

  @Override
  public String toString() {
    return String.format("%s(%s,%s)", this.getClass().getSimpleName(), getFirstname(), getLastname());
  }

}
