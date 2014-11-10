package com.logica.ndk.tm.utilities.transformation.scantailor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.CDMUtilityTest;
import com.logica.ndk.tm.utilities.file.CreateScansCSVImpl;
import com.logica.ndk.tm.utilities.integration.wf.task.Scan;

public class RunScantailorCropImplIT extends CDMUtilityTest {

  private final RunScantailorCropImpl scantailorCrop = new RunScantailorCropImpl();
  private final String PROFILE_SCANTAILORCOLOR = "scantailorcolor";
  private final String PROFILE_SCANTAILORBW = "scantailorbw";

  private final String COLOR_MODE_BLACK_AND_WHITE = "black_and_white";
  private final String COLOR_MODE_GRAYSCALE_COLOR_MODE = "color_grayscale";
  private final String COLOR_MODE_MIXED_COLOR_MODE = "mixed";

  private final String CROPTYPE_AUTODETECT = "0";
  private final String CROPTYPE_ONE_PAGE = "1";
  private final String CROPTYPE_ONE_PAGE_CUTTING_NEEDED = "2";

  private final Integer OUTPUT_DPI = 400;
  private final Integer DIMENSION_X = 600;
  private final Integer DIMENSION_Y = 800;

  CreateScansCSVImpl createScansCSV;
  List<Scan> scans;

  @Before
  public void setUp() throws Exception {
    prepareCdm();
    scans = new ArrayList<Scan>();
    prepareCdm();
    createScansCSV = new CreateScansCSVImpl();
    createScansCSV.execute(CDM_ID_SCANTAILOR, this.scans);
    setUpCdmById(CDM_ID_SCANTAILOR);
  }

  @After
  public void tearDown() throws Exception {
    deleteCdmById(CDM_ID_SCANTAILOR);
  }

  @Ignore
  public void testExecuteProfileColor() {
    new CreateScantailorConfigImpl().execute(CDM_ID_SCANTAILOR);
    new RunScantailorPreprocessImpl().execute(CDM_ID_SCANTAILOR, PROFILE_SCANTAILORCOLOR, null, CROPTYPE_AUTODETECT, DIMENSION_X, DIMENSION_Y, OUTPUT_DPI);

    final int response = scantailorCrop.execute(CDM_ID_SCANTAILOR, PROFILE_SCANTAILORCOLOR, COLOR_MODE_BLACK_AND_WHITE, CROPTYPE_AUTODETECT, DIMENSION_X, DIMENSION_Y, OUTPUT_DPI);

    System.out.println(response + " jjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjj");
//    assertThat(response)
//        .isNotNull()
//        .isNotEmpty()
//        .isEqualTo(ResponseStatus.RESPONSE_OK);
  }

//  @Test
  public void testExecuteProfileBw() {
    new CreateScantailorConfigImpl().execute(CDM_ID_SCANTAILOR);
    new RunScantailorPreprocessImpl().execute(CDM_ID_SCANTAILOR, PROFILE_SCANTAILORBW, COLOR_MODE_GRAYSCALE_COLOR_MODE, CROPTYPE_ONE_PAGE_CUTTING_NEEDED, DIMENSION_X, DIMENSION_Y, OUTPUT_DPI);

    final int response = scantailorCrop.execute(CDM_ID_SCANTAILOR, PROFILE_SCANTAILORBW, COLOR_MODE_MIXED_COLOR_MODE, CROPTYPE_ONE_PAGE, DIMENSION_X, DIMENSION_Y, OUTPUT_DPI);
    System.out.println(response + "jjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjj");

    //
//    assertThat(response)
//        .isNotNull()
//        .isNotEmpty()
//        .isEqualTo(ResponseStatus.RESPONSE_OK);
  }

  private void prepareCdm() throws Exception {
    scans = new ArrayList<Scan>();

    for (int i = 0; i < 2; i++) {
      Scan scan = new Scan();
      scan.setPackageId((long) 111);
      scan.setCreateDT(new Date());
      scan.setCreateUserName("test-name");
      scan.setScanId((long) i);
      scan.setScannerCode("test-ScannerCode");
      scan.setScanTypeCode("test-scanTypeCode");
      scan.setLocalURN("test-localUrn");
      scan.setNote("test-note");
      scan.setScanCount(113);
      scan.setDoublePage(true);
      scan.setPages("pages");
      scan.setValidity(true);
      scan.setScanModeCode("114");
      scan.setStatePP(115);
      //  scan.setCropTypeCode(CROPTYPE_ONE_PAGE);
      scan.setProfilePPCode(PROFILE_SCANTAILORCOLOR);
      scan.setDimensionX(10);
      scan.setDimensionY(10);
      scan.setScanDuration((long) 116);

      scans.add(scan);
    }
  }

}
