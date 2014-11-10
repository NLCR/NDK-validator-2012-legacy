package com.logica.ndk.tm.utilities.integration.wf.exception;

import javax.xml.ws.WebFault;

/**
 * WF incorrect request exception
 * @author majdaf
 *
 */
@WebFault
public class UnknownActivityException extends Exception {

  private static final long serialVersionUID = 1L;

  public UnknownActivityException(String message) {
    super(message);
  }

}
