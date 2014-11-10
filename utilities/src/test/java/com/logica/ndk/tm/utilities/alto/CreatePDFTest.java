package com.logica.ndk.tm.utilities.alto;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.CDMUtilityTest;
import com.logica.ndk.tm.utilities.alto.exception.InconsistentDataException;
import com.logica.ndk.tm.utilities.alto.exception.InvalidSourceFolderException;

public class CreatePDFTest extends CDMUtilityTest {

  private static final String cdmId = "alto";
  
  @Before
  public void prepareData() throws Exception {
    setUpCdmById(cdmId);
  }
  
  @After
  public void cleanData() throws Exception {
    deleteCdmById(cdmId);
  }

  
  @Test(expected = IllegalArgumentException.class)
  public void testExecuteNoParams() throws IllegalArgumentException, InconsistentDataException, InvalidSourceFolderException {
    CreatePDFImpl u = new CreatePDFImpl();
    u.execute(null, false, cdm.getWorkspaceDir(cdmId) + "/MZ");
  }

  @Ignore//(expected = InvalidSourceFolderException.class)
  public void testExecuteEmptyDir() throws IllegalArgumentException, InconsistentDataException, InvalidSourceFolderException {
    CreatePDFImpl u = new CreatePDFImpl();
    u.execute("123", false, cdm.getWorkspaceDir(cdmId) + "/MZ");
  }

  @Ignore
  public void testPDF() throws IllegalArgumentException, InconsistentDataException, InvalidSourceFolderException {
    
    CreatePDFImpl u = new CreatePDFImpl();
    u.execute(cdmId, false, cdm.getWorkspaceDir(cdmId) + "/MZ");
    File pdf = new File(cdm.getWorkspaceDir(cdmId) + "/MZ/PDF_" + cdmId + ".pdf");
    assertTrue(pdf.exists());
    assertTrue(pdf.isFile());
    assertTrue(FileUtils.sizeOf(pdf) > 0);
    
  }
}