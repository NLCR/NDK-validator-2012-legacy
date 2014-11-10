package com.logica.ndk.tm.utilities.transformation;

import javax.xml.ws.WebFault;

import com.logica.ndk.tm.utilities.UtilityException;

@WebFault
public class TransformationException extends UtilityException {
    private static final long serialVersionUID = -1168213101594378731L;

    public TransformationException() {
        super();
    }

    public TransformationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransformationException(String message) {
        super(message);
    }

    public TransformationException(Throwable cause) {
        super(cause);
    }
}
