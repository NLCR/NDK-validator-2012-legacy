package com.logica.ndk.tm.utilities.transformation.scantailor;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.logica.ndk.tm.utilities.CDMUtilityTest;

public class CreateScantailorConfigImplTest extends ScantailorTest {

  private final CreateScantailorConfigImpl createScantailorConfig = new CreateScantailorConfigImpl();

  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  @Test
  public void testExecuteMultipleScans() throws Exception {

    createScantailorConfig.execute(CDM_ID_SCANTAILOR);

    assertThat(cdm.getScantailorConfigsDir(CDM_ID_SCANTAILOR).list())
        .isNotNull()
        .isNotEmpty()
        .hasSize(3+1)
        .containsOnly("jpeg-tif-location.txt", "1.scanTailor", "2.scanTailor", "3.scanTailor");

    {
      final File configFile = new File(cdm.getScantailorConfigsDir(CDM_ID_SCANTAILOR), "1.scanTailor");
      assertThat(configFile)
          .isNotNull()
          .exists();

      final Document document0001 = getFileAsDocument(configFile);
      assertThat(document0001.selectSingleNode("//images")).isNotNull();
      assertThat(document0001.selectNodes("//image")).hasSize(4);
      assertThat(document0001.selectNodes("//size")).hasSize(4);
      assertThat(document0001.selectNodes("//dpi")).hasSize(4);
      assertThat(document0001.selectSingleNode("//files")).isNotNull();
      assertThat(document0001.selectNodes("//file")).hasSize(4);
    }
    {
      final File configFile = new File(cdm.getScantailorConfigsDir(CDM_ID_SCANTAILOR), "2.scanTailor");
      assertThat(configFile)
          .isNotNull()
          .exists();

      final Document document0001 = getFileAsDocument(configFile);
      assertThat(document0001.selectSingleNode("//images")).isNotNull();
      assertThat(document0001.selectNodes("//image")).hasSize(2);
      assertThat(document0001.selectNodes("//size")).hasSize(2);
      assertThat(document0001.selectNodes("//dpi")).hasSize(2);
      assertThat(document0001.selectSingleNode("//files")).isNotNull();
      assertThat(document0001.selectNodes("//file")).hasSize(2);
    }
  }

  @Test
  public void testExecuteSingleScan() throws Exception {

    for (final File file : cdm.getFlatDataDir(CDM_ID_SCANTAILOR).listFiles()) {
      if (file.getName().startsWith("1_") || file.getName().startsWith("3_")) {
        file.delete();
      }
    }

    createScantailorConfig.execute(CDM_ID_SCANTAILOR);

    assertThat(cdm.getScantailorConfigsDir(CDM_ID_SCANTAILOR).list())
        .isNotNull()
        .isNotEmpty()
        .hasSize(1+1)
        .containsOnly("jpeg-tif-location.txt", "2.scanTailor");

    {
      final File configFile = new File(cdm.getScantailorConfigsDir(CDM_ID_SCANTAILOR), "2.scanTailor");
      assertThat(configFile)
          .isNotNull()
          .exists();

      final Document document0001 = getFileAsDocument(configFile);
      assertThat(document0001.selectSingleNode("//images")).isNotNull();
      assertThat(document0001.selectNodes("//image")).hasSize(2);
      assertThat(document0001.selectNodes("//size")).hasSize(2);
      assertThat(document0001.selectNodes("//dpi")).hasSize(2);
      assertThat(document0001.selectSingleNode("//files")).isNotNull();
      assertThat(document0001.selectNodes("//file")).hasSize(2);
    }
  }

  private Document getFileAsDocument(final File configFile) throws DocumentException {
    checkNotNull(configFile, "configFile must not be null");

    final SAXReader reader = new SAXReader();
    final Document document = reader.read(configFile);

    return document;
  }
}
