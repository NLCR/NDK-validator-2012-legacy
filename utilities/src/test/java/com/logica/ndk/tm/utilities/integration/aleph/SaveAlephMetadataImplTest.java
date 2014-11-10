package com.logica.ndk.tm.utilities.integration.aleph;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.file.CreateEmptyCdmImpl;

@Ignore
public class SaveAlephMetadataImplTest {

  final static String md = "aaa\nbbb\nccc";
  private static CDM cdm;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    cdm = new CDM();
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    cdm = null;
  }

  @Ignore
  public void testExecute() {
    String cdmid1 = null;
    try {
      final CreateEmptyCdmImpl u1 = new CreateEmptyCdmImpl();
      final SaveAlephMetadataImpl u2 = new SaveAlephMetadataImpl();
      cdmid1 = u1.execute("barcode1","taskId");
      u2.execute(cdmid1, md);
      // FIXME assert ?
    }
    finally {
      FileUtils.deleteQuietly(cdm.getCdmDir(cdmid1));
    }

  }

}
