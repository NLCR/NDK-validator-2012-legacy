package com.logica.ndk.tm.utilities.transformation.em.getuuid;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.integration.wf.UUIDFinder;
import com.logica.ndk.tm.utilities.integration.wf.exception.BadRequestException;
import com.logica.ndk.tm.utilities.integration.wf.task.UUIDResult;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;

import com.logica.ndk.tm.utilities.transformation.em.GetUUIDImpl;
import com.logica.ndk.tm.utilities.transformation.em.UUID;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author krchnacekm
 */
public final class GetUUIDWFServiceTest extends GetUUIDAbstractTest {

    private static final int EXPECTED_COUNT_OF_RETURNED_UUIDS = 2;
    private static final String EXPECTED_TITLE = "Bratrstvo";
    private static final String EXPECTED_VOLUME_UUID = "02120310-149e-11e3-892d-00505682629d";
    private static final String EXPECTED_TITLE_UUID = "a123";
    private static final String EXPECTED_SOURCE_OF_RECORD = "WF";
    private static final String BLACKLISTED_VOLUME_UUID = "02120310-0000-11e3-892d-20505682629d";
    public static final String EXPECTED_VOLUME_NUMBER = "2013";

    @Before
    public void setUp() throws IOException, BadRequestException {
        this.wfClientMock = mock(WFClient.class);

        doReturn(mockedUUIDResult).when(wfClientMock).getUUIDs(any(UUIDFinder.class));
       
        this.cdmMock = mock(CDM.class);

        this.target = new GetUUIDImpl(wfClientMock, cdmMock, new CDMMetsHelper());
        this.target.setUseUUIDGenerator(true);
        
        // Some tests removes list of values returned by getUUIDsMock. 
        // So setUp have to initialize this list before calling of each test method.
        mockedUUIDResult.clear();
        mockedUUIDResult.addAll(getUUIDsMockResult());

        //final String dummyMetsFile = "METS_89693cb0-3c81-11e3-afd2-00505682629d.xml";
        //URL resource = this.getClass().getResource(dummyMetsFile);
        //doReturn(new File(resource.getPath())).when(cdmMock).getMetsFile(Mockito.anyString());
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testGetVolumeUUID() {
        final List<UUID> result = target.execute(RECORD_IDENTIFIER, null, null, EXPECTED_VOLUME_NUMBER, VOLUME_TYPE).getUuidsList();
        assertEquals(EXPECTED_COUNT_OF_RETURNED_UUIDS, result.size());
        boolean itemFounded = false;
        for (UUID uuidItem : result) {
            if (EXPECTED_VOLUME_UUID.equals(uuidItem.getValue())) {
                itemFounded = true;
                assertEquals(EXPECTED_TITLE, result.get(FIRST_ITEM_IN_LIST).getTitle());
                assertEquals(EXPECTED_VOLUME_UUID, result.get(FIRST_ITEM_IN_LIST).getValue());
                assertEquals(EXPECTED_SOURCE_OF_RECORD, result.get(FIRST_ITEM_IN_LIST).getSource());
                assertTrue(result.get(FIRST_ITEM_IN_LIST).getLink().startsWith(EXPECTED_LINK_PREFIX));
                assertEquals(EXPECTED_LINK_PREFIX.length() + 1, result.get(FIRST_ITEM_IN_LIST).getLink().length());
                assertEquals(EXPECTED_VOLUME_NUMBER, result.get(FIRST_ITEM_IN_LIST).getVolumeNumber());
            }
        }
        assertTrue(itemFounded);
    }

    /**
     * Method have to returns UUIDResult objects which will be returned by
     * WFClient mock. Each test case requires different UUIDs returned from
     * WFClient mock. So all inherited tests have to implement own version of
     * this method.
     *
     * @return list of dummy UUIDResult objects
     */
    private List<UUIDResult> getUUIDsMockResult() {
        final List<UUIDResult> result = new ArrayList<UUIDResult>();

        final UUIDResult bratrstvoUUIDItem = new UUIDResult();
        bratrstvoUUIDItem.setTitleUUID(EXPECTED_TITLE_UUID);
        bratrstvoUUIDItem.setVolumeUUID(EXPECTED_VOLUME_UUID);
        bratrstvoUUIDItem.setTitle(EXPECTED_TITLE);
        bratrstvoUUIDItem.setVolumeNumber(EXPECTED_VOLUME_NUMBER);
        bratrstvoUUIDItem.setId(Long.valueOf(1));
        result.add(bratrstvoUUIDItem);

        final UUIDResult secondUUIDItem = new UUIDResult();
        secondUUIDItem.setTitleUUID(EXPECTED_TITLE_UUID);
        secondUUIDItem.setVolumeUUID(EXPECTED_VOLUME_UUID);
        secondUUIDItem.setTitle("something completely different");
        secondUUIDItem.setVolumeNumber(EXPECTED_VOLUME_NUMBER);
        secondUUIDItem.setId(Long.valueOf(2));
        result.add(secondUUIDItem);

        final UUIDResult uuidItemWithDifferentUuid = new UUIDResult();
        uuidItemWithDifferentUuid.setTitleUUID("b223");
        uuidItemWithDifferentUuid.setVolumeUUID("02120310-549e-11e3-892d-20505682629d");
        uuidItemWithDifferentUuid.setTitle("some title");
        uuidItemWithDifferentUuid.setVolumeNumber(EXPECTED_VOLUME_NUMBER);
        uuidItemWithDifferentUuid.setId(Long.valueOf(3));
        result.add(uuidItemWithDifferentUuid);

        return result;
    }

    @Test
    public void testGetVolumeUUIDReturnedUUIDObjectDoesntContainIDValue() throws Exception {
        mockedUUIDResult.clear();
        final UUIDResult bratrstvoUUIDItem = new UUIDResult();
        bratrstvoUUIDItem.setTitleUUID(EXPECTED_TITLE_UUID);
        bratrstvoUUIDItem.setVolumeUUID(EXPECTED_VOLUME_UUID);
        bratrstvoUUIDItem.setTitle(EXPECTED_TITLE);
        bratrstvoUUIDItem.setVolumeNumber(EXPECTED_VOLUME_NUMBER);
        bratrstvoUUIDItem.setId(null);
        mockedUUIDResult.add(bratrstvoUUIDItem);

        final List<UUID> result = target.execute(RECORD_IDENTIFIER, null, null, EXPECTED_VOLUME_NUMBER, VOLUME_TYPE).getUuidsList();

        boolean itemFounded = false;
        for (UUID uuidItem : result) {
            if (EXPECTED_VOLUME_UUID.equals(uuidItem.getValue())) {
                itemFounded = true;
                assertEquals(EXPECTED_TITLE, result.get(FIRST_ITEM_IN_LIST).getTitle());
                assertEquals(EXPECTED_VOLUME_UUID, result.get(FIRST_ITEM_IN_LIST).getValue());
                assertEquals(EXPECTED_SOURCE_OF_RECORD, result.get(FIRST_ITEM_IN_LIST).getSource());
                assertEquals(EMPTY_STRING, result.get(FIRST_ITEM_IN_LIST).getLink());
            }
        }

        assertTrue(itemFounded);
    }

    @Test
    public void testGetTitleUUID() {
        final List<UUID> result = target.execute(RECORD_IDENTIFIER, null, null, null, TITLE_TYPE).getUuidsList();

        assertEquals(EXPECTED_COUNT_OF_RETURNED_UUIDS, result.size());

        boolean itemFounded = false;
        for (UUID uuidItem : result) {
            if (EXPECTED_TITLE_UUID.equals(uuidItem.getValue())) {
                itemFounded = true;
                assertEquals(EXPECTED_TITLE, uuidItem.getTitle());
                assertEquals(EXPECTED_TITLE_UUID, uuidItem.getValue());
                assertEquals(EXPECTED_SOURCE_OF_RECORD, uuidItem.getSource());
                assertTrue(result.get(FIRST_ITEM_IN_LIST).getLink().startsWith(EXPECTED_LINK_PREFIX));
                assertEquals(EXPECTED_LINK_PREFIX.length() + 1, result.get(FIRST_ITEM_IN_LIST).getLink().length());
                assertEquals(EXPECTED_VOLUME_NUMBER, result.get(FIRST_ITEM_IN_LIST).getVolumeNumber());
            }
        }

        assertTrue(itemFounded);
    }

    @Test
    public void testReadPropertyInTmConfigDefaults() {
        final String WFLINK_PROPERTY_KEY = "jbpmws.wfLink";
        assertEquals(EXPECTED_LINK_PREFIX, TmConfig.instance().getString(WFLINK_PROPERTY_KEY));
    }
}
