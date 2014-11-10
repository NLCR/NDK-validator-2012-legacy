package com.logica.ndk.tm.utilities.io;

import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.file.GuessEncoding;

public class ValidateEncodingUTF8Impl extends AbstractUtility {
  
  public Boolean execute(String file) {
    log.info("ValidateEncodingUTF8 start for file: " + file);
    boolean valid = new GuessEncoding().isUTF8(file);
    log.info("ValidateEncodingUTF8 end for file: " + file);
    return valid;
  }
}
