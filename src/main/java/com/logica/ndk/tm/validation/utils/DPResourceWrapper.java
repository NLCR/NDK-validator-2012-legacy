package com.logica.ndk.tm.validation.utils;

import java.io.File;
import java.util.*;

import com.logica.ndk.tm.validation.data.ValidationProfile;

/**
 * @author Tomas Mriz (Logica)
 */
public class DPResourceWrapper {

    private static final String MANDATORY_DIR = "mandatory.directory.";
    private static final String CROSSVALIDATION = "crossvalidation.on";
    private static final String METS_XSD = "mets.xsdfile.location";
    private static final String MODS_XSD = "mods.xsdfile.location";
    private static final String METS_PROFILE = "metsdata.profile.";
    private static final String METS_PROFILE_NAME = ".name";
    private static final String METS_PROFILE_TEST = ".tests";
    private static final String METADATA_TEMPLATES = "metsdata.templates.location";
    private static final String TEMPLATE_VERSION = "metsdata.templates.version";
    private static final String PREMIS_XSD = "premis.xsdfile.location";
    private static final String MIX_XSD = "mix.xsdfile.location";
    private static final String ALTO_XSD = "alto.xsdfile.location";

    private static final String OUTPUT_DIR = "validation.output.location";
    private static final String APPEND_OUTPUT = "validation.output.append";
    private static final String INPUT_DIRMODE = "validation.input.dirmode";
    private Properties dpProperties;

    public DPResourceWrapper(Properties dpProperties) {
        this.dpProperties = dpProperties;
    }

    public List<String> getMandatoryDirs() {
        int i = 0;

        List<String> mandatoryDirs = new ArrayList<String>();
        Object dir = null;
        while ((dir = dpProperties.get(MANDATORY_DIR + i)) != null) {
            if (dir != null) {
                mandatoryDirs.add((String) dir);
                i++;
            }
        }

        return mandatoryDirs;
    }

    public String getMetsXSDLocation() {
        return (String) dpProperties.get(METS_XSD);
    }

    public String getModsXSDLocation() {
        return (String) dpProperties.get(MODS_XSD);
    }

    public List<String> getMetsProfiles() {
        int i = 0;

        List<String> metsProfiles = new ArrayList<String>();

        Object name = null;
        while ((name = dpProperties.get(METS_PROFILE + i
                + METS_PROFILE_NAME)) != null) {
            if (name != null) {
                metsProfiles.add((String) name);
                i++;
            }
        }

        return metsProfiles;
    }

    public ValidationProfile getValidationProfile(String profileName) {
        int i = 0;
        List<String> tests = new ArrayList<String>();
        boolean profileNotFound = true;

        Object name = null;
        while ((name = dpProperties.get(METS_PROFILE + i
                + METS_PROFILE_NAME)) != null) {
            if (name != null) {
                String parsedName = (String) name;
                if (parsedName.equalsIgnoreCase(profileName)) {
                    profileNotFound = false;
                    break;
                }
            }
            i++;
        }

        if (!profileNotFound) {
            tests = getTestForProfile(i);
            return new ValidationProfile(profileName, tests);
        } else {
            return null;
        }
    }

    public List<String> getTestForProfile(int profileId) {
        Object profileTests = dpProperties.get(METS_PROFILE + profileId
                + METS_PROFILE_TEST);

        List<String> testForProfile = new ArrayList<String>();

        if (profileTests != null) {
            String parsedTest = (String) profileTests;
            if (!parsedTest.isEmpty()) {
                String[] tests = parsedTest.split(",");
                for (String test : tests) {
                    testForProfile.add(test);
                }
            }
        }

        return testForProfile;
    }

    public String getMetadataTemplates() {
        return (String) dpProperties.get(METADATA_TEMPLATES);
    }

    public String getPremisXsdLocation() {
        return (String) dpProperties.get(PREMIS_XSD);
    }

    public String getMixXsdLocation() {
        return (String) dpProperties.get(MIX_XSD);
    }    

    public String getOutputDir() {
        return (String) dpProperties.get(OUTPUT_DIR);
    }
    
    public String getAltoXsdLocation(){
        return (String) dpProperties.get(ALTO_XSD);
    }

    public boolean getOutputAppend() {
        String isAppend = (String) dpProperties.get(APPEND_OUTPUT);
        return Boolean.valueOf(isAppend);
    }

    public boolean getCrossValidation() {
        String isCross = (String) dpProperties.get(CROSSVALIDATION);
        return Boolean.valueOf(isCross);
    }

    public String getTemplateVersion() {
        return (String) dpProperties.get(TEMPLATE_VERSION);
    }

    public boolean getDirMode() {
        String isDirMode = (String) dpProperties.get(INPUT_DIRMODE);
        return Boolean.valueOf(isDirMode);
    }
}
