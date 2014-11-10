package com.logica.ndk.tm.utilities.integration;

import javax.xml.ws.WebFault;

import com.logica.ndk.tm.utilities.UtilityException;

/**
 * @author ondrusekl
 */
@WebFault
public class NotExpectedResultSizeException extends UtilityException {

  private static final long serialVersionUID = 1L;

  public NotExpectedResultSizeException() {
    super();
  }

  public NotExpectedResultSizeException(String message, Throwable cause) {
    super(message, cause);
  }

  public NotExpectedResultSizeException(String message) {
    super(message);
  }

  public NotExpectedResultSizeException(Throwable cause) {
    super(cause);
  }

}
