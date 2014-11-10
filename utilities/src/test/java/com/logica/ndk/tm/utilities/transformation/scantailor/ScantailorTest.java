package com.logica.ndk.tm.utilities.transformation.scantailor;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.mockito.ArgumentMatcher;

import com.logica.ndk.tm.utilities.CDMUtilityTest;
import com.logica.ndk.tm.utilities.file.CreateScansCSVImpl;
import com.logica.ndk.tm.utilities.integration.wf.task.Scan;

@Ignore
public class ScantailorTest extends CDMUtilityTest {
  List<Scan> scans;
  
  protected final CreateScantailorConfigImpl createScantailorConfig = new CreateScantailorConfigImpl();
  protected final DefaultExecutor executorMock = mock(DefaultExecutor.class);
  String PROFILE_SCANTAILORCOLOR = "SCANTAILORCOLOR";
  String PROFILE_SCANTAILORBW = "SCANTAILORBW";
  String PROFILE_MANUAL = "MANUAL";
  CreateScansCSVImpl createScansCSV;

  protected final String COLOR_MODE_BLACK_AND_WHITE = "black_and_white";
  protected final String COLOR_MODE_GRAYSCALE_COLOR_MODE = "color_grayscale";
  protected final String COLOR_MODE_MIXED_COLOR_MODE = "mixed";

  protected final String CROPTYPE_AUTODETECT = "0";
  protected final String CROPTYPE_ONE_PAGE = "1";
  protected final String CROPTYPE_ONE_PAGE_CUTTING_NEEDED = "2";
  protected final String CROPTYPE_TWO_PAGE = "3";

  protected final Integer DIMENSION_X = 600;
  protected final Integer DIMENSION_Y = 800;

  protected final Integer OUTPUT_DPI = 400;

  protected void prepareCdm() throws Exception {
    scans = new ArrayList<Scan>();

    Scan scan1 = new Scan();
    scan1.setPackageId((long) 111);
    scan1.setCreateDT(new Date());
    scan1.setCreateUserName("test-name");
    scan1.setScanId((long)1);
    scan1.setScannerCode("test-ScannerCode");
    scan1.setScanTypeCode("test-scanTypeCode");
    scan1.setLocalURN("test-localUrn");
    scan1.setNote("test-note");
    scan1.setScanCount(113);
    scan1.setDoublePage(true);
    scan1.setPages("pages");
    scan1.setValidity(true);
    scan1.setScanModeCode("114");
    scan1.setStatePP(115);
    scan1.setCropTypeCode(CROPTYPE_ONE_PAGE);
    scan1.setProfilePPCode(PROFILE_SCANTAILORCOLOR);
    scan1.setDimensionX(800);
    scan1.setDimensionY(600);
    scan1.setScanDuration((long) 116);

    Scan scan2 = new Scan();
    scan2.setPackageId((long) 111);
    scan2.setCreateDT(new Date());
    scan2.setCreateUserName("test-name");
    scan2.setScanId((long)2);
    scan2.setScannerCode("test-ScannerCode");
    scan2.setScanTypeCode("test-scanTypeCode");
    scan2.setLocalURN("test-localUrn");
    scan2.setNote("test-note");
    scan2.setScanCount(113);
    scan2.setDoublePage(true);
    scan2.setPages("pages");
    scan2.setValidity(true);
    scan2.setScanModeCode("114");
    scan2.setStatePP(115);
    scan2.setCropTypeCode(CROPTYPE_TWO_PAGE);
    scan2.setProfilePPCode(PROFILE_SCANTAILORCOLOR);
    scan2.setDimensionX(800);
    scan2.setDimensionY(600);
    scan2.setScanDuration((long) 116);

    Scan scan3 = new Scan();
    scan3.setPackageId((long) 111);
    scan3.setCreateDT(new Date());
    scan3.setCreateUserName("test-name");
    scan3.setScanId((long)3);
    scan3.setScannerCode("test-ScannerCode");
    scan3.setScanTypeCode("test-scanTypeCode");
    scan3.setLocalURN("test-localUrn");
    scan3.setNote("test-note");
    scan3.setScanCount(113);
    scan3.setDoublePage(true);
    scan3.setPages("pages");
    scan3.setValidity(true);
    scan3.setScanModeCode("114");
    scan3.setStatePP(115);
    scan3.setCropTypeCode(null);
    scan3.setProfilePPCode(PROFILE_SCANTAILORCOLOR);
    scan3.setDimensionX(800);
    scan3.setDimensionY(600);
    scan3.setScanDuration((long) 116);

    Scan scan4 = new Scan();
    scan4.setPackageId((long) 111);
    scan4.setCreateDT(new Date());
    scan4.setCreateUserName("test-name");
    scan4.setScanId((long)4);
    scan4.setScannerCode("test-ScannerCode");
    scan4.setScanTypeCode("test-scanTypeCode");
    scan4.setLocalURN("test-localUrn");
    scan4.setNote("test-note");
    scan4.setScanCount(114);
    scan4.setDoublePage(true);
    scan4.setPages("pages");
    scan4.setValidity(true);
    scan4.setScanModeCode("114");
    scan4.setStatePP(115);
    scan4.setCropTypeCode(null);
    scan4.setProfilePPCode(PROFILE_MANUAL);
    scan4.setDimensionX(800);
    scan4.setDimensionY(600);
    scan4.setScanDuration((long) 116);

    scans.add(scan1);
    scans.add(scan2);
    scans.add(scan3);
    scans.add(scan4);
  }
  
  @Before
  public void setUp() throws Exception {
    setUpCdmById(CDM_ID_SCANTAILOR);
    prepareCdm();
    createScansCSV = new CreateScansCSVImpl();
    createScansCSV.execute(CDM_ID_SCANTAILOR, this.scans);
  }

  @After
  public void tearDown() throws Exception {
    deleteCdmById(CDM_ID_SCANTAILOR);
  }

}
