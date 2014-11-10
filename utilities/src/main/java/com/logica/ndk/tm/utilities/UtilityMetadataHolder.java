package com.logica.ndk.tm.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ondrusekl
 */
public class UtilityMetadataHolder {

  private final transient Logger log = LoggerFactory.getLogger(getClass());

  private String version;

  private UtilityMetadataHolder() {
    final InputStream manifestStream = Thread.currentThread()
        .getContextClassLoader()
        .getResourceAsStream("META-INF/MANIFEST.MF");
    try {
      final Manifest manifest = new Manifest(manifestStream);
      final Attributes attributes = manifest.getMainAttributes();
      this.version = attributes.getValue(Name.IMPLEMENTATION_VERSION);
    }
    catch (final IOException ex) {
      log.warn("Error while reading version: " + ex.getMessage());
      this.version = "DEV";
    }
  }

  public String getVersion() {
    return version;
  }

  private static class SingletonHolder {
    public static final UtilityMetadataHolder instance = new UtilityMetadataHolder();
  }

  public static UtilityMetadataHolder getInstance() {
    return SingletonHolder.instance;
  }

  public static void main(final String[] args) {
    System.out.println(UtilityMetadataHolder.getInstance().getVersion());
  }

}
