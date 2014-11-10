package com.logica.ndk.tm.cdm;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.dom4j.DocumentException;
import org.junit.Assert;
import org.junit.Test;

public class CDMAltoHelperTest {
  @Test
  public void test() {
    try {
      URL url = getClass().getClassLoader().getResource("CDM_alto_helper/1_1_testrenat_00007.tif.xml");
      File file = new File(url.toURI());
      CDMAltoHelper cdmAltoHelper = new CDMAltoHelper(file.getAbsolutePath());
      Assert.assertEquals("Page1", cdmAltoHelper.getPageId());
    }
    catch (URISyntaxException e) {
      e.printStackTrace();
    }
    catch (DocumentException e) {
      e.printStackTrace();
    }

  }
}
