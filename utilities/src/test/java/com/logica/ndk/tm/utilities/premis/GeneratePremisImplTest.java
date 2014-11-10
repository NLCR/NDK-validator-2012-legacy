package com.logica.ndk.tm.utilities.premis;


import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.XPath;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.logica.ndk.tm.utilities.CDMUtilityTest;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord.Operation;

@Ignore
public class GeneratePremisImplTest extends CDMUtilityTest {

  private final GeneratePremisImpl generatePremis = new GeneratePremisImpl();

  private File file1;
  private File file2;

  @Before
  public void setUp() throws Exception {
    setUpEmptyCdm();
    file1 = new File(cdm.getMasterCopyDir(CDM_ID_EMPTY), "file1.tif.jp2");
    file2 = new File(cdm.getMasterCopyDir(CDM_ID_EMPTY), "file2.tif.jp2");
  }

  @After
  public void tearDown() throws Exception {
    deleteEmptyCdm();
  }

  @Ignore
  public final void testExecute() throws Exception {
    final GregorianCalendar calendar = new GregorianCalendar();
    calendar.add(Calendar.MINUTE, -3);
    final PremisCsvRecord record1 = new PremisCsvRecord(
        calendar.getTime(),
        "utility",
        "utilityVersion",
        Operation.convert_image,
        "MC",
        "agent",
        "agentVersion",
        "agentNote",
        "agentRole",
        file1,
        PremisCsvRecord.OperationStatus.OK,
        "image/tiff",
        "fmt/353",
        "deleted");

    calendar.add(Calendar.MINUTE, -3);
    final PremisCsvRecord record2 = new PremisCsvRecord(
        calendar.getTime(),
        "utility",
        "utilityVersion",
        Operation.convert_image,
        "MC",
        "agent",
        "agentVersion",
        "agentNote",
        "agentRole",
        file2,
        PremisCsvRecord.OperationStatus.OK,
        "image/jp2",
        "fmt/151",
        "preservation");

    calendar.add(Calendar.MINUTE, -3);
    final PremisCsvRecord record3 = new PremisCsvRecord(
        calendar.getTime(),
        "utility",
        "utilityVersion",
        Operation.convert_image,
        "MC",
        "agentBond",
        "agentVersion",
        "agentNote",
        "agentRole",
        file2,
        PremisCsvRecord.OperationStatus.OK,
        "image/jp2",
        "fmt/151",
        "preservation");

    calendar.add(Calendar.MINUTE, -3);
    final PremisCsvRecord record4 = new PremisCsvRecord(
        calendar.getTime(),
        "utility",
        "utilityVersion",
        Operation.convert_image,
        "MC",
        "agentBond",
        "agentVersion",
        "agentNote",
        "agentRole",
        file1,
        PremisCsvRecord.OperationStatus.FAILED,
        "fmt/101",
        "text/xml",
        "preservation");

    cdm.addValidationEvent(CDM_ID_EMPTY, record1);
    cdm.addValidationEvent(CDM_ID_EMPTY, record2);
    cdm.addValidationEvent(CDM_ID_EMPTY, record3);

    cdm.addTransformationEvent(CDM_ID_EMPTY, record4, null);
    cdm.addTransformationEvent(CDM_ID_EMPTY, record4, null);

    FileUtils.touch(file1);
    FileUtils.write(file1, "Test data file 1");
    FileUtils.touch(file2);
    FileUtils.write(file2, "Test data file 2");

    generatePremis.execute(CDM_ID_EMPTY);

    assertThat(cdm.getPremisDir(CDM_ID_EMPTY))
        .isNotNull()
        .exists();

    final Document document = DocumentHelper.parseText(FileUtils.readFileToString(new File(cdm.getPremisDir(CDM_ID_EMPTY), "PREMIS_file1.xml")));
    assertThat(document).isNotNull();

    final ImmutableMap<String, String> namespace = ImmutableMap.<String, String> of("ns", "info:lc/xmlns/premis-v2");
    final XPath objectXPath = DocumentHelper.createXPath("//ns:object");
    objectXPath.setNamespaceURIs(namespace);
    assertThat(objectXPath.selectNodes(document))
        .isNotNull()
        .isNotEmpty()
        .hasSize(1);
    final XPath eventXPath = DocumentHelper.createXPath("//ns:event");
    eventXPath.setNamespaceURIs(namespace);
    assertThat(eventXPath.selectNodes(document))
        .isNotNull()
        .isNotEmpty()
        .hasSize(2);
    final XPath agentXPath = DocumentHelper.createXPath("//ns:agent");
    agentXPath.setNamespaceURIs(namespace);
    assertThat(agentXPath.selectNodes(document))
        .isNotNull()
        .isNotEmpty()
        .hasSize(2);
  }


}
