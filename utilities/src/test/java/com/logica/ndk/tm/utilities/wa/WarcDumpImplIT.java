package com.logica.ndk.tm.utilities.wa;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.cdm.CDM;

public class WarcDumpImplIT {
  @Ignore
  public void testExecute() {
    //String sourceDir = "c:\\NDK\\data_test\\_wa\\new\\arc\\warc\\";
    //String dumpDir = "c:\\NDK\\data_test\\_wa\\new\\arc\\warc\\dump";
    String cdmId = "08a7b9f0-5f5b-11e4-81d0-00505682629d";
    CDM cdm = new CDM();
    String sourceDir = cdm.getWarcsDataDir(cdmId).getAbsolutePath();
    String dumpDir = cdm.getTxtDir(cdmId).getAbsolutePath();
    String workDir = cdm.getWorkspaceDir(cdmId).getAbsolutePath();
    String execute = null;
    try {
      execute = new WarcDumpImpl().execute(cdmId, sourceDir, dumpDir, workDir);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println(execute);
  }
}
