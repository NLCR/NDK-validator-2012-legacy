package com.logica.ndk.tm.utilities.ocr;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.commons.utils.test.TestUtils;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.process.CheckOcrOutputResponse;
import com.logica.ndk.tm.utilities.CDMUtilityTest;


@Ignore
public class CheckOcrOutputImplTest extends CDMUtilityTest {

  private final CheckOcrOutputImpl checkOcrOutput = new CheckOcrOutputImpl();

  private File outputDir;
  private File exceptionsDir;
  private File testDir;
  private File resourcesDir;
  private File ocrListFile;

  private final int CHECK_INTERVAL_MINUTES = TmConfig.instance().getInt("process.ocr.checkSoftLimit");
  private final int CHECK_TIMEOUT_MINUTES = TmConfig.instance().getInt("process.ocr.checkTimeoutLimit");

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    setUpEmptyCdm();

    testDir = new File(FileUtils.getTempDirectory(), "CheckOcrOutputImplTest");
    testDir.mkdirs();
    outputDir = new File(testDir, "outputDir");
    outputDir.mkdirs();
    exceptionsDir = new File(testDir, "exceptionsDir");
    exceptionsDir.mkdirs();
    resourcesDir = new File("test-data/ocr");
    ocrListFile = cdm.getOcrFilesListFile(CDM_ID_EMPTY);

//    TestUtils.setField(checkOcrOutput, "OCR_ENGINE_OUTPUT_DIR", outputDir.getAbsolutePath());
//    TestUtils.setField(checkOcrOutput, "OCR_ENGINE_EXCEPTIONS_DIR", exceptionsDir.getAbsolutePath());
//    TestUtils.setField(checkOcrOutput, "cdm", cdm);
  }

  @After
  public void tearDown() throws Exception {
    FileUtils.deleteDirectory(testDir);

    deleteEmptyCdm();
  }

  @Test
  public void testCheckDone() throws Exception {

    FileUtils.copyFileToDirectory(new File(resourcesDir, "scan0001.tif.result.xml"), new File(outputDir, CDM_ID_EMPTY));
    FileUtils.copyFileToDirectory(new File(resourcesDir, "scan0002.tif.result.xml"), new File(outputDir, CDM_ID_EMPTY));
    FileUtils.copyFileToDirectory(new File(resourcesDir, "scan0003.tif.result.xml"), new File(outputDir, CDM_ID_EMPTY));

    prepareOcrListFile(new Date());

    final CheckOcrOutputResponse response = checkOcrOutput.check(CDM_ID_EMPTY);

    assertThat(response).isNotNull();
    assertThat(response.getResponseCode())
        .isNotNull()
        .isEqualTo(OCRStatus.RESPONSE_OK);
    assertThat(response.getOcrRate())
        .isNotNull()
        .isEqualTo(80);
    assertThat(response.getOcrPagesException())
        .isEqualTo(0);
    assertThat(response.getOcrPagesOk())
        .isEqualTo(3);
  }

  @Test
  public void testCheckInProgress() throws Exception {

    FileUtils.copyFileToDirectory(new File(resourcesDir, "scan0001.tif.result.xml"), new File(outputDir, CDM_ID_EMPTY));
    FileUtils.copyFileToDirectory(new File(resourcesDir, "scan0002.tif.result.xml"), new File(outputDir, CDM_ID_EMPTY));
    // the last file is missing

    prepareOcrListFile(new Date());

    final CheckOcrOutputResponse response = checkOcrOutput.check(CDM_ID_EMPTY);

    assertThat(response).isNotNull();
    assertThat(response.getResponseCode())
        .isNotNull()
        .isEqualTo(OCRStatus.RESPONSE_IN_PROGRESS);
    assertThat(response.getOcrRate())
        .isEqualTo(0); // ocrRate is primitive type of integer
  }

  @Test
  public void testCheckException() throws Exception {

    FileUtils.copyFileToDirectory(new File(resourcesDir, "scan0001.tif.result.xml"), new File(outputDir, CDM_ID_EMPTY));
    FileUtils.copyFileToDirectory(new File(resourcesDir, "scan0002.tif.result.xml"), new File(outputDir, CDM_ID_EMPTY));
    // the last file is in exception dir
    FileUtils.copyFileToDirectory(new File(resourcesDir, "scan0003.tif.result.xml"), new File(exceptionsDir, CDM_ID_EMPTY));

    prepareOcrListFile(new Date());

    final CheckOcrOutputResponse response = checkOcrOutput.check(CDM_ID_EMPTY);

    assertThat(response).isNotNull();
    assertThat(response.getResponseCode())
        .isNotNull()
        .isEqualTo(OCRStatus.RESPONSE_EXCEPTION_OCCURED);
    assertThat(response.getOcrPagesOk())
        .isEqualTo(2);
    assertThat(response.getOcrRate())
        .isGreaterThan(0)
        .isEqualTo(80);
  }

  @Test
  public void testCheckSoftLimit() throws Exception {

    prepareOcrListFile(new Date());

    final File outputDirData = new File(outputDir, CDM_ID_EMPTY);
    final File exceptionsDirData = new File(exceptionsDir, CDM_ID_EMPTY);
    outputDirData.mkdirs();
    exceptionsDirData.mkdirs();
    // set output dir last modified time
    final Calendar time = new GregorianCalendar();
    time.add(Calendar.MINUTE, (CHECK_INTERVAL_MINUTES * -1 - 10));
    outputDirData.setLastModified(time.getTimeInMillis());
    exceptionsDirData.setLastModified(time.getTimeInMillis());

    final CheckOcrOutputResponse response = checkOcrOutput.check(CDM_ID_EMPTY);

    assertThat(response).isNotNull();
    assertThat(response.getResponseCode())
        .isNotNull()
        .isEqualTo(OCRStatus.RESPONSE_SOFT_LIMIT_EXCEEDED);
    assertThat(response.getOcrRate())
        .isEqualTo(0); // ocrRate is primitive type of integer
  }

  @Test
  public void testCheckHardLimit() throws Exception {

    FileUtils.copyFileToDirectory(new File(resourcesDir, "scan0001.tif.result.xml"), new File(outputDir, CDM_ID_EMPTY));
    FileUtils.copyFileToDirectory(new File(resourcesDir, "scan0002.tif.result.xml"), new File(outputDir, CDM_ID_EMPTY));
    FileUtils.copyFileToDirectory(new File(resourcesDir, "scan0003.tif.result.xml"), new File(outputDir, CDM_ID_EMPTY));

    final Calendar cal = new GregorianCalendar();
    cal.add(Calendar.MINUTE, (CHECK_TIMEOUT_MINUTES * -1 - 60));

    prepareOcrListFile(cal.getTime());

    final CheckOcrOutputResponse response = checkOcrOutput.check(CDM_ID_EMPTY);

    assertThat(response).isNotNull();
    assertThat(response.getResponseCode())
        .isNotNull()
        .isEqualTo(OCRStatus.RESPONSE_HARD_LIMIT_EXCEEDED);
    assertThat(response.getOcrRate())
        .isEqualTo(0); // ocrRate is primitive type of integer
  }

  private void prepareOcrListFile(final Date createdDate) throws IOException {
    FileUtils.copyFileToDirectory(new File(resourcesDir, CDM_ID_EMPTY + ".ocrList"), cdm.getOcrDir(CDM_ID_EMPTY));

    String content = FileUtils.readFileToString(ocrListFile);
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    content = content.replace("${createdDate}", dateFormat.format(createdDate));
    FileUtils.write(ocrListFile, content, false);
  }
}
