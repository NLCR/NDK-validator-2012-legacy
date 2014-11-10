package com.logica.ndk.tm.utilities;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Rudolf Daco
 *
 */
public class ErrorHelper {
  private static final Logger log = LoggerFactory.getLogger(ErrorHelper.class);
  
  private static final String BUNDLE_NAME = "errors";
  private static final String ERROR_MSG_PREFIX = "error.";
  private static final Locale DEFAULT_LOCALE = new Locale("cs", "CZ");
  
  public static Long getErrorCode(Throwable t) {
    if (t instanceof UtilityException) {
      UtilityException exUtil = (UtilityException) t;
      if (exUtil.getErrorCode() != null) {
        return exUtil.getErrorCode();
      }
    }
    return null;
  }

  public static String getStackTrace(Throwable e) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    e.printStackTrace(printWriter);
    String result = stringWriter.toString();
    if (stringWriter != null) {
      try {
        stringWriter.close();
      }
      catch (IOException e1) {
        log.error("Error at closing stringWriter.", e1);
        throw new SystemException("Error at closing stringWriter.", ErrorCodes.CLOSING_WRITER_FAILED);
      }
    }
    if (printWriter != null) {
      printWriter.close();
    }
    return result;
  }
  
  public static StringBuffer getStackTraceInBuffer(Throwable e) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    e.printStackTrace(printWriter);
    StringBuffer result = stringWriter.getBuffer();
    if (stringWriter != null) {
      try {
        stringWriter.close();
      }
      catch (IOException e1) {
        log.error("Error at closing stringWriter.", e1);
        throw new SystemException("Error at closing stringWriter.", e1);
      }
    }
    if (printWriter != null) {
      printWriter.close();
    }
    return result;
  }
  
  public static String getLocalizedMessage(Throwable t) {
    Long errorCode = getErrorCode(t);
    if (errorCode != null) {
      return getString(ERROR_MSG_PREFIX + errorCode.toString());
    }
    return null;
  }
  
  public static String getLocalizedMessage(Long errorCode) {
    if (errorCode != null) {
      return getString(ERROR_MSG_PREFIX + errorCode.toString());
    }
    return null;
  }
  
  public static String getString(String key) {
    return getString(DEFAULT_LOCALE, key);
  }
  
  public static String getString(Locale locale, String key) {
    try {
      return PropertyResourceBundle.getBundle(BUNDLE_NAME, locale).getString(key);
    } catch (MissingResourceException e) {
      return '!' + key + '!';
    }
  }

  public static String getString(Locale locale, String key, Object... params) {
    try {
      return MessageFormat.format(PropertyResourceBundle.getBundle(BUNDLE_NAME, locale).getString(key), params);
    } catch (MissingResourceException e) {
      return '!' + key + '!';
    }
  }

}
