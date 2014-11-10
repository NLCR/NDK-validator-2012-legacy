/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.em;

import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import static org.fest.assertions.Assertions.assertThat;

import com.logica.ndk.tm.utilities.CDMUtilityTest;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author kovalcikm
 */
public class ExtendEmCsvImplTest extends CDMUtilityTest {

  @Before
  public void setUp() throws Exception {
    setUpCdmById(CDM_ID_EM);
    setUpCdmById(CDM_ID_COMMON);
  }

  @Test
  public void test() {
    ExtendEmCsvImpl extendEmCsv = new ExtendEmCsvImpl();
    extendEmCsv.execute(CDM_ID_COMMON, "ABBY", "added-taskId");

    for (EmCsvRecord record : EmCsvHelper.getRecords(EmCsvHelper.getCsvReader(cdm.getEmConfigFile(CDM_ID_COMMON).getAbsolutePath()))) {
      assertThat(record.getProfilOCR().equals("LATIN"));
    }
  }

  @Test(expected = SystemException.class)
  public void testNoFile() {
    ExtendEmCsvImpl extendEmCsv = new ExtendEmCsvImpl();
    extendEmCsv.execute(CDM_ID_EM, "ABBY", "added-taskId");

  }

  @After
  public void cleanUp() throws Exception {
    deleteCdmById(CDM_ID_EM);
    deleteCdmById(CDM_ID_COMMON);
  }
}
