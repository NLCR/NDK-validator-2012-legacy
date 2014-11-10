package com.logica.ndk.commons.shutdown;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.config.TmConfig;

/**
 * Check if shut down attribute is set or not. Shut down attribute expose if TM shut down is in progress.
 * 
 * @author Rudolf Daco
 */
public class ShutdownAttribute {
  private static final Logger LOG = LoggerFactory.getLogger(ShutdownAttribute.class);
  private static final String ATTRIBUTE_NAME = "shutdownAttribute.fileLocation";

  /**
   * @return true if shutdown attribute is set.
   */
  public static boolean isSet() {
    boolean result = false;
    String fileLocation = TmConfig.instance().getString(ATTRIBUTE_NAME, null);
    if (fileLocation != null) {
      File file = new File(fileLocation);
      result = file.exists();
    }
    return result;
  }

  /**
   * Throws ShutdownException if attribute is set to true.
   * 
   * @throws ShutdownException
   */
  public static void checkShutdownAttribute() throws ShutdownException {
    if (ShutdownAttribute.isSet() == true) {
      LOG.warn("Shutdown in progress. ShutdownAttribute is set to true!");
      throw new ShutdownException("Shutdown in progress. ShutdownAttribute is set to true!");
    }
  }
}
