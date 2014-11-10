package com.logica.ndk.tm.cdm;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * @author krchnacekm
 */
public class ImportFromLTPHelperTest {

    private static final String CDM_ID = "7f3536b0-27f9-11e2-a433-005056827e52";

    @Test
    public void testIsFromLTPFlagExist() throws Exception {
        final boolean result = ImportFromLTPHelper.isFromLTPFlagExist(CDM_ID);
        assertFalse(result);
    }
}
