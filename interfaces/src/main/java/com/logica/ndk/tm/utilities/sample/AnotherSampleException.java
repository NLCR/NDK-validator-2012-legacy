package com.logica.ndk.tm.utilities.sample;

import javax.xml.ws.WebFault;

@WebFault
public class AnotherSampleException extends Exception {

  private static final long serialVersionUID = -526943692710986348L;

  public AnotherSampleException() {
    super();
  }

  public AnotherSampleException(String arg0, Throwable arg1) {
    super(arg0, arg1);
  }

  public AnotherSampleException(String arg0) {
    super(arg0);
  }

  public AnotherSampleException(Throwable arg0) {
    super(arg0);
  }
}
