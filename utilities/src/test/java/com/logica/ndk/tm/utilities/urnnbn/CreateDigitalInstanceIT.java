package com.logica.ndk.tm.utilities.urnnbn;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

import java.math.BigInteger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import com.logica.ndk.commons.utils.test.TestUtils;
import com.logica.ndk.commons.uuid.UUID;
import com.logica.ndk.tm.cdm.JAXBContextPool;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.process.AssignUrnNbnResponse;
import com.logica.ndk.tm.utilities.CDMUtilityTest;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.urnnbn.Monograph.TitleInfo;

public class CreateDigitalInstanceIT extends CDMUtilityTest {

  private CreateDigitalInstanceImpl createDigitalInstance = new CreateDigitalInstanceImpl();;

  private final String cdmId = CDM_ID_COMMON;
  private final String PROFILE = "nk";
  private final String ACCESSIBILITY = TmConfig.instance().getString("utility.urnNbn.publish.public");
  private final RestTemplate restTemplateMock = mock(RestTemplate.class);
  private final AssignUrnNbnImpl assignUrnNbnUtility = new AssignUrnNbnImpl();

  @Before
  public void setUp() throws Exception {
    setUpCdmById(cdmId);
    TestUtils.setField(assignUrnNbnUtility, "restTemplate", new RestTemplate());
    TestUtils.setField(createDigitalInstance, "restTemplate", new RestTemplate());
  }

  @After
  public void tearDown() throws Exception {
    deleteCdmById(cdmId);
    reset(restTemplateMock);
  }

  @Ignore
  public void testAssignMonographStringFromResolver() throws Exception {
    createImport();
    final AssignUrnNbnResponse response = assignUrnNbnUtility.assign(VALID_LIBRARY_ID, cdmId);
    String urnNbn = response.getUrnNbn();
    String result = createDigitalInstance.execute(cdmId, urnNbn, PROFILE, ACCESSIBILITY, true);
    assertTrue(result.equals("OK"));
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
}
