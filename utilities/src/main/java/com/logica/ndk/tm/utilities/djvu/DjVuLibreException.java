package com.logica.ndk.tm.utilities.djvu;

public class DjVuLibreException extends Exception {
  private static final long serialVersionUID = 1910850517330714717L;

  protected DjVuLibreException() {
    super();
  }

  protected DjVuLibreException(String message, Throwable cause) {
    super(message, cause);
  }

  protected DjVuLibreException(String message) {
    super(message);
  }

  protected DjVuLibreException(Throwable cause) {
    super(cause);
  }
}
