package com.logica.ndk.tm.utilities.alto;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.CDMUtilityTest;

@Ignore
public class CreateTXTImplTest extends CDMUtilityTest {

  private String cdmId = "alto";

  @Before
  public void prepareData() throws Exception {
    setUpCdmById(cdmId);
  }

  @After
  public void cleanData() throws Exception {
    deleteCdmById(cdmId);
  }

  @Ignore
  public void testTXT() throws IOException {
    
    CreateTXTImpl u = new CreateTXTImpl();
    u.execute(cdmId,cdm.getWorkspaceDir(cdmId) + "/MZ/");
    File txt = new File(cdm.getWorkspaceDir(cdmId) + "/MZ/TXT_" + cdmId + ".txt");
    assertTrue(txt.exists());
    assertTrue(txt.isFile());
    assertTrue(FileUtils.sizeOf(txt) > 0);
    
    FileInputStream fstream = new FileInputStream(txt);
    DataInputStream in = new DataInputStream(fstream);
    BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
    String pattern1 = "Bankovní soustava";
    String pattern2 = "ošetřit lehké zranění";
    String strLine;
    boolean pattern1ok = false;
    boolean pattern2ok = false;
    while ((strLine = br.readLine()) != null)   {
      if (strLine.contains(pattern1)) pattern1ok = true; 
      if (strLine.contains(pattern2)) pattern2ok = true; 
    }
    in.close();
    assertTrue(pattern1ok);
    assertTrue(pattern2ok);
    
  }
}
