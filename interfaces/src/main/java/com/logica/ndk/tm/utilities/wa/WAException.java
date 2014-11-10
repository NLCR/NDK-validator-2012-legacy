package com.logica.ndk.tm.utilities.wa;

import javax.xml.ws.WebFault;

import com.logica.ndk.tm.utilities.UtilityException;

@WebFault
public class WAException extends UtilityException {
  private static final long serialVersionUID = -5508616407197339506L;

  public WAException() {
    super();
  }

  public WAException(String message, Throwable cause) {
    super(message, cause);
  }

  public WAException(String message) {
    super(message);
  }

  public WAException(Throwable cause) {
    super(cause);
  }
}
