package com.logica.ndk.tm.utilities.integration.wf.exception;

import javax.xml.ws.WebFault;

import com.logica.ndk.tm.utilities.SystemException;

@WebFault
public class WFConnectionUnavailableException extends SystemException {

  private static final long serialVersionUID = 1L;

  public WFConnectionUnavailableException() {
    super();
  }

  public WFConnectionUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }

  public WFConnectionUnavailableException(String message) {
    super(message);
  }

  public WFConnectionUnavailableException(Throwable cause) {
    super(cause);
  }

}
