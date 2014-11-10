package com.logica.ndk.tm.utilities.file;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.cdm.CDM;

@Ignore
public class RsyncImplTest {

  private final static String CDM_ID = "abc";
  
  @Before
  public void prepareData() {
    File source = new File("test-data/alto/CDM");
    CDM cdm = new CDM();
    File target = cdm.getCdmDir(CDM_ID);
    try {
      FileUtils.copyDirectory(source, target, new FileFilter() {

        @Override
        public boolean accept(File pathname) {
          return !pathname.getName().contains(".svn");
        }
      
      });
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  @After
  public void cleanData() {
    CDM cdm = new CDM();
    File target = cdm.getCdmDir(CDM_ID);
    try {
      FileUtils.deleteDirectory(target);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Ignore
  public void testExecute() {
    RsyncImpl u = new RsyncImpl();
    String pathId = CDM_ID;
    String localURNString = "/cygdrive/" + new File("test-data/a/").getAbsolutePath().replace(":\\", "/").replace("\\", "/"); 
    System.out.println(localURNString);
    u.execute(pathId, localURNString);
    assertTrue(new File(new CDM().getRawDataDir(CDM_ID).getAbsolutePath() + "/a/logo.jpg").exists());
  }

}
