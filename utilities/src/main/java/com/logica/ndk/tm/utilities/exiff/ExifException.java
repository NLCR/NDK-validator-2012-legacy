package com.logica.ndk.tm.utilities.exiff;

public class ExifException extends Exception {

  private static final long serialVersionUID = -3946192540080229413L;

  protected ExifException() {
    super();
  }

  protected ExifException(String message, Throwable cause) {
    super(message, cause);
  }

  protected ExifException(String message) {
    super(message);
  }

  protected ExifException(Throwable cause) {
    super(cause);
  }

}
