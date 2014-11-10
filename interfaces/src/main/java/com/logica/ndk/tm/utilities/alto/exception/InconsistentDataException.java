package com.logica.ndk.tm.utilities.alto.exception;

import javax.xml.ws.WebFault;

import com.logica.ndk.tm.utilities.SystemException;

//TODO Should be inherited form BusoinessException
@WebFault
public class InconsistentDataException extends SystemException {
	private static final long serialVersionUID = 4787253549789067564L;

	public InconsistentDataException() {
		super ("\nInconsistent img or xml files. Check their number and names respectively. Both must match.");	
	}
	public InconsistentDataException(String message) {
		super(message);
	}
}
