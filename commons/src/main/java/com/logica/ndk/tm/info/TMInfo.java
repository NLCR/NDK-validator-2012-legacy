package com.logica.ndk.tm.info;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

public class TMInfo {
  private static final Logger LOG = LoggerFactory.getLogger(TMInfo.class);
  private static Properties infoProperties;
  private static final String PROPERTY_FILE = "/tm-info.properties";
  private static final String BUILD_VERSION_PROPERTY = "tm.build.version";
  private static final String BUILD_TIMESTAMP_PROPERTY = "tm.build.timestamp";

  static {
    init();
  }

  public static void init() {
    infoProperties = new Properties();
    InputStream resourceAsStream = null;
    try {
      resourceAsStream = TMInfo.class.getResourceAsStream(PROPERTY_FILE);
      infoProperties.load(resourceAsStream);
    }
    catch (Exception e) {
      LOG.error("Could not load " + PROPERTY_FILE, e);
      throw new RuntimeException("Could not load " + PROPERTY_FILE, e);
    }
    finally {
      if (resourceAsStream != null) {
        try {
          resourceAsStream.close();
        }
        catch (IOException e) {
          LOG.error(String.format("Can't close InputStream of file: %s", PROPERTY_FILE), e);
        }
      }
    }

    StringWriter writer = new StringWriter();
    infoProperties.list(new PrintWriter(writer));
    LOG.info("TMInfo: \n{}\n", writer.toString());
  }

  public static String getBuildVersion() {
    return infoProperties.getProperty(BUILD_VERSION_PROPERTY);
  }

  public static String getBuildTimestamp() {
    return infoProperties.getProperty(BUILD_TIMESTAMP_PROPERTY);
  }
}
