package com.logica.ndk.tm.utilities.jhove;

import java.io.File;
import java.net.URISyntaxException;

import static org.junit.Assert.*;
import org.junit.Test;

import com.logica.ndk.tm.utilities.transformation.JhoveException;

public class JhoveServiceValidateIT {
  private static final String TEST_FILE_XML = "./com/logica/ndk/tm/utilities/jhove/test_0001.tif.xml";
  private static final String TEST_FILE_TIFF = "./com/logica/ndk/tm/utilities/jhove/test_A4_2_0012.tif";
  private static final String TEST_FILE_JPG = "./com/logica/ndk/tm/utilities/jhove/test_A4_2_0012.tif.jpg";
  private static final String TEST_FILE_JPEG2000 = "./com/logica/ndk/tm/utilities/jhove/test_A4_2_0012.tif.jp2";
  private static final String TEST_FILE_PDF = "./com/logica/ndk/tm/utilities/jhove/urn-uuid-0bf4eb81-2b2b-4aae-b5a9-4aa6631b9f8f.pdf";
  
  @Test
  public void testValidateTiff() {
    try {
      assertTrue(new JhoveService().validateTiff(new File(getClass().getClassLoader().getResource(TEST_FILE_TIFF).toURI())));
    }
    catch (JhoveException e) {
      e.printStackTrace();
    }
    catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }
  
  @Test
  public void testValidateTiffError() {
    try {
      assertFalse(new JhoveService().validateTiff(new File(getClass().getClassLoader().getResource(TEST_FILE_XML).toURI())));
    }
    catch (JhoveException e) {
      e.printStackTrace();
    }
    catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }
  
  @Test
  public void testValidateJpg() {
    try {
      assertTrue(new JhoveService().validateJpg(new File(getClass().getClassLoader().getResource(TEST_FILE_JPG).toURI())));
    }
    catch (JhoveException e) {
      e.printStackTrace();
    }
    catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }
  
  @Test
  public void testValidateJpgError() {
    try {
      assertFalse(new JhoveService().validateJpg(new File(getClass().getClassLoader().getResource(TEST_FILE_XML).toURI())));
    }
    catch (JhoveException e) {
      e.printStackTrace();
    }
    catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }
  
  @Test
  public void testValidateJpeg2000() {
    try {
      assertTrue(new JhoveService().validateJpeg2000(new File(getClass().getClassLoader().getResource(TEST_FILE_JPEG2000).toURI())));
    }
    catch (JhoveException e) {
      e.printStackTrace();
    }
    catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }
  
  @Test
  public void testValidateJpeg2000Error() {
    try {
      assertFalse(new JhoveService().validateJpeg2000(new File(getClass().getClassLoader().getResource(TEST_FILE_XML).toURI())));
    }
    catch (JhoveException e) {
      e.printStackTrace();
    }
    catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }
  
  @Test
  public void testValidatePdf() {
    try {
      assertTrue(new JhoveService().validatePdf(new File(getClass().getClassLoader().getResource(TEST_FILE_PDF).toURI())));
    }
    catch (JhoveException e) {
      e.printStackTrace();
    }
    catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }
  
  @Test
  public void testValidatePdfError() {
    try {
      assertFalse(new JhoveService().validatePdf(new File(getClass().getClassLoader().getResource(TEST_FILE_XML).toURI())));
    }
    catch (JhoveException e) {
      e.printStackTrace();
    }
    catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }
}
