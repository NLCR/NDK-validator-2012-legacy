package com.logica.ndk.tm.config;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.DefaultConfigurationBuilder;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

public class TmConfig {

  private static final Logger LOG = LoggerFactory.getLogger(TmConfig.class);
  private static DefaultConfigurationBuilder dcb;
  private final static String CLASSPATH_PREFIX = "classpath:";

  private static CombinedConfiguration config = null;
  static {
    try {
      dcb = new DefaultConfigurationBuilder("tm-config-config.xml");
      // disable warning for optional config files
      dcb.clearErrorListeners();
      //dcb.setReloadingStrategy(new FileChangedReloadingStrategy());
      
      config = dcb.getConfiguration(true);
      //config.setForceReloadCheck(true);
      LOG.debug("Initial configuration:\n{}\n", ConfigurationUtils.toString(config));
    }
    catch (final ConfigurationException ex) {
      throw new RuntimeException(ex);
    }
  }

  public static Configuration instance() {
    /*try {
      config = dcb.getConfiguration(true);
    }
    catch (final ConfigurationException ex) {
      throw new RuntimeException(ex);
    }*/
    return config;
    
  }

  /**
   * Get file from config.<br>
   * If prefix "classpath:" is used, then file will be search in classpath.
   * 
   * @param key
   *          config key
   * @return {@link File} object
   * @throws FileNotFoundException
   */
  @Nullable
  public static File getFile(final String key) throws FileNotFoundException {
    checkNotNull(key, "key must not be null");

    final String value = TmConfig.instance().getString(key);

    if (value.startsWith(CLASSPATH_PREFIX)) {
      final String filePath = value.replace(CLASSPATH_PREFIX, "");
      final URL url = Thread.currentThread().getContextClassLoader().getResource(filePath);
      if (url == null) {
        throw new FileNotFoundException(format("File %s not found on classpath", filePath));
      }
      return new File(url.getFile().replace("%20", " "));
    }
    else {
      return new File(value);
    }
  }

  /**
   * Get config as map
   * 
   * @param key
   *          key to map structure in config
   * @return {@link Map}
   */
  @Nullable
  public static Map<String, String> getMap(String key) {
    checkNotNull(key, "key must not be null");

    final Map<String, String> map = Maps.newHashMap();

    for (String configKey : Iterators.toArray(TmConfig.instance().getKeys(key), String.class)) {
      map.put(configKey, TmConfig.instance().getString(configKey));
    }

    return map;
  }
}
