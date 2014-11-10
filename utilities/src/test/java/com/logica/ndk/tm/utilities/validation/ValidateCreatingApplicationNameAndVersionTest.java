package com.logica.ndk.tm.utilities.validation;

import org.junit.Before;
import org.junit.Test;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by krchnacekm on 19.12.13.
 */
public class ValidateCreatingApplicationNameAndVersionTest {

    private static final String CORRECT_AMD_METS_NAME = "AMD_METS_dc551ca0-527a-11e3-ae59-005056827e52_0174.xml";
    private static final String INCORRECT_AMD_METS_NAME = "AMD_METS_dc551ca0-527a-11e3-ae59-005056827e52_0175.xml";
    private static final String CDM_PATH_RESOURCE_NAME = "cdm1";
    private static final String CDM_ID = "1";
    private static final String EXPECTED_APPLICATION_NAME_ERRROR_MESSAGE = "AMD METS (AMD_METS_dc551ca0-527a-11e3-ae59-005056827e52_0175.xml) of CDM 1. Value of element premis:creatingApplicationName is empty.";
    private static final String EXPECTED_APPLICATION_VERSION_ERROR_MESSAGE = "AMD METS (AMD_METS_dc551ca0-527a-11e3-ae59-005056827e52_0175.xml) of CDM 1. Value of element premis:creatingApplicationVersion is empty.";
    private static final int EXPECTED_COUNT_OF_ERRROR_MESSAGES = 2;

    private File correctAmdMetsFile;
    private File incorrectAmdMetsFile;
    private String amdSecFolder;

    @Before
    public void setUp() throws Exception {
        final File cdmFolder = new File(this.getClass().getResource(CDM_PATH_RESOURCE_NAME).getPath());
        this.amdSecFolder = String.format("%s\\CDM_%s\\data\\amdSec", cdmFolder.getParent(), CDM_ID);
        this.correctAmdMetsFile = new File(String.format("%s\\%s", amdSecFolder, CORRECT_AMD_METS_NAME));
        this.incorrectAmdMetsFile = new File(String.format("%s\\%s", amdSecFolder, INCORRECT_AMD_METS_NAME));
    }


    @Test
    public void existAmdMetsFiles() {
       assertTrue(this.correctAmdMetsFile.exists());
       assertTrue(this.incorrectAmdMetsFile.exists());
    }

    @Test
    public void validate() throws ParserConfigurationException {
        ValidateCreatingApplicationNameAndVersion target = new ValidateCreatingApplicationNameAndVersion(CDM_ID, new File(amdSecFolder));
        final List<String> errorMessages = target.validateAmdMets();
        assertEquals(EXPECTED_COUNT_OF_ERRROR_MESSAGES, errorMessages.size());
        assertTrue(errorMessages.contains(EXPECTED_APPLICATION_NAME_ERRROR_MESSAGE));
        assertTrue(errorMessages.contains(EXPECTED_APPLICATION_VERSION_ERROR_MESSAGE));
    }

}
