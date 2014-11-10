package com.logica.ndk.tm.utilities.integration.wf;

public class ScannerFinder {
  String code;

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getQueryParams() {
    StringBuffer b = new StringBuffer("");
    // Max items
    if (code != null) {
      b.append("code=" + code);
    }
    return b.toString();
  }
  
}
