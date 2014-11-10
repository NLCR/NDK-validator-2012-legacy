package com.logica.ndk.tm.utilities.transformation.em;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.CDMUtilityTest;
import com.logica.ndk.tm.utilities.ResponseStatus;

@Ignore
public class PurgeByMetsImplTest extends CDMUtilityTest {

  private final PurgeByMetsImpl purgeByMets = new PurgeByMetsImpl();

  @Before
  public void setUp() throws Exception {
    setUpCdmById(CDM_ID_PURGE);
  }

  @After
  public void tearDown() throws Exception {
    deleteCdmById(CDM_ID_PURGE);
  }

  @Ignore
  public final void testExecute() throws Exception {

    assertThat(cdm.getMasterCopyDir(CDM_ID_PURGE).list())
        .isNotNull()
        .hasSize(4);
    assertThat(cdm.getRawDataDir(CDM_ID_PURGE).list())
        .isNotNull()
        .hasSize(4);
    assertThat(new File(cdm.getWorkspaceDir(CDM_ID_PURGE), "mix/rawData").list())
        .isNotNull()
        .hasSize(4);

    final String result = purgeByMets.execute(CDM_ID_PURGE);

    assertThat(result)
        .isNotNull()
        .isEqualTo(ResponseStatus.RESPONSE_OK);

    assertThat(cdm.getMasterCopyDir(CDM_ID_PURGE).list())
        .isNotNull()
        .hasSize(3);
    assertThat(cdm.getRawDataDir(CDM_ID_PURGE).list())
        .isNotNull()
        .hasSize(3);
    assertThat(new File(cdm.getWorkspaceDir(CDM_ID_PURGE), "mix/rawData").list())
        .isNotNull()
        .hasSize(3);

  }
}
