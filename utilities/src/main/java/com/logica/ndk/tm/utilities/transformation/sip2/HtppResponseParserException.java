package com.logica.ndk.tm.utilities.transformation.sip2;

import com.logica.ndk.tm.utilities.SystemException;

public class HtppResponseParserException extends SystemException {

  public HtppResponseParserException() {
  }

  public HtppResponseParserException(String message, Throwable cause) {
    super(message, cause);
  }

  public HtppResponseParserException(String message) {
    super(message);
  }

  public HtppResponseParserException(Throwable cause) {
    super(cause);
  }

  public HtppResponseParserException(String message, Long errorCode) {
    super(message, errorCode);
  }

  public HtppResponseParserException(String message, Throwable cause, Long errorCode) {
    super(message, cause, errorCode);
  }

  public HtppResponseParserException(Throwable cause, Long errorCode) {
    super(cause, errorCode);
  }

}
