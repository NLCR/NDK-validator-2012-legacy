package com.logica.ndk.tm.utilities.integration.aleph.exception;

import javax.xml.ws.WebFault;

import com.logica.ndk.tm.utilities.SystemException;

@WebFault
public class XMLParsingException extends SystemException {
  private static final long serialVersionUID = 3497591197301017910L;

  public XMLParsingException() {
    super();
  }

  public XMLParsingException(Exception e) {
    super(e);
  }

  public XMLParsingException(String message) {
    super(message);
  }

  public XMLParsingException(String message, Throwable cause) {
    super(message, cause);
  }

  public XMLParsingException(Throwable cause) {
    super(cause);
  }

}
