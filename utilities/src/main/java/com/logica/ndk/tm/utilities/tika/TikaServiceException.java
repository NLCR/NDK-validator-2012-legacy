package com.logica.ndk.tm.utilities.tika;

public class TikaServiceException extends Exception {
    private static final long serialVersionUID = -1734920798421983744L;

    protected TikaServiceException() {
        super();
    }

    protected TikaServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    protected TikaServiceException(String message) {
        super(message);
    }

    protected TikaServiceException(Throwable cause) {
        super(cause);
    }
}
