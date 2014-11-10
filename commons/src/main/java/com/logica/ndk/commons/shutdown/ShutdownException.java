package com.logica.ndk.commons.shutdown;

/**
 * Exception to expose that shut down of TM is in progress because shutdown attribute is set to true.
 * 
 * @author Rudolf Daco
 */
public class ShutdownException extends RuntimeException {
  private static final long serialVersionUID = 9066738509347010285L;

  public ShutdownException() {
    super();
  }

  public ShutdownException(String message) {
    super(message);
  }
}
