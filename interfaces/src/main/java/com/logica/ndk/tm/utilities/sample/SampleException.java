package com.logica.ndk.tm.utilities.sample;

import javax.xml.ws.WebFault;

@WebFault
public class SampleException extends Exception {

  private static final long serialVersionUID = 9210962817757825078L;

  public SampleException() {
    super();
  }

  public SampleException(String message, Throwable cause) {
    super(message, cause);
  }

  public SampleException(String message) {
    super(message);
  }

  public SampleException(Throwable cause) {
    super(cause);
  }

}
