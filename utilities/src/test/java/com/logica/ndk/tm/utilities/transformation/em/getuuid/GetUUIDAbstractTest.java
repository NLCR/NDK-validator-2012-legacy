package com.logica.ndk.tm.utilities.transformation.em.getuuid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import com.logica.ndk.tm.utilities.transformation.em.UUID;
import com.logica.ndk.tm.utilities.transformation.em.UUIDWrapper;
import com.logica.ndk.tm.utilities.transformation.em.GetUUIDImpl;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.integration.wf.task.UUIDResult;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;

/**
 * @author krchnacekm
 */
public abstract class GetUUIDAbstractTest {

  protected static final String RECORD_IDENTIFIER = "nkc20122430296";
  protected static final int FIRST_ITEM_IN_LIST = 0;
  protected static final String EMPTY_STRING = "";
  protected static final String VOLUME_TYPE = "volume";
  protected static final String TITLE_TYPE = "title";
  protected static final String GENERATED_SOURCE_OF_RECORD = "vygenerováno";
  protected static final String INCORRECT_RECORD_IDENTIFIER = "nuihgfguyfuyf";
  protected static final String EXPECTED_LINK_PREFIX = "http://hastest:9090/safe/PersistentObjectAction.do?id=";
  /*
   * Utility GetUUID have to use WFClient. Utility is looking for uuid and
   * cdmId in WFClient, so unit tests have to inject the mock of WFClient into
   * the utility.
   */
  protected WFClient wfClientMock;
  /**
   * The mock of WFClient have to returns list of UUIDResult objects. This
   * emulates getting uuids from WF.
   */
  protected List<UUIDResult> mockedUUIDResult = new ArrayList<UUIDResult>();
  /**
   * Utility also have to use CDM, so tests have to inject the CDM mock into
   * the utility.
   */
  protected CDM cdmMock;
  /**
   * Variable target contains reference to the tested object.
   */
  protected GetUUIDImpl target;

  /**
   * Utility have to return non nullable result if all input values are null.
   */
  @Ignore
  public void testIllegalArguments() {
    this.mockedUUIDResult.clear();

    this.target.setUseUUIDGenerator(true);
    assertEquals(1, target.execute(null, null, null, null, null).getUuidsList().size());

    this.target.setUseUUIDGenerator(false);
    assertTrue(target.execute(null, null, null, null, null).getUuidsList().isEmpty());
  }

  /**
   * Utility have to return non nullable result if all input values are empty.
   */
  @Ignore
  public void testCallingWithEmptyArguments() {
    this.mockedUUIDResult.clear();

    this.target.setUseUUIDGenerator(true);
    assertEquals(1, target.execute(EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING).getUuidsList().size());

    this.target.setUseUUIDGenerator(false);
    assertTrue(target.execute(EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING).getUuidsList().isEmpty());
  }

  /**
   * If record doesn't exist in WF. Utility have to generate one new UUID value.
   * 
   * @throws Exception
   */
  @Test
  public void testRecordNotExistGenerateUUIDEnabled() {
    final int expectedCountOfReturnedValues = 1;

    this.target.setUseUUIDGenerator(true);
    mockedUUIDResult.clear();

    final List<UUID> uuidsList = target.execute(INCORRECT_RECORD_IDENTIFIER, null, null, null, TITLE_TYPE).getUuidsList();
    assertEquals(expectedCountOfReturnedValues, uuidsList.size());

    final UUID result = uuidsList.get(FIRST_ITEM_IN_LIST);
    assertEquals(String.format("Source of result item have to be \"%s\", because any uuid wasn't found and uuid generator is enabled. Actual source is: \"%s\"", GENERATED_SOURCE_OF_RECORD, result.getSource()), GENERATED_SOURCE_OF_RECORD, result.getSource());
  }

  /**
   * If any uuid is not found and uuid generator is disabled. Result should be empty list.
   */
  @Test
  public void testRecordNotExistGenerateUUIDDisabled() {
    mockedUUIDResult.clear();
    target.setUseUUIDGenerator(false);

    final UUIDWrapper foundedUUIDs = target.execute(INCORRECT_RECORD_IDENTIFIER, null, null, null, TITLE_TYPE);
    assertTrue(foundedUUIDs.getUuidsList().isEmpty());
  }
}
