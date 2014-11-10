package com.logica.ndk.tm.utilities;

import javax.xml.ws.WebFault;

/**
 * Not critical exception. Is thrown if an error type no records found, etc.
 * 
 * @author ondrusekl
 */
@WebFault
public class BusinessException extends UtilityException {

  public static final long serialVersionUID = 1L;

  protected BusinessException() {
    super();
  }

  public BusinessException(String message, Throwable cause) {
    super(message, cause);
  }

  public BusinessException(String message) {
    super(message);
  }

  public BusinessException(Throwable cause) {
    super(cause);
  }

  public BusinessException(String message, Long errorCode) {
    super(message, errorCode);
  }

  public BusinessException(String message, Throwable cause, Long errorCode) {
    super(message, cause, errorCode);
  }

  public BusinessException(Throwable cause, Long errorCode) {
    super(cause, errorCode);
  }

}
