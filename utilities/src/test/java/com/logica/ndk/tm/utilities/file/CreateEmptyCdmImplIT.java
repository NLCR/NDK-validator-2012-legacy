package com.logica.ndk.tm.utilities.file;

import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.cdm.CDMException;

public class CreateEmptyCdmImplIT {
  @Ignore
  public void testExecute() {
      long start = System.currentTimeMillis();
      String execute = null;
      try {
        execute = new CreateEmptyCdmImpl().execute("ANL000002","taskId");
      }
      catch (CDMException e) {
        e.printStackTrace();
      }
      System.out.println("Takes: " + ((System.currentTimeMillis() - start) / 1000) + "s.");
      System.out.println("Result:" + execute);
  }
}
