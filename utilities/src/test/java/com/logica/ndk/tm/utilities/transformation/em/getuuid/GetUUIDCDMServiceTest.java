package com.logica.ndk.tm.utilities.transformation.em.getuuid;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.utilities.integration.wf.TaskFinder;
import com.logica.ndk.tm.utilities.integration.wf.exception.BadRequestException;
import com.logica.ndk.tm.utilities.integration.wf.task.TaskHeader;
import com.logica.ndk.tm.utilities.integration.wf.task.TaskHeaderBuilder;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.Activity;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;
import com.logica.ndk.tm.utilities.transformation.em.GetUUIDImpl;
import com.logica.ndk.tm.utilities.transformation.em.UUID;
import com.logica.ndk.tm.utilities.transformation.em.UUIDWrapper;
import com.logica.ndk.tm.utilities.transformation.em.getuuid.cdm.TaskFinderBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author krchnacekm
 */
public final class GetUUIDCDMServiceTest extends GetUUIDAbstractTest {

  private static final String SAMPLE_CDM_ID = "89693cb0-3c81-11e3-afd2-00505682629d";
  private static final String SAMPLE_METS_FILE_NAME = "METS_89693cb0-3c81-11e3-afd2-00505682629d.xml";
  private static final String CDM_ID_FOR_MERGE_TEST = "89693cb0-3c81-11e3-afd2-000000000000";
  private static final String CDM_ID_FOR_MERGE_TEST_FILE_NAME = "METS_89693cb0-3c81-11e3-afd2-000000000000.xml";
  private static final String EXPECTED_TITLE_UUID = "aaaa8ba0-3c82-11e3-afd2-00505682629d";
  private static final String EXPECTED_VOLUME_UUID = "38ca8ba0-3c82-11e3-afd2-00505682629d";
  private static final String EXPECTED_TITLE = "Áčko";
  private static final String EXPECTED_SOURCE_OF_RECORD = "CDM";
  private static final String CORRECT_PART_NUMBER = "1";
  private static final String INCORRECT_PART_NUMBER = "455";

  @Before
  public void setUp() {
    this.wfClientMock = mock(WFClient.class);
    this.cdmMock = mock(CDM.class);
    this.mockedUUIDResult.clear();
    this.target = new GetUUIDImpl(wfClientMock, cdmMock, new CDMMetsHelper());
    this.target.setUseUUIDGenerator(false);

    final File metsFile = new File(this.getClass().getResource(SAMPLE_METS_FILE_NAME).getPath());
    Assert.assertNotNull("Mets file have to be non null.", metsFile);
    Assert.assertTrue(String.format("MetsFile %s dont't exist", metsFile), metsFile.exists());
    doReturn(metsFile).when(cdmMock).getMetsFile(SAMPLE_CDM_ID);

    final File metsForMergeTest = new File(this.getClass().getResource(CDM_ID_FOR_MERGE_TEST_FILE_NAME).getPath());
    Assert.assertNotNull("metsForMergeTest have to be non null.", metsForMergeTest);
    Assert.assertTrue(String.format("metsForMergeTest %s dont't exist", metsForMergeTest), metsForMergeTest.exists());
    doReturn(metsForMergeTest).when(cdmMock).getMetsFile(CDM_ID_FOR_MERGE_TEST);
  }

  @Test
  public void testGetVolumeUUID() throws TransformerException, IOException, BadRequestException {
      final String expectedVolumeNumber = "1";

    final TaskHeader taskHeader = new TaskHeaderBuilder().setCdmId(SAMPLE_CDM_ID).setId(Long.valueOf(0)).setActivityCode(Activity.KONTROLA.getValue()).build();
    final List<TaskHeader> taskHeaders = new ListBuilder<TaskHeader>().add(taskHeader).build();
    final TaskFinder taskFinder = new TaskFinderBuilder().setRecordIdentifier(RECORD_IDENTIFIER).build();

    doReturn(taskHeaders).when(this.wfClientMock).getTasks(taskFinder);

      final List<UUID> result = this.target.execute(RECORD_IDENTIFIER, null, null, expectedVolumeNumber, VOLUME_TYPE).getUuidsList();

    assertEquals(1, result.size());
    final UUID firstItemInList = result.get(FIRST_ITEM_IN_LIST);
    assertEquals(EXPECTED_TITLE, firstItemInList.getTitle());
    assertEquals(EXPECTED_VOLUME_UUID, firstItemInList.getValue());
    assertEquals(EXPECTED_SOURCE_OF_RECORD, firstItemInList.getSource());
    final String errorMessage = String.format("Link variable of first item in list of results should have prefix \"%s\". It's value is \"%s\"", EXPECTED_LINK_PREFIX, firstItemInList.getLink());
    assertTrue(errorMessage, firstItemInList.getLink().startsWith(EXPECTED_LINK_PREFIX));
    assertEquals(EXPECTED_LINK_PREFIX.length() + 1, firstItemInList.getLink().length());
    assertEquals(expectedVolumeNumber, firstItemInList.getVolumeNumber());
    Mockito.verify(wfClientMock).getTasks(Mockito.eq(taskFinder));

  }

  @Test
  public void testGetTitleUUID() throws TransformerException, IOException, BadRequestException {
    final int expectedCountOfReturnedUUIDs = 1;

    final TaskHeader taskHeader = new TaskHeaderBuilder().setCdmId(SAMPLE_CDM_ID).setId(Long.valueOf(0)).setActivityCode(Activity.KONTROLA.getValue()).build();
    final List<TaskHeader> taskHeaders = new ListBuilder<TaskHeader>().add(taskHeader).build();
    final TaskFinder taskFinder = new TaskFinderBuilder().setRecordIdentifier(RECORD_IDENTIFIER).build();

    doReturn(taskHeaders).when(this.wfClientMock).getTasks(taskFinder);

    final List<UUID> result = target.execute(RECORD_IDENTIFIER, null, null, null, TITLE_TYPE).getUuidsList();

    assertEquals(expectedCountOfReturnedUUIDs, result.size());

    final UUID uuidItem = result.get(FIRST_ITEM_IN_LIST);
    if (EXPECTED_TITLE_UUID.equals(uuidItem.getValue())) {
      assertEquals(EXPECTED_TITLE, uuidItem.getTitle());
      assertEquals(EXPECTED_TITLE_UUID, uuidItem.getValue());
      assertEquals(EXPECTED_SOURCE_OF_RECORD, uuidItem.getSource());
      assertTrue(result.get(FIRST_ITEM_IN_LIST).getLink().startsWith(EXPECTED_LINK_PREFIX));
      assertEquals(EXPECTED_LINK_PREFIX.length() + 1, result.get(FIRST_ITEM_IN_LIST).getLink().length());

      Mockito.verify(wfClientMock).getTasks(Mockito.eq(taskFinder));
    }
  }

  @Test
  @Override
  public void testRecordNotExistGenerateUUIDEnabled() {
    this.target.setUseUUIDGenerator(true);
    super.testRecordNotExistGenerateUUIDEnabled();
  }

  @Test
  @Override
  public void testRecordNotExistGenerateUUIDDisabled() {
    super.testRecordNotExistGenerateUUIDDisabled();
  }

  @Test
  public void testWfClientForMergeCdmIdResults() throws TransformerException, IOException, BadRequestException {

    final int expectedCountOfMergedUUIDs = 1;
    final List<TaskHeader> taskHeaderReturnedForAllParameters = new ArrayList<TaskHeader>();
    taskHeaderReturnedForAllParameters.add(new TaskHeaderBuilder().setCdmId("89693cb0-3c81-11e3-afd2-000000000000").setActivityCode(Activity.KONTROLA.getValue()).build());

    final TaskFinder finder = new TaskFinderBuilder().setCcnb("cnb000000000").setRecordIdentifier("nkc00000000000").setIssn("issn000000000").setVolumeNumber(CORRECT_PART_NUMBER).build();
    doReturn(taskHeaderReturnedForAllParameters).when(wfClientMock).getTasks(new TaskFinderBuilder().setCcnb(finder.getCcnb()).build());
    doReturn(taskHeaderReturnedForAllParameters).when(wfClientMock).getTasks(new TaskFinderBuilder().setRecordIdentifier(finder.getRecordIdentifier()).build());
    doReturn(taskHeaderReturnedForAllParameters).when(wfClientMock).getTasks(new TaskFinderBuilder().setIssn(finder.getIssn()).build());
    doReturn(taskHeaderReturnedForAllParameters).when(wfClientMock).getTasks(new TaskFinderBuilder().setVolumeNumber(finder.getVolumeNumber()).build());

    final UUIDWrapper utilityResult = this.runUtility(finder, VOLUME_TYPE);
    Assert.assertNotNull(utilityResult);
    Assert.assertEquals("WfClient have to return three same cdmIds, but after merging, there should be only one returned cdmId.", expectedCountOfMergedUUIDs, utilityResult.getUuidsList().size());

    Mockito.verify(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setCcnb(finder.getCcnb()).build()));
    Mockito.verify(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setRecordIdentifier(finder.getRecordIdentifier()).build()));
    Mockito.verify(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setIssn(finder.getIssn()).build()));
    Mockito.verify(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setVolumeNumber(finder.getVolumeNumber()).build()));
  }

  @Test
  public void testWfClientForMergeCdmIdResultsTitle() throws TransformerException, IOException, BadRequestException {
    final TaskFinder finder = new TaskFinderBuilder().setCcnb("cnb000000000").setRecordIdentifier("nkc00000000000").setIssn("issn000000000").setVolumeNumber(CORRECT_PART_NUMBER).build();
    final UUID uuid = this.findUUID(finder, TITLE_TYPE).getUuidsList().get(FIRST_ITEM_IN_LIST);
    Assert.assertEquals("aaaa8ba0-3c82-11e3-afd2-00505682629d", uuid.getValue());
    Assert.assertEquals(EXPECTED_SOURCE_OF_RECORD, uuid.getSource());

    Mockito.verify(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setCcnb(finder.getCcnb()).build()));
    Mockito.verify(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setRecordIdentifier(finder.getRecordIdentifier()).build()));
    Mockito.verify(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setIssn(finder.getIssn()).build()));
    Mockito.verify(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setVolumeNumber(finder.getVolumeNumber()).build()));
  }

  @Test
  public void testWfClientForMergeCdmIdResultsVolume() throws TransformerException, IOException, BadRequestException {
    final TaskFinder finder = new TaskFinderBuilder().setCcnb("cnb000000000").setRecordIdentifier("nkc00000000000").setIssn("issn000000000").setVolumeNumber(CORRECT_PART_NUMBER).build();
    final UUID uuid = this.findUUID(finder, VOLUME_TYPE).getUuidsList().get(FIRST_ITEM_IN_LIST);
    Assert.assertEquals("38ca8ba0-3c82-11e3-afd2-00505682629d", uuid.getValue());
    Assert.assertEquals(EXPECTED_SOURCE_OF_RECORD, uuid.getSource());

    Mockito.verify(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setCcnb(finder.getCcnb()).build()));
    Mockito.verify(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setRecordIdentifier(finder.getRecordIdentifier()).build()));
    Mockito.verify(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setIssn(finder.getIssn()).build()));
    Mockito.verify(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setVolumeNumber(finder.getVolumeNumber()).build()));
  }

  private UUIDWrapper findUUID(TaskFinder finder, String uuidType) throws TransformerException, IOException, BadRequestException {
    final List<TaskHeader> taskHeaderReturnedForAllParameters = new ArrayList<TaskHeader>();
    taskHeaderReturnedForAllParameters.add(new TaskHeaderBuilder().setCdmId("89693cb0-3c81-11e3-afd2-000000000000").setActivityCode(Activity.KONTROLA.getValue()).build());

    doReturn(taskHeaderReturnedForAllParameters).when(wfClientMock).getTasks(new TaskFinderBuilder().setCcnb(finder.getCcnb()).build());
    doReturn(taskHeaderReturnedForAllParameters).when(wfClientMock).getTasks(new TaskFinderBuilder().setRecordIdentifier(finder.getRecordIdentifier()).build());
    doReturn(taskHeaderReturnedForAllParameters).when(wfClientMock).getTasks(new TaskFinderBuilder().setIssn(finder.getIssn()).build());
    doReturn(taskHeaderReturnedForAllParameters).when(wfClientMock).getTasks(new TaskFinderBuilder().setVolumeNumber(finder.getVolumeNumber()).build());

    return this.runUtility(finder, uuidType);
  }

  @Test
  public void testWfClientForMergeCdmIdResults2() throws TransformerException, IOException, BadRequestException {
    final int expectedCountOfMergedUUIDs = 0;
    final TaskFinder finder = new TaskFinderBuilder().setCcnb("cnb000000001").setRecordIdentifier("nkc00000000001").setIssn("issn000000001").setVolumeNumber(CORRECT_PART_NUMBER).build();

    final List<TaskHeader> taskHeaderReturnedForAllParameters = new ArrayList<TaskHeader>();
    taskHeaderReturnedForAllParameters.add(new TaskHeaderBuilder().setCdmId("89693cb0-3c81-11e3-afd2-000000000001").setActivityCode(Activity.KONTROLA.getValue()).build());

    final List<TaskHeader> emptyTaskHeaders = new ArrayList<TaskHeader>();

    doReturn(emptyTaskHeaders).when(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setCcnb(finder.getCcnb()).build()));
    doReturn(taskHeaderReturnedForAllParameters).when(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setRecordIdentifier(finder.getRecordIdentifier()).build()));
    doReturn(emptyTaskHeaders).when(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setIssn(finder.getIssn()).build()));
    doReturn(taskHeaderReturnedForAllParameters).when(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setVolumeNumber(finder.getVolumeNumber()).build()));

    final UUIDWrapper utilityResult = this.runUtility(finder);
    Assert.assertNotNull(utilityResult);
    Assert.assertEquals("One of the result from WFClient is not equals to other, result after merging have to be empty list. (AND)", expectedCountOfMergedUUIDs, utilityResult.getUuidsList().size());

    Mockito.verify(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setCcnb(finder.getCcnb()).build()));
    Mockito.verify(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setRecordIdentifier(finder.getRecordIdentifier()).build()));
    Mockito.verify(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setIssn(finder.getIssn()).build()));
    Mockito.verify(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setVolumeNumber(finder.getVolumeNumber()).build()));

  }

  @Test
  public void testFilterOfActitivityStates() throws TransformerException, IOException, BadRequestException {
    final int expectedCountOfMergedUUIDs = 0;
    final String cdmIdReturnedForAllParameters = "89693cb0-3c81-11e3-afd2-000000000000";
    final TaskFinder finder = new TaskFinderBuilder().setCcnb("cnb000000000").setRecordIdentifier("nkc00000000000").setIssn("issn000000000").setVolumeNumber(CORRECT_PART_NUMBER).build();

    final List<TaskHeader> getTasksResult = new ArrayList<TaskHeader>();
    getTasksResult.add(new TaskHeaderBuilder().setCdmId(cdmIdReturnedForAllParameters).setActivityCode(Activity.EDITACNI_MODUL.getValue()).build());

    doReturn(getTasksResult).when(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setCcnb(finder.getCcnb()).build()));
    doReturn(getTasksResult).when(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setRecordIdentifier(finder.getRecordIdentifier()).build()));
    doReturn(getTasksResult).when(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setIssn(finder.getIssn()).build()));
    doReturn(getTasksResult).when(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setVolumeNumber(finder.getVolumeNumber()).build()));

    final UUIDWrapper utilityResult = this.runUtility(finder);
    Assert.assertNotNull(utilityResult);
    Assert.assertEquals("Utility result have to be empty, because cdm returner by wfclient are in forbidden activity", expectedCountOfMergedUUIDs, utilityResult.getUuidsList().size());

    Mockito.verify(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setCcnb(finder.getCcnb()).build()));
    Mockito.verify(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setRecordIdentifier(finder.getRecordIdentifier()).build()));
    Mockito.verify(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setIssn(finder.getIssn()).build()));
    Mockito.verify(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setVolumeNumber(finder.getVolumeNumber()).build()));
  }

  @Ignore
  public void testFilterOfIncorrectPartNumber() throws TransformerException, IOException, BadRequestException {
    final TaskFinder finder = new TaskFinderBuilder().setCcnb("cnb000000000").setRecordIdentifier("nkc00000000000").setIssn("issn000000000").setVolumeNumber(INCORRECT_PART_NUMBER).build();
    assertEquals(1, this.findUUID(finder, VOLUME_TYPE).getUuidsList().size());

    Mockito.verify(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setCcnb(finder.getCcnb()).build()));
    Mockito.verify(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setRecordIdentifier(finder.getRecordIdentifier()).build()));
    Mockito.verify(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setIssn(finder.getIssn()).build()));
    Mockito.verify(wfClientMock).getTasks(Mockito.eq(new TaskFinderBuilder().setVolumeNumber(finder.getVolumeNumber()).build()));
  }

  private UUIDWrapper runUtility(TaskFinder finder) {
    return this.runUtility(finder, VOLUME_TYPE);
  }

  private UUIDWrapper runUtility(TaskFinder finder, String uuidType) {
    return this.target.execute(finder.getRecordIdentifier(), finder.getCcnb(), finder.getIssn(), finder.getVolumeNumber(), uuidType);
  }
}
