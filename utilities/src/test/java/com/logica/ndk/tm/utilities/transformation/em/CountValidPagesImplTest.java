package com.logica.ndk.tm.utilities.transformation.em;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.logica.ndk.tm.utilities.CDMUtilityTest;

public class CountValidPagesImplTest extends CDMUtilityTest {

  private final CountValidPagesImpl countValidPages = new CountValidPagesImpl();

  @Before
  public void setUp() throws Exception {
    setUpCdmById(CDM_ID_PURGE);
  }

  @After
  public void tearDown() throws Exception {
    deleteCdmById(CDM_ID_PURGE);
  }

  @Test
  public void testCreate() throws Exception {

    Integer validPages = countValidPages.execute(CDM_ID_PURGE);
    assertThat(validPages)
        .isNotNull()
        .isEqualTo(3);

  }

}
