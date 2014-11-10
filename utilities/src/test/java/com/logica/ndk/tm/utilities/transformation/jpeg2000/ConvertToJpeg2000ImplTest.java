package com.logica.ndk.tm.utilities.transformation.jpeg2000;

import static org.fest.assertions.Assertions.assertThat;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.CDMUtilityTest;

@Ignore
public class ConvertToJpeg2000ImplTest extends CDMUtilityTest {

//  private String COMMON_PATH = "utility.convertToJpeg2000.profiles.";
  private String JPEG2000PRESERVEDPI_PROFILE = "JPEG2000PRESERVEDPI";
  private String JPEG2000DPI300_PROFILE = "JPEG2000DPI300";
  private String JPEG2000PRESERVEDPIMCLOSSY_PROFILE = "JPEG2000PRESERVEDPIMCLOSSY";
  private String JPEG2000HALFDPIMCLOSSY_PROFILE = "JPEG2000HALFDPIMCLOSSY";
  private String CDM_NAME = "ANL000003";
  private String SOURCE_FOLDER_NAME = FileUtils.getTempDirectoryPath() + "\\NAK___KNIH_VALD_A_75_2GKH\\EX\\";
  private String TARGET_FOLDER_NAME_MC = FileUtils.getTempDirectoryPath() + "\\CDM_common\\data\\MC\\";
  private String TARGET_FOLDER_NAME_UC = FileUtils.getTempDirectoryPath() + "\\CDM_common\\data\\UC\\";

  ConvertToJpeg2000Impl jpeg2000ImplService;

  @Before
  public void setUp() throws Exception {
    super.setUpCdmById(CDM_ID_COMMON);
    jpeg2000ImplService = new ConvertToJpeg2000Impl();
  }

  @After
  public void tearDown() throws Exception {
    super.deleteCdmById(CDM_ID_COMMON);
  }

  @Ignore
  public void testProfile_JPEG2000PRESERVEDPI() throws Exception {
    CDM cdm = new CDM();
    final int response = jpeg2000ImplService.execute(
        CDM_ID_EMPTY,
        cdm.getPostprocessingDataDir(CDM_ID_EMPTY).getAbsolutePath(),
        cdm.getMasterCopyDir(CDM_ID_EMPTY).getAbsolutePath(),
        JPEG2000PRESERVEDPI_PROFILE,
        "");

    assertThat(response)
        .isNotNull()
        .isEqualTo(9);
  }

  @Ignore
  public void testProfile_JPEG2000DPI300() throws Exception {
    final int response = jpeg2000ImplService.execute(
        CDM_NAME,
        SOURCE_FOLDER_NAME,
        TARGET_FOLDER_NAME_MC,
        JPEG2000DPI300_PROFILE,
        "");

    assertThat(response)
        .isNotNull()
        .isEqualTo(9);
  }

  @Ignore
  public void testProfile_JPEG2000PRESERVEDPIMCLOSSY() throws Exception {
    final int response = jpeg2000ImplService.execute(
        CDM_NAME,
        SOURCE_FOLDER_NAME,
        TARGET_FOLDER_NAME_UC,
        JPEG2000PRESERVEDPIMCLOSSY_PROFILE,
        "");

    assertThat(response)
        .isNotNull()
        .isEqualTo(6);
  }

  @Ignore
  public void testProfile_JPEG2000HALFDPIMCLOSSY() throws Exception {
    final int response = jpeg2000ImplService.execute(
        CDM_NAME,
        SOURCE_FOLDER_NAME,
        TARGET_FOLDER_NAME_UC,
        JPEG2000HALFDPIMCLOSSY_PROFILE,
        "");

    assertThat(response)
        .isNotNull()
        .isEqualTo(6);
  }

}
