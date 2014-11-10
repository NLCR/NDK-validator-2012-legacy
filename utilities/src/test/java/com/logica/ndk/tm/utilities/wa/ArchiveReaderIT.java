package com.logica.ndk.tm.utilities.wa;

import java.io.File;
import java.io.IOException;

import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveReaderFactory;
import org.junit.Test;

public class ArchiveReaderIT {
  @Test
  public void testIsValidWarc() {
    File warcFile = new File("c:\\NDK\\data_test\\_wa\\warc\\new\\WEB-20120507085807562-00000-7863~crawler00.webarchiv.cz~7778.warc");
    try {
      ArchiveReader reader = ArchiveReaderFactory.get(warcFile);
      boolean valid = reader.isValid();
      System.out.println(valid);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testIsValidArc() {
    File arcFile = new File("c:\\NDK\\data_test\\_wa\\arc\\a.arc");
    try {
      ArchiveReader reader = ArchiveReaderFactory.get(arcFile);
      boolean valid = reader.isValid();
      System.out.println(valid);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }
}
