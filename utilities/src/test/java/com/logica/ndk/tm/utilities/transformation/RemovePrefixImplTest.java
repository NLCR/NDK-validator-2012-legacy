/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

import java.io.File;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.CDMUtilityTest;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author kovalcikm
 */
@Ignore
public class RemovePrefixImplTest extends CDMUtilityTest {

  @Before
  public void setUp() throws Exception {
    setUpEmptyCdm();
    new File(cdm.getMasterCopyDir(CDM_ID_EMPTY) + File.separator + "MC_testFile0001.jp2").createNewFile();
    new File(cdm.getMasterCopyDir(CDM_ID_EMPTY) + File.separator + "MC_testFile0002.jp2").createNewFile();
  }

  @Ignore
  public void test1() {
    Integer response = new RemovePrefixImpl().execute(cdm.getMasterCopyDir(CDM_ID_EMPTY).getAbsolutePath(), "MC");
    assertThat(response.equals(2));
  }
  
  @Ignore
  public void test2() {
    Integer response = new RemovePrefixImpl().execute(cdm.getMasterCopyDir(CDM_ID_EMPTY).getAbsolutePath(), "MC");
    assertThat(response.equals(0));
  }

}
