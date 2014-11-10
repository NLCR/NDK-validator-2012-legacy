package com.logica.ndk.tm.utilities.integration.aleph;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class GetAlephItemIT {

  private GetAlephItemImpl getAlephItemUtility = new GetAlephItemImpl();

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Ignore
  public void testGetItem() throws Exception {

    AlephItem item = getAlephItemUtility.getItem("1000331208", "000357969", BaseGetAleph.LIBRARY_NK, BaseGetAleph.ALEPH_BASE_NK_MAIN);

    assertThat(item).isNotNull();
    assertThat(item.getRecKey()).isNotNull();
    assertThat(item.getRecKey()).isEqualTo("000357969000120");

    assertThat(item.getBarcode()).isNotNull();
    assertThat(item.getBarcode()).isEqualTo("1000331208");
  }

}
