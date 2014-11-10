package com.logica.ndk.tm.utilities.integration.aleph.notification;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Properties;

import javax.xml.xpath.XPathConstants;

import org.aspectj.lang.annotation.Before;
import org.junit.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.apsr.mtk.base.File;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.CDMUtilityTest;

/**
 * @author krchnacekm
 */
@Ignore
public class CreateAlephRecordImplTest extends CDMUtilityTest {
   /*
  private static final Logger log = LoggerFactory.getLogger(CreateAlephRecordImplTest.class);
  private final String outputDirectoryPath = "C:\\Users\\krchnacekm\\AppData\\Local\\Temp\\cdm\\CDM_common\\data\\.workspace\\alephNotification";
  private String docNumber = "000358463"; //"64ed6d38-c472-4b0d-96cd-b256e9c5efa1";
  private String cdmId = "common";
  private CreateAlephRecordImpl target;
  private CDM mockedCdm = mock(CDM.class);
  private XPathParser xPathParser;
  private String outputFileName;
  private String libraryCode;
  private Properties properties;

  @Before
  public void before() throws Exception {
    setUpCdmById(cdmId);

    properties = new Properties();
    properties.put("barCode", docNumber);
    properties.put("uuid", "3b7b80c0-21d0-11e2-89b1-005056827e52");
    when(mockedCdm.getCdmProperties(cdmId)).thenReturn(properties);

    target = new CreateAlephRecordImpl();
    target.setCdm(mockedCdm);
  }

  @Test
  public void testSingleMonograph() throws Exception {
    this.properties.put("documentType", "Monograph");

    final String expectedUUID = "0aa4c2e0-1a2c-11e3-8679-00505682629d";
    final String mzkUrlUUID = "http://ndk-test.mzk.cz/search/handle/uuid:";

    final String metsFileName = "METS_Monograph.xml";
    final String outputDirectoryName = "MonographOutputDir";

    mockMetsFileAndOutputDirectory(metsFileName, outputDirectoryName);
    removeOldOutputFiles();
    setMzkOutputFile();
    target.execute(cdmId, docNumber, this.libraryCode);

    File outputFile = new File(this.outputFileName);
    assertTrue(outputFile.exists());

    this.xPathParser = new XPathParser(outputFile);
    assertNotNull(this.xPathParser.getDocument());
    assertNotNull(this.xPathParser.getXPath());

    final String expectedDocNumberAndBarCode = "000358463";
    final String docNumberExpression = "//docNumber";
    assertEquals(expectedDocNumberAndBarCode, this.xPathParser.getXPath().compile(docNumberExpression).evaluate(this.xPathParser.getDocument(), XPathConstants.STRING));

    final String barCodeExpression = "//docNumber";
    assertEquals(expectedDocNumberAndBarCode, this.xPathParser.getXPath().compile(barCodeExpression).evaluate(this.xPathParser.getDocument(), XPathConstants.STRING));

    // <record>/<url> melo byt vzdy uvedeno URL na titul
    final String expectedUrl = String.format("%s%s", mzkUrlUUID, expectedUUID);
    final String urlExpression = "/URNNBNNotification/records/record/url";
    assertEquals(expectedUrl, this.xPathParser.getXPath().compile(urlExpression).evaluate(this.xPathParser.getDocument(), XPathConstants.STRING));

    //<record>/<uuid> UUID titulu
    final String uuidExpression = "/URNNBNNotification/records/record/uuid";
    assertEquals(expectedUUID, this.xPathParser.getXPath().compile(uuidExpression).evaluate(this.xPathParser.getDocument(), XPathConstants.STRING));

    // <record>/<digitalRecords>/<digitalRecord>
    final String digitalRecordExpression = "/URNNBNNotification/records/record/digitalRecords/digitalRecord";
    final String expectedDrUrl = String.format("%s%s", mzkUrlUUID, expectedUUID);
    assertEquals(expectedDrUrl, this.xPathParser.getXPath().compile(String.format("%s/url", digitalRecordExpression)).evaluate(this.xPathParser.getDocument(), XPathConstants.STRING));
    final String expectedDrUUID = expectedUUID;
    assertEquals(expectedDrUUID, this.xPathParser.getXPath().compile(String.format("%s/uuid", digitalRecordExpression)).evaluate(this.xPathParser.getDocument(), XPathConstants.STRING));

  }

  private void setMzkOutputFile() {
    this.libraryCode = "mzk";

    final String result = String.format("%s\\mzk_%s.xml", this.outputDirectoryPath, cdmId);
    this.outputFileName = result;
  }

  private void removeOldOutputFiles() {
    new File(String.format("%s\\nkp_%s.xml", this.outputDirectoryPath, cdmId)).delete();
    new File(String.format("%s\\mzk_%s.xml", this.outputDirectoryPath, cdmId)).delete();
  }

  private void mockMetsFileAndOutputDirectory(String metsFileName, String outputDirectoryName) {
    File metsFile = new File(this.getClass().getResource(metsFileName).getFile());
    assertTrue(metsFile.exists());
    when(mockedCdm.getMetsFile(cdmId)).thenReturn(metsFile);

  //  this.outputDirectoryPath = String.format("%s\\%s", metsFile.getParent(), outputDirectoryName);
    assertNotNull(this.outputDirectoryPath);

    File outputDir = new File(this.outputDirectoryPath);
    outputDir.mkdir();
    assertTrue(outputDir.exists());

    when(mockedCdm.getAlephNotificationDir(cdmId)).thenReturn(outputDir);
  }

  @Test
  public void testPeriodical() throws Exception
  {
    this.properties.put("documentType", "Periodical");

    final String expectedUUID = "f02268a0-1ea9-11e3-94e9-00505682629d";
    final String mzkUrlUUID = "http://192.168.131.17:8080/search/handle/uuid:";

    final String metsFileName = "METS_Periodical.xml";
    final String outputDirectoryName = "PeriodicalOutputDir";

    mockMetsFileAndOutputDirectory(metsFileName, outputDirectoryName);
    removeOldOutputFiles();
    setNkcrOutputFile();
    target.execute(cdmId, docNumber, this.libraryCode);

    File mzkCommon = new File(this.outputFileName);
    assertTrue(mzkCommon.exists());

    this.xPathParser = new XPathParser(mzkCommon);
    assertNotNull(this.xPathParser.getDocument());
    assertNotNull(this.xPathParser.getXPath());

    final String expectedDocNumberAndBarCode = "000358463";
    final String docNumberExpression = "//docNumber";
    assertEquals(expectedDocNumberAndBarCode, this.xPathParser.getXPath().compile(docNumberExpression).evaluate(this.xPathParser.getDocument(), XPathConstants.STRING));

    final String barCodeExpression = "//docNumber";
    assertEquals(expectedDocNumberAndBarCode, this.xPathParser.getXPath().compile(barCodeExpression).evaluate(this.xPathParser.getDocument(), XPathConstants.STRING));

    // <record>/<url> melo byt vzdy uvedeno URL na titul
    final String expectedUrl = String.format("%s%s", mzkUrlUUID, expectedUUID);
    final String urlExpression = "/URNNBNNotification/records/record/url";
    assertEquals(expectedUrl, this.xPathParser.getXPath().compile(urlExpression).evaluate(this.xPathParser.getDocument(), XPathConstants.STRING));

    //<record>/<uuid> UUID titulu
    final String uuidExpression = "/URNNBNNotification/records/record/uuid";
    assertEquals(expectedUUID, this.xPathParser.getXPath().compile(uuidExpression).evaluate(this.xPathParser.getDocument(), XPathConstants.STRING));

    // <record>/<digitalRecords>/<digitalRecord>
    final String digitalRecordExpression = "/URNNBNNotification/records/record/digitalRecords/digitalRecord";
    final String expectedDrUrl = String.format("%s%s", mzkUrlUUID, expectedUUID);
    assertEquals(expectedDrUrl, this.xPathParser.getXPath().compile(String.format("%s/url", digitalRecordExpression)).evaluate(this.xPathParser.getDocument(), XPathConstants.STRING));
    final String expectedDrUUID = expectedUUID;
    assertEquals(expectedDrUUID, this.xPathParser.getXPath().compile(String.format("%s/uuid", digitalRecordExpression)).evaluate(this.xPathParser.getDocument(), XPathConstants.STRING));

  }

  private void setNkcrOutputFile() {
    this.libraryCode = "nkcr";

    final String result = String.format("%s\\nkp_%s.xml", this.outputDirectoryPath, cdmId);
    this.outputFileName = result;
  }

  @Test
  public void testMultiMonograph() throws Exception {
    this.properties.put("documentType", "Monograph");
    this.properties.put("Monograph type", "multipart monograph");

    final String expectedUUID = "69f2f630-19f1-11e3-8c37-00505682629d";
    final String mzkUrlUUID = "http://ndk-test.mzk.cz/search/handle/uuid:";

    final String metsFileName = "METS_Multipart_Monograph.xml";
    final String outputDirectoryName = "MultipartMonographOutputDir";

    mockMetsFileAndOutputDirectory(metsFileName, outputDirectoryName);
    removeOldOutputFiles();
    setMzkOutputFile();
    target.execute(cdmId, docNumber, this.libraryCode);

    File mzkCommon = new File(this.outputFileName);
    assertTrue(mzkCommon.exists());

    this.xPathParser = new XPathParser(mzkCommon);
    assertNotNull(this.xPathParser.getDocument());
    assertNotNull(this.xPathParser.getXPath());

    final String expectedDocNumberAndBarCode = "000358463";
    final String docNumberExpression = "//docNumber";
    assertEquals(expectedDocNumberAndBarCode, this.xPathParser.getXPath().compile(docNumberExpression).evaluate(this.xPathParser.getDocument(), XPathConstants.STRING));

    final String barCodeExpression = "//docNumber";
    assertEquals(expectedDocNumberAndBarCode, this.xPathParser.getXPath().compile(barCodeExpression).evaluate(this.xPathParser.getDocument(), XPathConstants.STRING));

    // <record>/<url> melo byt vzdy uvedeno URL na titul
    final String expectedUrl = String.format("%s%s", mzkUrlUUID, expectedUUID);
    final String urlExpression = "/URNNBNNotification/records/record/url";
    assertEquals(expectedUrl, this.xPathParser.getXPath().compile(urlExpression).evaluate(this.xPathParser.getDocument(), XPathConstants.STRING));

    //<record>/<uuid> UUID titulu
    final String uuidExpression = "/URNNBNNotification/records/record/uuid";
    assertEquals(expectedUUID, this.xPathParser.getXPath().compile(uuidExpression).evaluate(this.xPathParser.getDocument(), XPathConstants.STRING));

    // <record>/<digitalRecords>/<digitalRecord>
    final String digitalRecordExpression = "/URNNBNNotification/records/record/digitalRecords/digitalRecord";
    final String expectedDrUrl = String.format("%s%s", mzkUrlUUID, expectedUUID);
    assertEquals(expectedDrUrl, this.xPathParser.getXPath().compile(String.format("%s/url", digitalRecordExpression)).evaluate(this.xPathParser.getDocument(), XPathConstants.STRING));
    final String expectedDrUUID = expectedUUID;
    assertEquals(expectedDrUUID, this.xPathParser.getXPath().compile(String.format("%s/uuid", digitalRecordExpression)).evaluate(this.xPathParser.getDocument(), XPathConstants.STRING));

    //URN-NBN konkretniho svazku
    final Double countOfUrrnnbn = (Double) this.xPathParser.getXPath().compile(String.format("count(%s/urnnbn)", digitalRecordExpression)).evaluate(this.xPathParser.getDocument(), XPathConstants.NUMBER);
    assertTrue(countOfUrrnnbn > 0);

  }
  */
}
