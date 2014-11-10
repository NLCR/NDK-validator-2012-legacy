package com.logica.ndk.tm.utilities.file;

import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.cdm.CDMMetsWAHelper;

public class CreateEmptyCdmWAImplIT {
  @Ignore
  public void testExecute() {
      long start = System.currentTimeMillis();
      String execute = null;
      try {
        execute = new CreateEmptyCdmWAImpl().execute(CDMMetsWAHelper.DOCUMENT_TYPE_WEB_ARCHIVE);
      }
      catch (CDMException e) {
        e.printStackTrace();
      }
      System.out.println("Takes: " + ((System.currentTimeMillis() - start) / 1000) + "s.");
      System.out.println("Result:" + execute);
  }
}
