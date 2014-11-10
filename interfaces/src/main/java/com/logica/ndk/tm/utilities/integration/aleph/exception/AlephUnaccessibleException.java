package com.logica.ndk.tm.utilities.integration.aleph.exception;

import javax.xml.ws.WebFault;

import com.logica.ndk.tm.utilities.SystemException;

//TODO Should be inherited form BusinessException
@WebFault
public class AlephUnaccessibleException extends SystemException {
	private static final long serialVersionUID = 1744466334097015036L;

	public AlephUnaccessibleException() {
		super ("\nAleph unaccessible.");
	}
	public AlephUnaccessibleException(Exception e) {
		super (e);
	}
	public AlephUnaccessibleException(String message) {
		super(message);
	}
}
