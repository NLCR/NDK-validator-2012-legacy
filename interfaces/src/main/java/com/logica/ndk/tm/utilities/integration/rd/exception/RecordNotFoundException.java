package com.logica.ndk.tm.utilities.integration.rd.exception;

import javax.xml.ws.WebFault;

import com.logica.ndk.tm.utilities.integration.NotExpectedResultSizeException;

/**
 * @author ondrusekl
 */
@WebFault
public class RecordNotFoundException extends NotExpectedResultSizeException {

  private static final long serialVersionUID = 1L;

  public RecordNotFoundException() {
    super();
  }

  public RecordNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public RecordNotFoundException(String message) {
    super(message);
  }

  public RecordNotFoundException(Throwable cause) {
    super(cause);
  }

}
