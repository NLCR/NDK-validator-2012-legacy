package com.logica.ndk.tm.utilities.integration.rd.exception;

import javax.xml.ws.WebFault;

import com.logica.ndk.tm.utilities.BusinessException;

/**
 * @author ondrusekl
 */
@WebFault
public class BadDigitizationStateException extends BusinessException {

  private static final long serialVersionUID = 1L;

  public BadDigitizationStateException() {
    super();
  }

  public BadDigitizationStateException(String message, Throwable cause) {
    super(message, cause);
  }

  public BadDigitizationStateException(String message) {
    super(message);
  }

  public BadDigitizationStateException(Throwable cause) {
    super(cause);
  }

  public BadDigitizationStateException(String message, Long errorCode) {
    super(message, errorCode);
  }

  public BadDigitizationStateException(String message, Throwable cause, Long errorCode) {
    super(message, cause, errorCode);
  }

  public BadDigitizationStateException(Throwable cause, Long errorCode) {
    super(cause, errorCode);
  }
  
  
  
}
