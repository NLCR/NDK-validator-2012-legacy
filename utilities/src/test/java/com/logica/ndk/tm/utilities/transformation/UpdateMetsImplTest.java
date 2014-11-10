package com.logica.ndk.tm.utilities.transformation;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.CDMUtilityTest;

@Ignore
public class UpdateMetsImplTest extends CDMUtilityTest {
  private static final String cdmId = "common";
  UpdateMetsImpl updateMets;

  @Before
  public void prepareCdm() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    new CDM().zapCdm(cdmId);
  }

  @Ignore
  public void testValidate() {
    final CDM cdm = new CDM();
    updateMets = new UpdateMetsImpl();
    updateMets.execute(cdmId);
    String validMetsName = cdm.getCdmDataDir(cdmId)+File.separator+"METS_common.xml";
    System.out.println(cdm.getMetsFile(cdmId).toString());
    assertThat(cdm.getMetsFile(cdmId).toString()).isEqualTo(validMetsName);
  }

}
