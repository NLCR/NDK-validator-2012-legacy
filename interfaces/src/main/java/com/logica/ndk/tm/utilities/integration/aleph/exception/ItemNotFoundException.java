package com.logica.ndk.tm.utilities.integration.aleph.exception;

import javax.xml.ws.WebFault;

import com.logica.ndk.tm.utilities.BusinessException;

//TODO Should be inherited form BusoinessException
@WebFault
public class ItemNotFoundException extends BusinessException {

	private static final long serialVersionUID = -2018103761411403848L;

	public ItemNotFoundException() {
		super();
	}
	public ItemNotFoundException(String message) {
		super(message);
	}
  public ItemNotFoundException(String message, Long errorCode) {
    super(message, errorCode);
  }
	
	
}
