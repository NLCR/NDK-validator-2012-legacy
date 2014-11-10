package com.logica.ndk.tm.utilities.transformation;

import static org.junit.Assert.assertTrue;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.CDMUtilityTest;

@Ignore
public class ConvertImageTest extends CDMUtilityTest {

//  private String COMMON_PATH = "utility.convertToJpeg2000.profiles.";
  private String JPEG2000PRESERVEDPI_PROFILE = "JPEG2000PRESERVEDPI";
  private String JPEG2000DPI300_PROFILE = "JPEG2000DPI300";
  private String JPEG2000PRESERVEDPIMCLOSSY_PROFILE = "JPEG2000PRESERVEDPIMCLOSSY";
  private String JPEG2000HALFDPIMCLOSSY_PROFILE = "JPEG2000HALFDPIMCLOSSY";
  private String CDM_NAME = "ANL000003";
  private String SOURCE_FOLDER_NAME = FileUtils.getTempDirectoryPath() + "\\CDM_common\\data\\postprocessingData";
  private String TARGET_FOLDER_NAME_TH = FileUtils.getTempDirectoryPath() + "\\CDM_common\\data\\TH\\";
  private String TARGET_FOLDER_NAME_UC = FileUtils.getTempDirectoryPath() + "\\CDM_common\\data\\UC\\";
  private String THUMBNAIL_PROFILE_PATH = "utility.convertImage.profile.thumbnail";
  private String PREVIEW_PROFILE_PATH = "utility.convertImage.profile.preview";

  ConvertImageImpl jpeg2000ImplService;
  String thumbnailProfile;

  @Before
  public void setUp() throws Exception {
    super.setUpCdmById(CDM_ID_METS_HELPER);
    jpeg2000ImplService = new ConvertImageImpl();
  }

  @After
  public void tearDown() throws Exception {
    super.deleteCdmById(CDM_ID_METS_HELPER);
    jpeg2000ImplService = null;
  }

  @Ignore
  public void testExecute() {
    Integer result = new ConvertImageImpl().execute(
        CDM_ID_METS_HELPER,
        SOURCE_FOLDER_NAME,
        TARGET_FOLDER_NAME_TH,
        THUMBNAIL_PROFILE_PATH,
        "",
        "jpg");

    assertTrue(result.equals(new Integer(6)));
  }
}
