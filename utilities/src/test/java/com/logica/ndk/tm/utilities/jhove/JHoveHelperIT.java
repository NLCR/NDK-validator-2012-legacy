package com.logica.ndk.tm.utilities.jhove;

import java.io.File;
import java.net.URISyntaxException;

import org.dom4j.DocumentException;
import org.junit.Test;

public class JHoveHelperIT {
  @Test
  public void test() {
    try {
      String filePath = "./com/logica/ndk/tm/utilities/jhove/test_A4_2_0001.tif.jp2.xml";
      File file = new File(getClass().getClassLoader().getResource(filePath).toURI());
      JHoveHelper jHoveHelper = new JHoveHelper(file.getAbsolutePath());
      System.out.println(jHoveHelper.getFormat());
      System.out.println(jHoveHelper.getMinorVersionJPEG2000());
      System.out.println(jHoveHelper.getColorSchemeJPEG2000());
      System.out.println(jHoveHelper.getColorDepthJPEG2000());
    }
    catch (DocumentException e) {
      e.printStackTrace();
    }
    catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }
}
