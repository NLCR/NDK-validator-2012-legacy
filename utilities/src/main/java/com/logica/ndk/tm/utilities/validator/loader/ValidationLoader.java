/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.logica.ndk.tm.utilities.validator.loader;

import com.logica.ndk.tm.cdm.JAXBContextPool;
import com.logica.ndk.tm.utilities.validator.structures.ValidationTemplate;
import org.apache.commons.io.IOUtils;

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
public class ValidationLoader {

  public static <V> void save(String path, V object, Class<V> clazz) {
    try {

      File file = new File(path);
      if (!file.exists()) {
        try {
          file.createNewFile();
        }
        catch (IOException ex) {
          Logger.getLogger(ValidationLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
      }

      JAXBContext jaxbContext = JAXBContextPool.getContext(clazz);
      Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

      // output pretty printed
      jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

      jaxbMarshaller.marshal(object, file);
      //Only for test
      //jaxbMarshaller.marshal(object, System.out);

    }
    catch (JAXBException e) {
      e.printStackTrace();
    }

  }

  public static <V> V load(String path, Class<V> clazz) throws JAXBException, URISyntaxException, FileNotFoundException {
    InputStream stream = null;
    try {
      stream = new FileInputStream(new File(path));

      JAXBContext jaxbContext = JAXBContextPool.getContext(clazz);

      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      V object = (V) jaxbUnmarshaller.unmarshal(stream);
      //System.out.println(object);
      return object;
    }
    finally {
        IOUtils.closeQuietly(stream);
    }
  }

  public static ValidationTemplate load(String path) throws JAXBException, URISyntaxException, FileNotFoundException {
    return load(path, ValidationTemplate.class);
  }
}
