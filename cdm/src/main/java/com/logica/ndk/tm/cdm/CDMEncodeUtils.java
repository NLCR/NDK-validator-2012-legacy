package com.logica.ndk.tm.cdm;

import java.io.UnsupportedEncodingException;

public class CDMEncodeUtils {

  public static String encodeForFilename(String name) {
    if (name == null) {
      return "null";
    }
    if (name.length() == 0) {
      return "empty";
    }
    try {
      return java.net.URLEncoder.encode(name, "UTF-8");
    }
    catch (UnsupportedEncodingException ex) {
      throw new RuntimeException(ex);
    }
  }

}
