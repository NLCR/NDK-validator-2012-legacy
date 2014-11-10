package com.logica.ndk.tm.utilities.kakadu;

public class KakaduException extends Exception {
    private static final long serialVersionUID = -1734920798421983744L;

    protected KakaduException() {
        super();
    }

    protected KakaduException(String message, Throwable cause) {
        super(message, cause);
    }

    protected KakaduException(String message) {
        super(message);
    }

    protected KakaduException(Throwable cause) {
        super(cause);
    }
}
