package com.logica.ndk.tm.utilities.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.mozilla.universalchardet.Constants;
import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Uhadne encoding pre subor. Encoding nie je mozne urcit presne posla obsahu, pretoze suboru mozu vyhovovat viacero
 * naraz. Nie kazdy encoding je jednoznacne identifikovatelny pretoze nie je zaznaceny v subore. UTF-8 with BOM nie je
 * problem identifikovat podla BOM - prve znaky v subore. UTF-8 without BOM je problem identifikovat, ale pouzite API
 * vie dobre odhadnut ze ide o UTF-8 na zaklade obsahu suboru. UTF-8 podla spesifikacie nema prikazane pouzivat BOM
 * dokonca sa nedoporucuje.
 * 
 * @author Rudolf Daco
 */
public class GuessEncoding {
  private static final Logger LOG = LoggerFactory.getLogger(GuessEncoding.class);
  private static final int BUFFER_LENGTH = 4096;

  public String getEncoding(String fileName) {
    File file = new File(fileName);
    if (file.exists() == false) {
      throw new IllegalArgumentException("File not exists: " + fileName);
    }
    if (file.isFile() == false) {
      throw new IllegalArgumentException("This is not file: " + fileName);
    }
    byte[] buf = new byte[BUFFER_LENGTH];
    String encoding = null;
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(fileName);
      UniversalDetector detector = new UniversalDetector(null);
      int nread;
      while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
        detector.handleData(buf, 0, nread);
      }
      detector.dataEnd();
      encoding = detector.getDetectedCharset();
      detector.reset();
    }
    catch (FileNotFoundException e) {
      LOG.error("Error during getEncoding.", e);
      throw new SystemException("Error during getEncoding.", ErrorCodes.GET_ENCODING_FAILED);
    }
    catch (IOException e) {
      LOG.error("Error during getEncoding.", e);
      throw new SystemException("Error during getEncoding.", ErrorCodes.GET_ENCODING_FAILED);
    } finally {
    	if (fis != null) {    	
    		IOUtils.closeQuietly(fis);
    	}
    }
    return encoding;
  }

  /**
   * Ma subor encoding UTF-8 ?
   * 
   * @param fileName
   * @return
   */
  public boolean isUTF8(String fileName) {
    String encoding = getEncoding(fileName);
    if (encoding != null && encoding.equals(Constants.CHARSET_UTF_8)) {
      return true;
    }
    return false;
  }
}
