/**
 * 
 */
package com.logica.ndk.tm.utilities.io;

import org.junit.Test;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.CDMUtilityTest;

/**
 * @author kovalcikm
 */
public class FailIfDirExistsImplTest extends CDMUtilityTest {

  private static final String CDM_ID = CDM_ID_COMMON;

  @Test
  public void test() {
    new FailIfDirExistsImpl().execute(cdm.getPremisDir(CDM_ID).getAbsolutePath());
  }

  @Test(expected = BusinessException.class)
  public void testEx() {
    new FailIfDirExistsImpl().execute(cdm.getMasterCopyDir(CDM_ID).getAbsolutePath());
  }
}
