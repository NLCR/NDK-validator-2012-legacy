package com.logica.ndk.tm.utilities.jhove;

import java.io.File;
import java.net.URISyntaxException;

import static org.junit.Assert.*;
import org.junit.Test;

import com.logica.ndk.tm.utilities.transformation.JhoveException;

public class JhoveServiceIsCompressedTiffIT {
  private static final String TEST_FILE_TIFF_COMPRESSED = "./com/logica/ndk/tm/utilities/jhove/CCITT_3.tif";
  private static final String TEST_FILE_TIFF_NOT_COMPRESSED = "./com/logica/ndk/tm/utilities/jhove/test_A4_2_0012.tif";
  
  @Test
  public void testIsCompressed() {
    try {
      assertFalse(new JhoveService().isUncompressedTiff(new File(getClass().getClassLoader().getResource(TEST_FILE_TIFF_COMPRESSED).toURI())));
    }
    catch (JhoveException e) {
      e.printStackTrace();
    }
    catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }
  
  @Test
  public void testIsNotCompressed() {
    try {
      assertTrue(new JhoveService().isUncompressedTiff(new File(getClass().getClassLoader().getResource(TEST_FILE_TIFF_NOT_COMPRESSED).toURI())));
    }
    catch (JhoveException e) {
      e.printStackTrace();
    }
    catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }
}
