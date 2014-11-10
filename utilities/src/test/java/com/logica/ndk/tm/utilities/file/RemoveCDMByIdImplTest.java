package com.logica.ndk.tm.utilities.file;

import com.logica.ndk.tm.utilities.CDMUtilityTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * 
 * @author brizat
 */
@Ignore
public class RemoveCDMByIdImplTest extends CDMUtilityTest {

    private static final String cdmId = "common";

    @Before
    public void prepareTest() throws Exception {
        setUpCdmById(cdmId);
    }

    @Ignore
    public void test() {
        RemoveCDMByIdImpl removeCDMByIdImpl = new RemoveCDMByIdImpl();

        removeCDMByIdImpl.execute(cdmId);
    }

    @After
    public void cleanAfterTest() throws Exception {
        deleteCdmById(cdmId);
    }
}
