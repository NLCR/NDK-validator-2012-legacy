/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.logica.ndk.tm.utilities.validator.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.logica.ndk.tm.cdm.JAXBContextPool;
import com.logica.ndk.tm.utilities.validator.structures.ValidationNode;

/**
 * @author brizat
 */
public class Loader {

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
    InputStream stream = new FileInputStream(new File(path));

    JAXBContext jaxbContext = JAXBContextPool.getContext(clazz);

    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    V object = (V) jaxbUnmarshaller.unmarshal(stream);
    //System.out.println(object);
    return object;
  }

  public static ValidationNode load(String path) throws JAXBException, URISyntaxException, FileNotFoundException {
    return load(path, ValidationNode.class);
  }
}
