package com.logica.ndk.tm.utilities.ping;

import javax.xml.ws.WebFault;

import com.logica.ndk.tm.utilities.UtilityException;

@WebFault
public class PingException extends UtilityException {
  private static final long serialVersionUID = -5508616407197339506L;

  public PingException() {
    super();
  }

  public PingException(String message, Throwable cause) {
    super(message, cause);
  }

  public PingException(String message) {
    super(message);
  }

  public PingException(Throwable cause) {
    super(cause);
  }

  public PingException(String message, Long errorCode) {
    super(message, errorCode);
  }

  public PingException(String message, Throwable cause, Long errorCode) {
    super(message, cause, errorCode);
  }

  public PingException(Throwable cause, Long errorCode) {
    super(cause, errorCode);
  }

}
