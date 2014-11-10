/**
 * 
 */
package com.logica.ndk.tm.utilities.ocr;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.CDMUtilityTest;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author kovalcikm
 */
public class EvaluateOcrResultImplTest extends CDMUtilityTest {
  private static final String CDM_ID = CDM_ID_COMMON;

  @Before
  public void setUp() throws Exception {
    setUpCdmById(CDM_ID_COMMON);
    new FilesListImpl().generate(CDM_ID);
  }

  @Test
  public void test() {
    String result = new EvaluateOcrResultImpl().execute(CDM_ID, 0);
    Assertions.assertThat(result.equals(ResponseStatus.RESPONSE_OK));
  }

  @Test(expected = BusinessException.class)
  public void testEx() {
    String result = new EvaluateOcrResultImpl().execute(CDM_ID, 1);
  }

  @Test(expected = SystemException.class)
  public void testWrongPageCount() {
    File txtFirstFile = cdm.getTxtDir(CDM_ID).listFiles()[0];
    FileUtils.deleteQuietly(txtFirstFile);
    new EvaluateOcrResultImpl().execute(CDM_ID, 0);
  }
}
