package com.logica.ndk.tm.cdm;

import java.io.File;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class CDMTest {

  private static File baseDir = null;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    baseDir = new File(System.getProperty("java.io.tmpdir") + "/cdmtest");
    FileUtils.deleteQuietly(baseDir);
    if (!baseDir.mkdirs()) {
      throw new RuntimeException("Can't create " + baseDir);
    }
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    FileUtils.deleteQuietly(baseDir);
  }

  @Ignore
  public void testCreate() throws Exception {
    final String cdmId = "test.create.0001";
    final CDM cdm = createTestCdm(cdmId);
    Assert.assertTrue(cdm.getCdmDir(cdmId).exists());
    Assert.assertTrue(cdm.getMasterCopyDir(cdmId).exists());
    Assert.assertTrue(cdm.getUserCopyDir(cdmId).exists());
    Assert.assertTrue(cdm.getAmdDir(cdmId).exists());
  }

  @Ignore
  public void testDelete() throws Exception {
    final String cdmId = "test.delete.0001";
    final CDM cdm = createTestCdm(cdmId);
    Assert.assertTrue(cdm.getCdmDir(cdmId).exists());
    final File recycled = cdm.deleteCdm(cdmId);
    Assert.assertFalse(cdm.getCdmDir(cdmId).exists());
    Assert.assertTrue(recycled.exists());
    FileUtils.deleteQuietly(recycled); // cleanup
  }

  @Ignore
  public void testMETS() throws Exception {
    final String cdmId = "test.mets.0001";
    final CDM cdm = createTestCdm(cdmId);
    File f = cdm.createMetsFromContent(cdmId, true);
    Assert.assertNotNull(f);
    Assert.assertTrue(f.exists());
    boolean valid = cdm.validateCdm(cdmId, true);
    Assert.assertTrue(valid);
  }

  @Ignore
  public void testGit() throws Exception {
    final String cdmId = "test.mets.0001";
    final CDM cdm = createTestCdm(cdmId);
    System.out.println("git dir = " + cdm.getGitDir(cdmId));
  }

  @Ignore
  public void testCompound() throws Exception {
    final String cdmId = "test.mets.0001";
    final CDM cdm = createTestCdm(cdmId);
    boolean isCompound = cdm.isCompound(cdmId);
    Assert.assertEquals(isCompound, false);
    final String[] REFS = new String[] { "111", "222", "333" };
    cdm.setReferencedCdmList(cdmId, REFS);
    final String[] refs = cdm.getReferencedCdmList(cdmId);
    Assert.assertTrue(ArrayUtils.isEquals(REFS, refs));
    isCompound = cdm.isCompound(cdmId);
    Assert.assertEquals(isCompound, true);
  }

  @Ignore
  public void testCreateEmpty() throws Exception {
    final String cdmId = "test.empty.0001";
    final CDM cdm = createTestCdm(cdmId);
    Assert.assertTrue(cdm.getCdmDir(cdmId).exists());
  }

  @Test
  public void testGetAllRecycleBinDirs() {
    final CDM cdm = new CDM();
    final Set<File> f = cdm.getAllRecycleBinDirs();
    System.out.println(f);
  }

  private CDM createTestCdm(String cdmId) throws Exception {
    final CDM cdm = new CDM(baseDir, new CDMSchema());
    cdm.createEmptyCdm(cdmId, true);
    return cdm;
  }
}
