/**
 * 
 */
package com.logica.ndk.tm.utilities.validation;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.CDMUtilityTest;

/**
 * @author kovalcikm
 */
@Ignore
public class ValidateMnsImplTest extends CDMUtilityTest {

  CDM cdm = new CDM();
  String cdmId = CDM_ID_EMPTY;
  private static final File SOURCE_DIR = new File("test-data\\import\\mns\\tei\\EX\\");

  @Before
  public void setUp() throws Exception {
    setUpEmptyCdm();
    FileUtils.copyDirectory(SOURCE_DIR, new File(cdm.getRawDataDir(cdmId) + File.separator + "EX"));
  }

  @Ignore
  public void test() {
    ValidationViolationsWrapper response = new ValidateMnsImpl().execute("3bbb20b0-dc96-11e1-93a8-00505682629d", true);
    assertThat(response.getViolationsList().isEmpty());
  }

  @Ignore//(expected = BusinessException.class)
  public void testWrong() throws IOException {
    File file = new File(cdm.getRawDataDir(cdmId) + File.separator + "EX" + File.separator + "test.txt");
    file.createNewFile();
    new ValidateMnsImpl().execute(cdmId, true);
  }
}
