package com.logica.ndk.tm.validation.data;

import java.util.List;

/**
 * @author Tomas Mriz (Logica)
 */
public class ValidationProfile {

    private String profileName;
    private List<String> tests;

    public ValidationProfile(String profileName, List<String> tests) {
        this.profileName = profileName;
        this.tests = tests;
    }

    public String getProfileName() {
        return profileName;
    }

    public List<String> getTests() {
        return tests;
    }
}
