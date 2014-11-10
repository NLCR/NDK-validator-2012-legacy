package com.logica.ndk.tm.utilities.integration.wf.exception;

import javax.xml.ws.WebFault;

/**
 * WF incorrect request exception
 * @author majdaf
 *
 */
@WebFault
public class BadRequestException extends Exception {

  private static final long serialVersionUID = 1L;

  public BadRequestException(String message) {
    super(message);
  }
  
  public BadRequestException(String message, Throwable t) {
    super(message, t);
  }

}
