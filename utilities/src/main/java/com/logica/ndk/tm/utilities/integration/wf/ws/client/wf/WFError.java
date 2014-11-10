package com.logica.ndk.tm.utilities.integration.wf.ws.client.wf;

public class WFError {
  int status;
  String errorMsg;
  
  public String toString() {
    return status + " " + errorMsg;
  }
  
  public int getStatus() {
    return status;
  }
  public void setStatus(int status) {
    this.status = status;
  }
  public String getErrorMsg() {
    return errorMsg;
  }
  public void setErrorMsg(String errorMsg) {
    this.errorMsg = errorMsg;
  }
  
}
