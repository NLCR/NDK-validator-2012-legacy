package com.logica.ndk.tm.jbpm.handler.urnnbn;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.commons.utils.test.TestUtils;
import com.logica.ndk.commons.uuid.UUID;
import com.logica.ndk.tm.utilities.HandlerTestHelper;
import com.logica.ndk.tm.utilities.TestWorkItemBase;
import com.logica.ndk.tm.utilities.TestWorkItemManagerBase;
import com.logica.ndk.tm.utilities.urnnbn.Color;
import com.logica.ndk.tm.utilities.urnnbn.Compression;
import com.logica.ndk.tm.utilities.urnnbn.DigitalDocument;
import com.logica.ndk.tm.utilities.urnnbn.Format;
import com.logica.ndk.tm.utilities.urnnbn.Import;
import com.logica.ndk.tm.utilities.urnnbn.Monograph;
import com.logica.ndk.tm.utilities.urnnbn.Monograph.TitleInfo;
import com.logica.ndk.tm.utilities.urnnbn.OriginatorTypeType;
import com.logica.ndk.tm.utilities.urnnbn.PictureSize;
import com.logica.ndk.tm.utilities.urnnbn.PrimaryOriginator;
import com.logica.ndk.tm.utilities.urnnbn.Publication;
import com.logica.ndk.tm.utilities.urnnbn.RegistrarScopeIdentifier;
import com.logica.ndk.tm.utilities.urnnbn.RegistrarScopeIdentifiers;
import com.logica.ndk.tm.utilities.urnnbn.Resolution;
import com.logica.ndk.tm.utilities.urnnbn.TechnicalMetadata;

public class AssignUrnNbnAsyncHandlerIT extends HandlerTestHelper {

  private Map<String, Object> parameters;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    parameters = new HashMap<String, Object>();

    parameters.put("cdmId", "testCdmId");
    parameters.put("sigla", "aba001");
    parameters.put("document", createDocument());
  }

  @After
  public void tearDown() throws Exception {
  }

  @Ignore
  public void test() {
    final WorkItem wi = new TestWorkItemBase(parameters) {
      // just in the case I need to customize something
    };
    final WorkItemManager wim = new TestWorkItemManagerBase() {
      // just in the case I need to customize something
    };
    execute(new AssignUrnNbnAsyncHandler(), wi, wim, 5000);
  }

  private Import createDocument() {

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

    return importOb;
  }

}
