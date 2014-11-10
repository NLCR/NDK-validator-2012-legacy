package com.logica.ndk.tm.utilities.file;

import javax.xml.ws.WebFault;

import com.logica.ndk.tm.utilities.UtilityException;

@WebFault
public class FileCharacterizationException extends UtilityException {
    private static final long serialVersionUID = -4159083056282752417L;

    protected FileCharacterizationException() {
        super();
    }

    protected FileCharacterizationException(String message, Throwable cause) {
        super(message, cause);
    }

    protected FileCharacterizationException(String message) {
        super(message);
    }

    protected FileCharacterizationException(Throwable cause) {
        super(cause);
    }
}
