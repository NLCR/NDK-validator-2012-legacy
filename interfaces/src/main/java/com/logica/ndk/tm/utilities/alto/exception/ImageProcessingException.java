package com.logica.ndk.tm.utilities.alto.exception;

import javax.xml.ws.WebFault;

@WebFault
public class ImageProcessingException extends Exception {
	private static final long serialVersionUID = -6947984817358967298L;

	public ImageProcessingException() {
		super();
	}
	public ImageProcessingException(String message, Throwable cause) {
		super(message, cause);
	}
	public ImageProcessingException(String message) {
		super(message);
	}

	public ImageProcessingException(Throwable cause) {
		super(cause);
	}
}

