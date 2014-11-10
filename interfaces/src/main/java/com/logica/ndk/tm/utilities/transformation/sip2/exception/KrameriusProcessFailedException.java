package com.logica.ndk.tm.utilities.transformation.sip2.exception;

import com.logica.ndk.tm.utilities.BusinessException;

public class KrameriusProcessFailedException extends BusinessException {
	
	private static final long serialVersionUID = 1L;
	
	public KrameriusProcessFailedException (String message) {
		super(message);
	}

  public KrameriusProcessFailedException(String message, Long errorCode) {
    super(message, errorCode);
  }
	
}
