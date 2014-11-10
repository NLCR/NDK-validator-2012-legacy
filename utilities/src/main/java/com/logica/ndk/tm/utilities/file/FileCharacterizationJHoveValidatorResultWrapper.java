package com.logica.ndk.tm.utilities.file;

import java.util.ArrayList;
import java.util.List;

/**
 * @author krchnacekm
 */
public class FileCharacterizationJHoveValidatorResultWrapper {

    private final Boolean containsErrors;
    private final List<String> messages;

    public FileCharacterizationJHoveValidatorResultWrapper() {
        this.containsErrors = false;
        this.messages = new ArrayList<String>();
    }

    public FileCharacterizationJHoveValidatorResultWrapper(Boolean containsErrors, List<String> messages) {
        this.containsErrors = containsErrors;
        this.messages = messages;
    }

    public Boolean isValid() {
        return containsErrors;
    }

    public List<String> getMessages() {
        return messages;
    }

    @Override
    public String toString() {
        return "FileCharacterizationJHoveValidatorResultWrapper{" +
                "containsErrors=" + containsErrors +
                ", messages=" + messages +
                '}';
    }
}
