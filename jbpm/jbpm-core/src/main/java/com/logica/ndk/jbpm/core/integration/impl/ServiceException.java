package com.logica.ndk.jbpm.core.integration.impl;

/**
 * @author Rudolf Daco
 *
 */
public class ServiceException extends Exception {
  private static final long serialVersionUID = -2208452672571290769L;

  public ServiceException() {
    super();
  }

  public ServiceException(String message, Throwable cause) {
    super(message, cause);
  }

  public ServiceException(String message) {
    super(message);
  }

  public ServiceException(Throwable cause) {
    super(cause);
  }
}
