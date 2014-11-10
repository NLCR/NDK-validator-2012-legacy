package com.logica.ndk.tm.utilities.transformation.em;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.CDMUtilityTest;


public class CreateEmConfigImplTest extends CDMUtilityTest {

  private final CreateEmConfigImpl createEmConfig = new CreateEmConfigImpl();

  @Before
  public void setUp() throws Exception {
    setUpCdmById(CDM_ID_EM);
  }

  @After
  public void tearDown() throws Exception {
    deleteCdmById(CDM_ID_EM);
  }

  @Test
  public void testCreate() throws Exception {

    Integer countOfProcessedFiles = createEmConfig.create(CDM_ID_EM, "ABBY");
    assertThat(countOfProcessedFiles)
        .isNotNull()
        .isEqualTo(0);

    final File emConfigFile = new CDM().getEmConfigFile(CDM_ID_EM);

    assertThat(emConfigFile)
        .isNotNull()
        .exists();

    final List<String> lines = FileUtils.readLines(emConfigFile);
    assertThat(lines)
        .isNotNull()
        .isNotEmpty()
        .hasSize(2);
  }

}
