package com.logica.ndk.tm.utilities.transformation.em;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.fest.assertions.Condition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.CDMUtilityTest;

public class GetBibliographicDataImplTest extends CDMUtilityTest {

  private static final String ISSN_CDM_ID = "f6fd80d0-3563-11e3-9eaf-005056";
	
  private final GetBibliographicDataImpl u = new GetBibliographicDataImpl();
  private List<String> newCdmIds;

  @Before
  public void setUp() throws Exception {
    setUpCdmById(CDM_ID_SPLIT);
    setUpCdmById(ISSN_CDM_ID);
  }

  @After
  public void tearDown() throws Exception {
    deleteCdmById(CDM_ID_SPLIT);
    deleteCdmById(ISSN_CDM_ID);
  }

  @Test
  public final void testExecute() throws Exception {

    BibliographicData biblio = u.execute(CDM_ID_SPLIT);
    
    assertThat(biblio).isNotNull();
    assertThat(biblio.getTitle()).isEqualTo("Obchodní právo");
    // assertThat(biblio.getTitle()).equals(""); TODO majdaf - not yet implemented
    assertThat(biblio.getLanguage()).isEqualTo("cze");
    assertThat(biblio.getIsbn()).isNull();
    assertThat(biblio.getIssn()).isEqualTo("1210-8278");
    assertThat(biblio.getCcnb()).isEqualTo("cnb000358070");
    assertThat(biblio.getSigla()).isEqualTo("ABA001");
  }
  
  @Test
  public void testGetIssn() throws Exception {
	 BibliographicData biblio = u.execute(ISSN_CDM_ID);
	 assertEquals("0862-6634", biblio.getIssn());		
  }
}
