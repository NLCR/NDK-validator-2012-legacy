package com.logica.ndk.tm.utilities.alto.exception;

import javax.xml.ws.WebFault;

import com.logica.ndk.tm.utilities.SystemException;

//TODO Should be inherited form BusoinessException
@WebFault
public class InvalidSourceFolderException extends SystemException {
	private static final long serialVersionUID = -4000370664963054536L;

	public InvalidSourceFolderException() {
		super ("\nCan not resolve source folders with img and xml files.");
	}
	public InvalidSourceFolderException(String message) {
		super(message);
	}
}
