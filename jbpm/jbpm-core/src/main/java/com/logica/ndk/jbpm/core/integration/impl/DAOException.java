package com.logica.ndk.jbpm.core.integration.impl;

public class DAOException extends Exception {
  private static final long serialVersionUID = 4975294034361937336L;

  public DAOException() {
    super();
  }

  public DAOException(String message, Throwable cause) {
    super(message, cause);
  }

  public DAOException(String message) {
    super(message);
  }

  public DAOException(Throwable cause) {
    super(cause);
  }

}
