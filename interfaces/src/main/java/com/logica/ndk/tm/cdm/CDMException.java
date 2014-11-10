package com.logica.ndk.tm.cdm;

public class CDMException extends RuntimeException {
  private static final long serialVersionUID = -9142590631193607016L;

  public CDMException(String msg) {
    super(msg);
  }

  public CDMException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
