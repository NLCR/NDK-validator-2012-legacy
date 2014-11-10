package com.logica.ndk.tm.cdm;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.premis.PremisConstants;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord.Operation;

@Ignore
public class CDMaddPremisEventsTest {

  private static final String CDM_ID = CDMaddPremisEventsTest.class.getSimpleName();

  private final CDM cdm = new CDM();
  private String eventDir;
  private PremisCsvRecord record;

  @Before
  public void setUp() throws Exception {
    cdm.createEmptyCdm(CDM_ID, true);
    eventDir = new CDMSchema().getMasterCopyDirName();
    record = new PremisCsvRecord(
        new Date(),
        "utility",
        "utilityVersion",
        Operation.create_config,
        eventDir,
        "agent",
        "agentVersion",
        "agentNote",
        "agentRole",
        new File(cdm.getCdmDir(CDM_ID), "file"),
        PremisCsvRecord.OperationStatus.OK,
        "image/jp2",
        "x-cmp/12",
        "preservation"); 
  }

  @After
  public void tearDown() throws Exception {
    cdm.zapCdm(CDM_ID);
  }

  @Ignore
  public void testAddValidationEvent() throws Exception {

    cdm.addValidationEvent(CDM_ID, record);
    cdm.addValidationEvent(CDM_ID, record);
    cdm.addValidationEvent(CDM_ID, record);

    final File premisEvensCsvFile = new File(cdm.getWorkspaceDir(CDM_ID), PremisConstants.VALIDATIONS_DIR + "/" + eventDir + ".csv");
    assertThat(premisEvensCsvFile)
        .isNotNull()
        .exists();

    final List<String> lines = FileUtils.readLines(premisEvensCsvFile);
    assertThat(lines)
        .isNotNull()
        .isNotEmpty()
        .hasSize(5); // 1 comment, 1 header, 3 records
  }

  @Ignore
  public void testAddTransformationEvent() throws Exception {

    cdm.addTransformationEvent(CDM_ID, record, null);
    cdm.addTransformationEvent(CDM_ID, record, null);
    cdm.addTransformationEvent(CDM_ID, record, null);

    final File premisEvensCsvFile = new File(cdm.getWorkspaceDir(CDM_ID), PremisConstants.TRANSFORMATIONS_DIR + "/" + eventDir + ".csv");
    assertThat(premisEvensCsvFile)
        .isNotNull()
        .exists();

    final List<String> lines = FileUtils.readLines(premisEvensCsvFile);
    assertThat(lines)
        .isNotNull()
        .isNotEmpty()
        .hasSize(5); // 1 comment, 1 header, 3 records
  }

}
