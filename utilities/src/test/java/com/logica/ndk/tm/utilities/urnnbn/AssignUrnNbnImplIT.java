package com.logica.ndk.tm.utilities.urnnbn;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.FileUtils;
import org.dom4j.DocumentException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.transaction.support.TransactionTemplate;
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

public class AssignUrnNbnImplIT extends CDMUtilityTest {

  private final AssignUrnNbnImpl assignUrnNbnUtility = new AssignUrnNbnImpl();
  private UrnNbnDAO urnnbnDAO = new UrnNbnDAO();

  @Before
  public void setUp() throws Exception {
    // TODO majdaf - set DAO
    TestUtils.setField(assignUrnNbnUtility, "restTemplate", new RestTemplate());
    TestUtils.setField(assignUrnNbnUtility, "urnNbnDao", urnnbnDAO);
    TestUtils.setField(urnnbnDAO, "txTemplate", new TransactionTemplate());
    setUpEmptyCdm();
    super.setUpCdmById(CDM_ID_COMMON);
    createImport(CDM_ID_COMMON);
  }

  @After
  public void tearDown() throws Exception {
    super.deleteCdmById(CDM_ID_COMMON);
  }

  @Ignore
  public void testAssignMonographStringFromResolver() throws Exception {

    final AssignUrnNbnResponse response = assignUrnNbnUtility.assign(VALID_LIBRARY_ID, "76b7c610-7f51-11e2-ae1f-00505682629d");
    assertThat(response)
        .isNotNull();
    assertThat(response.getUrnNbn())
        .isNotNull()
        .isNotEmpty();
    assertThat(response.getUrnNbnSource())
        .isNotNull()
        .isEqualTo(UrnNbnSource.RESOLVER);

    File urnNbnFile = new File(cdm.getWorkspaceDir(CDM_ID_COMMON), BaseUrnNbn.URN_NBN_FILE_NAME);
    assertThat(urnNbnFile)
        .isNotNull()
        .exists();
    String urnNbnFromFile = FileUtils.readFileToString(urnNbnFile);
    assertThat(urnNbnFromFile)
        .isNotNull()
        .isNotEmpty()
        .startsWith(VALID_URN_NBN_BASE);
  }

  @Ignore
  public void testAssignMonographStringFromDB() {

    // TODO majdaf - set DAO
//    RestTemplate oldTemplate = (RestTemplate) TestUtils.getField(assignUrnNbnUtility, "restTemplate");
//    TestUtils.setField(assignUrnNbnUtility, "restTemplate", mock(RestTemplate.class));
//    final Import importOb = createImport();
//
//    final AssignUrnNbnResponse response = assignUrnNbnUtility.assign(importOb, VALID_LIBRARY_ID, CDM_ID_EMPTY);
//    assertThat(response)
//        .isNotNull();
//    assertThat(response.getUrnNbn())
//        .isNotNull()
//        .isNotEmpty();
//    assertThat(response.getUrnNbnSource())
//        .isNotNull()
//        .isEqualTo(UrnNbnSource.DB);
//    
//    TestUtils.setField(assignUrnNbnUtility, "restTemplate", oldTemplate);

  }

  @Ignore
  public void testAssignTwiceSameImport() throws IOException, CDMException, DocumentException {

//    String id;
//    List<RegistrarScopeIdentifierType> identifiers = importOb.getDigitalDocument().getRegistrarScopeIdentifiers().getId();
//    for (RegistrarScopeIdentifierType identifier : identifiers) {
//      if ("signatura".equals(identifier.getType())) {
//        id = identifier.getValue();
//      }
//    }

    final AssignUrnNbnResponse response1 = assignUrnNbnUtility.assign(VALID_LIBRARY_ID, CDM_ID_COMMON);
    final AssignUrnNbnResponse response2 = assignUrnNbnUtility.assign(VALID_LIBRARY_ID, CDM_ID_COMMON);

    assertThat(response1)
        .isNotNull();
    assertThat(response1.getUrnNbn())
        .isNotNull()
        .isNotEmpty();
    assertThat(response1.getUrnNbnSource())
        .isNotNull()
        .isEqualTo(UrnNbnSource.RESOLVER);

    assertThat(response2)
        .isNotNull();
    assertThat(response2.getUrnNbn())
        .isNotNull()
        .isNotEmpty();
    assertThat(response2.getUrnNbnSource())
        .isNotNull()
        .isNotEqualTo(UrnNbnSource.RESOLVER);

    File urnNbnFile = new File(cdm.getWorkspaceDir(CDM_ID_COMMON), BaseUrnNbn.URN_NBN_FILE_NAME);
    assertThat(urnNbnFile)
        .isNotNull()
        .exists();
    String urnNbnFromFile = FileUtils.readFileToString(urnNbnFile);
    assertThat(urnNbnFromFile)
        .isNotNull()
        .isNotEmpty()
        .startsWith(VALID_URN_NBN_BASE);

  }

  @Ignore
  public void testAssignMonographStringFromDBAfterErrorResponse() throws IOException, CDMException, DocumentException {


//    String id;
//    List<RegistrarScopeIdentifierType> identifiers = importOb.getDigitalDocument().getRegistrarScopeIdentifiers().getId();
//    for (RegistrarScopeIdentifierType identifier : identifiers) {
//      if ("signatura".equals(identifier.getType())) {
//        id = identifier.getValue();
//      }
//    }

    final AssignUrnNbnResponse response1 = assignUrnNbnUtility.assign(VALID_LIBRARY_ID, CDM_ID_COMMON);
    final AssignUrnNbnResponse response2 = assignUrnNbnUtility.assign(VALID_LIBRARY_ID, CDM_ID_COMMON);

    assertThat(response1)
        .isNotNull();
    assertThat(response1.getUrnNbn())
        .isNotNull()
        .isNotEmpty();
    assertThat(response1.getUrnNbnSource())
        .isNotNull()
        .isEqualTo(UrnNbnSource.RESOLVER);

    assertThat(response2)
        .isNotNull();
    assertThat(response2.getUrnNbn())
        .isNotNull()
        .isNotEmpty();
    assertThat(response2.getUrnNbnSource())
        .isNotNull()
        .isNotEqualTo(UrnNbnSource.RESOLVER);

    File urnNbnFile = new File(cdm.getWorkspaceDir(CDM_ID_COMMON), BaseUrnNbn.URN_NBN_FILE_NAME);
    assertThat(urnNbnFile)
        .isNotNull()
        .exists();
    String urnNbnFromFile = FileUtils.readFileToString(urnNbnFile);
    assertThat(urnNbnFromFile)
        .isNotNull()
        .isNotEmpty()
        .startsWith(VALID_URN_NBN_BASE);

  }

  private void createImport(String cdmId) {
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
