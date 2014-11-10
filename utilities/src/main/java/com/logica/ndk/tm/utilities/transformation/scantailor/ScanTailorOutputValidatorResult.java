package com.logica.ndk.tm.utilities.transformation.scantailor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author krchnacekm
 */
public class ScanTailorOutputValidatorResult {

    private Boolean valid;
    private List<String> messages;

    public ScanTailorOutputValidatorResult() {
        this.valid = true;
        this.messages = new ArrayList<String>();
    }

    public Boolean getValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    @Override
    public String toString() {
        return "ScanTailorOutputValidatorResult{" +
                "valid=" + valid +
                ", messages=" + messages +
                '}';
    }
}
