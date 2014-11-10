package com.logica.ndk.tm.utilities.transformation.mns;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.CDMUtilityTest;
import com.logica.ndk.tm.utilities.transformation.CreateMetsMnsImpl;

@Ignore
public class CreateMetsMnsImplTest extends CDMUtilityTest {

  CDM cdm;
  String cdmId = CDM_ID_COMMON;
  @Before
  public void setUp() throws Exception {
    //setUpEmptyCdm();
    
    setUpCdmById(cdmId);
    cdm = new CDM();
  }

  @After
  public void tearDown() throws Exception {
    FileUtils.cleanDirectory(cdm.getRawDataDir(cdmId));
    //FileUtils.deleteDirectory(cdm.getCdmDir(CDM_ID_EMPTY));
  }

  @Ignore
  public void testExecuteTei() throws IOException {
    FileUtils.copyDirectory(new File("test-data/import/mns/tei"), cdm.getRawDataDir(cdmId));
    cdm.updateProperty(cdmId, "packageId", "NAK___KNIH_VALD_A_75_2GKH");
    CreateMetsMnsImpl updateMets = new CreateMetsMnsImpl();
    updateMets.execute(cdmId);
  }

  @Ignore
  public void testExecuteOriginal() throws IOException {
    FileUtils.copyDirectory(new File("test-data/import/mns/original/NKCR__XI_A_12________1QOK"), cdm.getRawDataDir(cdmId));
    cdm.updateProperty(cdmId, "packageId", "NKCR__XI_A_12________1QOK");
    CreateMetsMnsImpl updateMets = new CreateMetsMnsImpl();
    updateMets.execute(cdmId);
  }
  
}
