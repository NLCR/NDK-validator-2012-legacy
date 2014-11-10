/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.logica.ndk.jbpm.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.URISyntaxException;

/**
 * @author brizat
 */
public class ConfigLoader {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigLoader.class);
  public static String CONFIG_PATH = "c:\\NDK\\processRuntimeConfig.xml";
  private static long FILE_TIME_STAMP = 0l;
  private static ProcessRuntimeConfig config;

  public static ProcessRuntimeConfig loadConfig() throws LoadRuntimeConfigurationException {
    try {
      File configFile = new File(CONFIG_PATH);
      if (configFile.lastModified() > FILE_TIME_STAMP) {
        LOG.info("Configuration change, reloading!");
        config = load(CONFIG_PATH, ProcessRuntimeConfig.class);
        FILE_TIME_STAMP = configFile.lastModified();
      }
      return config;
    }
    catch (Exception ex) {
      throw new LoadRuntimeConfigurationException("Error while loading config file", ex);
    }
  }

  public static <V> V load(String path, Class<V> clazz) throws JAXBException, URISyntaxException, FileNotFoundException {
    InputStream stream = null;
    try {
      stream = new FileInputStream(new File(path));

      JAXBContext jaxbContext = JAXBContext.newInstance(clazz);

      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      V object = (V) jaxbUnmarshaller.unmarshal(stream);
      return object;
    }
    finally {
      try {
        if (stream != null)
        {
          stream.close();
        }
      }
      catch (IOException e) {
        LOG.error(String.format("Can't close FileInputStream of file: %s", path), e);
      }
    }
  }

  public static void saveConfig(ProcessRuntimeConfig config) throws SaveRuntimeConfigurationException {
    try {
      save(CONFIG_PATH, config, ProcessRuntimeConfig.class);
    }
    catch (Exception e) {
      LOG.error("Error while saving config file.", e);
      throw new SaveRuntimeConfigurationException("Error while saving config file.", e);
    }
  }

  public static <V> void save(String path, V object, Class<V> clazz) throws IOException, JAXBException {
    File file = new File(path);

    if (!file.exists()) {
      file.createNewFile();
    }

    JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

    // output pretty printed
    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

    jaxbMarshaller.marshal(object, file);
    //Only for test
    jaxbMarshaller.marshal(object, System.out);

  }
}
