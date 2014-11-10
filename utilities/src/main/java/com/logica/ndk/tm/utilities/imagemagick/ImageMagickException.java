package com.logica.ndk.tm.utilities.imagemagick;

public class ImageMagickException extends Exception {
    private static final long serialVersionUID = -1734920798421983744L;

    protected ImageMagickException() {
        super();
    }

    protected ImageMagickException(String message, Throwable cause) {
        super(message, cause);
    }

    protected ImageMagickException(String message) {
        super(message);
    }

    protected ImageMagickException(Throwable cause) {
        super(cause);
    }
}
