package com.logica.ndk.tm.utilities.file.exception;

import javax.xml.ws.WebFault;

import com.logica.ndk.tm.utilities.SystemException;

/**
 * Generic rsync exception
 */
@WebFault
public class RsyncException extends SystemException {

  private static final long serialVersionUID = 1L;
  
  public RsyncException(String message) {
    super(message);
  }

}
