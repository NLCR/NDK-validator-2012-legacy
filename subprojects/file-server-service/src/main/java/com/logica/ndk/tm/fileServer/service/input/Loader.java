/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.logica.ndk.tm.fileServer.service.input;

import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author brizat
 */
public class Loader {

  private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Loader.class);

  public static <V> void save(String path, V object, Class<V> clazz) {
    try {

      File file = new File(path);
      if (!file.exists()) {
        try {
          file.createNewFile();
        }
        catch (IOException ex) {
          Logger.getLogger(Loader.class.getName()).log(Level.SEVERE, null, ex);
        }
      }

      JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
      Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

      // output pretty printed
      jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

      jaxbMarshaller.marshal(object, file);
      //Only for test
      jaxbMarshaller.marshal(object, System.out);

    }
    catch (JAXBException e) {
      e.printStackTrace();
    }

  }

  public static <V> V load(String path, Class<V> clazz) throws JAXBException, URISyntaxException, FileNotFoundException {
    InputStream stream = null;
    try {
      stream = new FileInputStream(new File(path));

      JAXBContext jaxbContext = JAXBContext.newInstance(clazz);

      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      V object = (V) jaxbUnmarshaller.unmarshal(stream);
      //System.out.println(object);
      return object;
    }
    finally {
        IOUtils.closeQuietly(stream);
    }
  }

  public static InputFile load(String path) throws JAXBException, URISyntaxException, FileNotFoundException {
    return load(path, InputFile.class);
  }
}
