package com.logica.ndk.tm.utilities.djvu;

import java.io.File;

import org.junit.Test;

public class DjVuLibreServiceIT {
  @Test
  public void test() {
    File inputFile = new File("d:\\work\\NDK\\_vzorky_dat\\import_K4\\convert\\1011000001.djvu");
    File outputDir = new File("d:\\work\\NDK\\_vzorky_dat\\import_K4\\convert\\");
    try {
      new DjVuLibreService().convertToTiff(inputFile, outputDir);
    }
    catch (DjVuLibreException e) {
      e.printStackTrace();
    }
  }
}
