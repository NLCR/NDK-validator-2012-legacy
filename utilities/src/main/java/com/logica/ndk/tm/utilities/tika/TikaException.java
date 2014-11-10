package com.logica.ndk.tm.utilities.tika;

public class TikaException extends Exception {
    private static final long serialVersionUID = -1734920798421983744L;

    protected TikaException() {
        super();
    }

    protected TikaException(String message, Throwable cause) {
        super(message, cause);
    }

    protected TikaException(String message) {
        super(message);
    }

    protected TikaException(Throwable cause) {
        super(cause);
    }
}
