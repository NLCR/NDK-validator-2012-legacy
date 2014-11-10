package com.logica.ndk.tm.utilities.integration.aleph;

import java.io.Serializable;

public class GetAlephExtendedDataResponse implements Serializable
{

  private static final long serialVersionUID = 1677820870477263986L;

  protected String result;
  protected String docnum;

  public String getResult() {
    return result;
  }

  public void setResult(String value) {
    this.result = value;
  }

  public String getDocnum() {
    return docnum;
  }

  public void setDocnum(String value) {
    this.docnum = value;
  }

}
