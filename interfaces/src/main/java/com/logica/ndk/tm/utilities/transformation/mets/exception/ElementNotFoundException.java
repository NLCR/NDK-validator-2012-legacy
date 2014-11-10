package com.logica.ndk.tm.utilities.transformation.mets.exception;

import com.logica.ndk.tm.utilities.BusinessException;

/**
 * Thrown when required element of METS not found in given XML 
 * @author majdaf
 *
 */
public class ElementNotFoundException extends BusinessException {

  private static final long serialVersionUID = 1L;

  public ElementNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public ElementNotFoundException(String message) {
    super(message);
  }

  public ElementNotFoundException(Throwable cause) {
    super(cause);
  }

  public ElementNotFoundException(String message, Long errorCode) {
    super(message, errorCode);
  }

  public ElementNotFoundException(String message, Throwable cause, Long errorCode) {
    super(message, cause, errorCode);
  }

  public ElementNotFoundException(Throwable cause, Long errorCode) {
    super(cause, errorCode);
  }
  
  
  
}
