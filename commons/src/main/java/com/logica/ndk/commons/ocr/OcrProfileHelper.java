/**
 * 
 */
package com.logica.ndk.commons.ocr;

import static java.lang.String.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.config.TmConfig;

/**
 * @author kovalcikm
 */
public class OcrProfileHelper {
  private static final String DEFAULT_OCR_PROFILE = TmConfig.instance().getString("process.ocr.defaultOcrProfile");

  protected final transient Logger log = LoggerFactory.getLogger(getClass());
  String ocr;
  String ocrFont;
  String language;

  public OcrProfileHelper(String ocr, String ocrFont, String language) {
    this.ocr = ocr;
    this.ocrFont = ocrFont;
    this.language = language;
  }

  public OcrProfileHelper() {
  }

  public String retrieveFromConfig(String value) {
    log.info("Resolving ocr parameter with path: " + format("process.ocr.%s.%s.%s.%s", ocr, ocrFont, language, value));
    String result = TmConfig.instance().getString(format("process.ocr.%s.%s.%s.%s", ocr, ocrFont, language, value));
    if (result == null) {
      log.info("Resolving ocr parameter with path: " + format("process.ocr.%s.%s.%s.%s", ocr, ocrFont, "default", value));
      result = TmConfig.instance().getString(format("process.ocr.%s.%s.%s.%s", ocr, ocrFont, "default", value));
    }
    if (result == null) {
      log.info("Resolving ocr parameter with path: " + format("process.ocr.%s.%s.%s.%s", ocr, "default", "default", value));
      result = TmConfig.instance().getString(format("process.ocr.%s.%s.%s.%s", ocr, "default", "default", value));
    }
    if (result == null) {
      log.info("Resolving ocr parameter with path: " + format("process.ocr.%s.%s.%s.%s", DEFAULT_OCR_PROFILE, "default", "default", value));
      result = TmConfig.instance().getString(format("process.ocr.%s.%s.%s.%s", DEFAULT_OCR_PROFILE, "default", "default", value));
    }
    log.info("Value '" + value + "' retrieved from config as: " + result);
    return result;
  }

  public String getAgentName() {
    return TmConfig.instance().getString("process.ocr." + ocr + ".agentName");
  }

  public String getAgentVersion() {
    return TmConfig.instance().getString("process.ocr." + ocr + ".agentVersion");
  }

  public String getOcr() {
    return ocr;
  }

  public void setOcr(String ocr) {
    this.ocr = ocr;
  }

  public String getOcrFont() {
    return ocrFont;
  }

  public void setOcrFont(String ocrFont) {
    this.ocrFont = ocrFont;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

}
