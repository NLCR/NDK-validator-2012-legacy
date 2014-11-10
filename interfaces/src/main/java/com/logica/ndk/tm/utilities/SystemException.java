package com.logica.ndk.tm.utilities;

import javax.xml.ws.WebFault;

/**
 * Critical error. Is thrown if an error type target system is not available, etc.
 * 
 * @author ondrusekl
 */
@WebFault
public class SystemException extends UtilityException {

  private static final long serialVersionUID = 1L;

  public SystemException() {
    super();
  }

  public SystemException(String message, Throwable cause) {
    super(message, cause);
  }

  public SystemException(String message) {
    super(message);
  }

  public SystemException(Throwable cause) {
    super(cause);
  }

  public SystemException(String message, Long errorCode) {
    super(message, errorCode);
  }

  public SystemException(String message, Throwable cause, Long errorCode) {
    super(message, cause, errorCode);
  }

  public SystemException(Throwable cause, Long errorCode) {
    super(cause, errorCode);
  }

}
