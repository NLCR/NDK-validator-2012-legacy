package com.logica.ndk.tm.utilities.transformation.sip1.exception;

import javax.xml.ws.WebFault;

import com.logica.ndk.tm.utilities.BusinessException;

/**
 * SIP1 import to LTP failed exception
 * @author majdaf
 *
 */
@WebFault
public class SIP1ImportFailedException extends BusinessException {

  private static final long serialVersionUID = 1L;

  public SIP1ImportFailedException(String message) {
    super(message);
  }

  public SIP1ImportFailedException(String message, Long errorCode) {
    super(message, errorCode);
  }

  
  
}
