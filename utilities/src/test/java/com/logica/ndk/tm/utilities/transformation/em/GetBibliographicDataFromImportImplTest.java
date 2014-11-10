/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.em;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.CDMUtilityTest;

/**
 * @author kovalcikm
 *
 */
@Ignore
public class GetBibliographicDataFromImportImplTest extends CDMUtilityTest{

  private final GetBibliographicDataFromImportImpl u = new GetBibliographicDataFromImportImpl();
  
  @Before
  public void setUp() throws Exception {
    setUpCdmById(CDM_ID_ALTO);
  }

  @After
  public void tearDown() throws Exception {
    deleteCdmById(CDM_ID_ALTO);
  }

  @Ignore
  public final void testExecute() throws Exception {

//    BibliographicData biblio = u.execute(cdm.getCdmDataDir(CDM_ID_ALTO).getAbsolutePath());
    BibliographicData biblio = u.execute("C:\\Users\\kovalcikm\\AppData\\Local\\Temp\\cdm\\procesing_NDK-000000000440_1361795459514");
    
    assertThat(biblio).isNotNull();
    assertThat(biblio.getTitle()).isEqualTo("Bila nemoc");
    assertThat(biblio.getLanguage()).isEqualTo("cze");
    assertThat(biblio.getIssn()).isNull();
    assertThat(biblio.getIsbn()).isEqualTo("80-86216-47-0");
    assertThat(biblio.getCcnb()).isEqualTo("cnb001490472");
    assertThat(biblio.getSigla()).isEqualTo("BOA001");
  }
}
