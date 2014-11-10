package com.logica.ndk.tm.utilities.tika;

import org.junit.Test;

import com.logica.ndk.tm.utilities.tika.TikaException;
import com.logica.ndk.tm.utilities.tika.TikaService;
import com.logica.ndk.tm.utilities.tika.TikaServiceException;

public class TikaServiceIT {
  @Test
  public void testExtract() {
    String inputFile = "c:\\NDK\\data_test\\_wa\\new\\arc\\warc\\data_files\\urn-uuid-084fa218-1598-4d63-be86-96dfb5b733e0.pdf";
    String outputFile = "c:\\NDK\\data_test\\_wa\\new\\arc\\warc\\data_files\\tyka_out\\urn-uuid-084fa218-1598-4d63-be86-96dfb5b733e0.txt";
    try {
      new TikaService().extract(inputFile, outputFile);
    }
    catch (TikaServiceException e) {
      e.printStackTrace();
    }
    catch (TikaException e) {
      e.printStackTrace();
    }
  }
}
