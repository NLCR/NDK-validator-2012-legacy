package com.logica.ndk.tm.utilities.urnnbn;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.File;
import java.math.BigInteger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.logica.ndk.commons.utils.test.TestUtils;
import com.logica.ndk.commons.uuid.UUID;
import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.cdm.JAXBContextPool;
import com.logica.ndk.tm.process.AssignUrnNbnResponse;
import com.logica.ndk.tm.process.UrnNbnSource;
import com.logica.ndk.tm.utilities.CDMUtilityTest;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.urnnbn.Monograph.TitleInfo;

@Ignore
public class AssignUrnNbnImplTest extends CDMUtilityTest {

  private final AssignUrnNbnImpl assignUrnNbnUtility = new AssignUrnNbnImpl();

  private final UrnNbnDAO urnNbnDAOMock = mock(UrnNbnDAO.class);
  private final RestTemplate restTemplateMock = mock(RestTemplate.class);

  private Document responseDocument;
  private final Namespace namespace = new Namespace("", "http://resolver.nkp.cz/v2/");

  private String cdmId = CDM_ID_SPLIT;

  @Before
  public void setUp() throws Exception {
    TestUtils.setField(assignUrnNbnUtility, "restTemplate", restTemplateMock);
    TestUtils.setField(assignUrnNbnUtility, "urnNbnDao", urnNbnDAOMock);
    setUpCdmById(cdmId);
  }

  @After
  public void tearDown() throws Exception {
    reset(urnNbnDAOMock);
    reset(restTemplateMock);
    //deleteCdmById(cdmId);
  }

  @Ignore
  public void testAssignMonographStringFromResolver() throws Exception {

    final Element urnNbnNode = DocumentHelper.createElement(new QName("urnNbn", namespace));
    urnNbnNode.addAttribute(QName.get("schemaLocation", "xsi", "http://www.w3.org/2001/XMLSchema-instance"), "http://resolver.nkp.cz/v2/ http://iris.mzk.cz/cache/resolver-response.xsd");
    final Element statusNode = urnNbnNode.addElement("status");
    statusNode.setText("ACTIVE");
    final Element registrarCodeNode = urnNbnNode.addElement("registrarCode");
    registrarCodeNode.setText(VALID_LIBRARY_ID);
    final Element valueNode = urnNbnNode.addElement("value");
    valueNode.setText(VALID_URN_NBN.toLowerCase());
    responseDocument = DocumentHelper.createDocument(urnNbnNode);
    ResponseEntity<String> responseEntity = new ResponseEntity<String>(responseDocument.asXML(), null, HttpStatus.OK);

    doReturn(responseEntity).when(restTemplateMock).postForEntity(anyString(), anyObject(), eq(String.class));

    createImport();

    final AssignUrnNbnResponse response = assignUrnNbnUtility.assign(VALID_LIBRARY_ID, cdmId);
    assertThat(response)
        .isNotNull();
    assertThat(response.getUrnNbn())
        .isNotNull()
        .isNotEmpty();
    assertThat(response.getUrnNbnSource())
        .isNotNull()
        .isEqualTo(UrnNbnSource.RESOLVER);

    File urnNbnFile = new File(cdm.getWorkspaceDir(cdmId), BaseUrnNbn.URN_NBN_FILE_NAME);
    assertThat(urnNbnFile)
        .isNotNull()
        .exists();
    String urnNbnFromFile = FileUtils.readFileToString(urnNbnFile);
    assertThat(urnNbnFromFile)
        .isNotNull()
        .isNotEmpty()
        .contains(VALID_URN_NBN);

    verifyZeroInteractions(urnNbnDAOMock);
    verify(restTemplateMock).postForEntity(anyString(), isA(Import.class), eq(String.class));

  }

  @Ignore
  public void testRegistrarMapping() throws Exception {

    final Element urnNbnNode = DocumentHelper.createElement(new QName("urnNbn", namespace));
    urnNbnNode.addAttribute(QName.get("schemaLocation", "xsi", "http://www.w3.org/2001/XMLSchema-instance"), "http://resolver.nkp.cz/v2/ http://iris.mzk.cz/cache/resolver-response.xsd");
    final Element statusNode = urnNbnNode.addElement("status");
    statusNode.setText("ACTIVE");
    final Element registrarCodeNode = urnNbnNode.addElement("registrarCode");
    registrarCodeNode.setText("boa003");
    final Element valueNode = urnNbnNode.addElement("value");
    valueNode.setText(VALID_URN_NBN.toLowerCase());
    responseDocument = DocumentHelper.createDocument(urnNbnNode);
    ResponseEntity<String> responseEntity = new ResponseEntity<String>(responseDocument.asXML(), null, HttpStatus.OK);

    doReturn(responseEntity).when(restTemplateMock).postForEntity(anyString(), anyObject(), eq(String.class));

    createImport();

    final AssignUrnNbnResponse response = assignUrnNbnUtility.assign("boa003", cdmId);
    String expectedUrlPart = "boa001";
    verifyZeroInteractions(urnNbnDAOMock);
    verify(restTemplateMock).postForEntity((String) argThat(new UrlPartMatcher(expectedUrlPart)), isA(Import.class), eq(String.class));

  }

  @Ignore
  public void testAssignMonographStringFromDB() throws CDMException, DocumentException {

    doReturn(VALID_URN_NBN).when(urnNbnDAOMock).assignUrnNbnFromDb(eq(VALID_LIBRARY_ID), eq(cdmId));
    doThrow(Exception.class).when(restTemplateMock).postForEntity(anyString(), anyObject(), eq(String.class));

    createImport();

    final AssignUrnNbnResponse response = assignUrnNbnUtility.assign(VALID_LIBRARY_ID, cdmId);
    assertThat(response)
        .isNotNull();
    assertThat(response.getUrnNbn())
        .isNotNull()
        .isNotEmpty();
    assertThat(response.getUrnNbnSource())
        .isNotNull()
        .isEqualTo(UrnNbnSource.DB);

    verify(restTemplateMock).postForEntity(anyString(), isA(Import.class), eq(String.class));
    verify(urnNbnDAOMock).assignUrnNbnFromDb(eq(VALID_LIBRARY_ID), eq(cdmId));

  }

  @Ignore
  public void testAssignMonographStringFromDBAfterErrorResponse() throws CDMException, DocumentException {

    final Element errorNode = DocumentHelper.createElement(new QName("error", namespace));
    errorNode.addAttribute(QName.get("schemaLocation", "xsi", "http://www.w3.org/2001/XMLSchema-instance"), "http://resolver.nkp.cz/v2/ http://iris.mzk.cz/cache/resolver-response.xsd");
    final Element codeNode = errorNode.addElement("code");
    codeNode.setText("INVALID_DIGITAL_DOCUMENT_IDENTIFIER");
    final Element messageNode = errorNode.addElement("message");
    messageNode.setText("digital document with identifier of type 'OAI_harvester' and value '1000' already registered by registrar with code aba001");
    responseDocument = DocumentHelper.createDocument(errorNode);
    ResponseEntity<String> responseEntity = new ResponseEntity<String>(responseDocument.asXML(), null, HttpStatus.OK);

    doReturn(VALID_URN_NBN).when(urnNbnDAOMock).assignUrnNbnFromDb(eq(VALID_LIBRARY_ID), eq(cdmId));
    doReturn(responseEntity).when(restTemplateMock).postForEntity(anyString(), anyObject(), eq(String.class));

    createImport();

    final AssignUrnNbnResponse response = assignUrnNbnUtility.assign(VALID_LIBRARY_ID, cdmId);
    assertThat(response)
        .isNotNull();
    assertThat(response.getUrnNbn())
        .isNotNull()
        .isNotEmpty();
    assertThat(response.getUrnNbnSource())
        .isNotNull()
        .isEqualTo(UrnNbnSource.DB);

    verify(restTemplateMock).postForEntity(anyString(), isA(Import.class), eq(String.class));
    verify(urnNbnDAOMock).assignUrnNbnFromDb(eq(VALID_LIBRARY_ID), eq(cdmId));

  }

  private void createImport() {
    final Monograph monograph = new Monograph();

    final TitleInfo titleInfo = new TitleInfo();
    titleInfo.setTitle("Babička");
    titleInfo.setSubTitle("Obrazy z venkovského života");

    monograph.setTitleInfo(titleInfo);
    monograph.setCcnb("cnb002251177");
    monograph.setIsbn("8090119964");
    monograph.setOtherId("DOI:TODO");
    monograph.setDocumentType("kniha");
    monograph.setDigitalBorn(false);

    final PrimaryOriginator originator = new PrimaryOriginator();
    originator.setType(OriginatorTypeType.AUTHOR);
    originator.setValue("Božena Němcová");
    monograph.setPrimaryOriginator(originator);

    monograph.setOtherOriginator("Adolf Kašpar");

    final Publication publication = new Publication();
    publication.setPublisher("Československý spisovatel");
    publication.setPlace("V Praze");
    publication.setYear("2011");

    monograph.setPublication(publication);

    final DigitalDocument digitalDocument = new DigitalDocument();
    digitalDocument.setArchiverId(3L);

    final RegistrarScopeIdentifiers identifiers = new RegistrarScopeIdentifiers();

    final RegistrarScopeIdentifier harvester = new RegistrarScopeIdentifier();
    harvester.setType("OAI_harvester");
    harvester.setValue(String.valueOf(TestUtils.generateInt(5000)));
    identifiers.getId().add(harvester);

    final RegistrarScopeIdentifier signature = new RegistrarScopeIdentifier();
    signature.setType("signatura");
    signature.setValue("PK-" + TestUtils.generateInt(9999) + "." + TestUtils.generateInt(999));
    identifiers.getId().add(signature);

    final RegistrarScopeIdentifier k4 = new RegistrarScopeIdentifier();
    k4.setType("K4_pid");
    k4.setValue("uuid:" + UUID.timeUUID());
    identifiers.getId().add(k4);

    digitalDocument.setRegistrarScopeIdentifiers(identifiers);
    digitalDocument.setFinanced("norské fondy");
    digitalDocument.setContractNumber("123");

    final TechnicalMetadata metadata = new TechnicalMetadata();

    final Format format = new Format();
    format.setVersion("1.0");
    format.setValue("jpeg");
    metadata.setFormat(format);

    metadata.setExtent("245 x jpeg2000;245 x mods-alto;1 x mods");

    final Resolution resolution = new Resolution();
    resolution.setHorizontal(BigInteger.valueOf(1280L));
    resolution.setVertical(BigInteger.valueOf(1024L));
    metadata.setResolution(resolution);

    final Compression compression = new Compression();
    compression.setRatio(0.3);
    compression.setValue("LZW");
    metadata.setCompression(compression);

    final Color color = new Color();
    color.setModel("RGB");
    color.setDepth(BigInteger.valueOf(24L));
    metadata.setColor(color);

    metadata.setIccProfile("ICC profile");

    final PictureSize pictureSize = new PictureSize();
    pictureSize.setWidth(BigInteger.valueOf(600L));
    pictureSize.setHeight(BigInteger.valueOf(1000L));
    metadata.setPictureSize(pictureSize);

    digitalDocument.setTechnicalMetadata(metadata);

    final Import importOb = new Import();
    importOb.setMonograph(monograph);
    importOb.setDigitalDocument(digitalDocument);
    JAXBContext context;
    try {
      context = JAXBContextPool.getContext(Import.class);
      final Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      marshaller.marshal(importOb, cdm.getUrnXml(cdmId));
    }
    catch (JAXBException e) {
      throw new SystemException("Failed marshalling to " + cdm.getUrnXml(cdmId).getPath(), ErrorCodes.JAXB_MARSHALL_ERROR);
    }
  }

  protected class UrlPartMatcher extends ArgumentMatcher<String> {
    String expectedPart;

    public UrlPartMatcher(String expectedCommand) {
      this.expectedPart = expectedCommand;
    }

    public boolean matches(Object o) {
      if (o instanceof String) {
        String ulr = (String) o;
        System.out.println("Expected part: " + expectedPart);
        System.out.println("Actual url: " + ulr.toString());
        return ulr.contains(expectedPart);
      }
      else {
        return true;
      }
    }
  }

//  <?xml version="1.0"?>
//  <error xmlns="http://resolver.nkp.cz/v2/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://resolver.nkp.cz/v2/ http://iris.mzk.cz/cache/resolver-response.xsd">
//    <code>INVALID_DIGITAL_DOCUMENT_IDENTIFIER</code>
//    <message>digital document with identifier of type 'OAI_harvester' and value '1000' already registered by registrar with code aba001</message>
//  </error>

//  <?xml version="1.0"?>
//  <urnNbn xmlns="http://resolver.nkp.cz/v2/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://resolver.nkp.cz/v2/ http://iris.mzk.cz/cache/resolver-response.xsd">
//    <status>ACTIVE</status>
//    <registrarCode>aba001</registrarCode>
//    <value>urn:nbn:cz:aba001-0003v9</value>
//  </urnNbn>
}
