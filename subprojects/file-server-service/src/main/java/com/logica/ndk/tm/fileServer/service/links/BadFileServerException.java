/**
 * 
 */
package com.logica.ndk.tm.fileServer.service.links;

/**
 * @author brizat
 *
 */
public class BadFileServerException extends Exception {

  /**
   * 
   */
  public BadFileServerException() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   */
  public BadFileServerException(String message) {
    super(message);
  }

  /**
   * @param exception
   */
  public BadFileServerException(Throwable exception) {
    super(exception);
  }

  /**
   * @param message
   * @param exception
   */
  public BadFileServerException(String message, Throwable exception) {
    super(message, exception);
  }

}
