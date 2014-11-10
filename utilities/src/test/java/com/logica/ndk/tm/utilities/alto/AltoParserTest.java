package com.logica.ndk.tm.utilities.alto;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Test;

public class AltoParserTest {

  @Test
  public void testParse03() throws JAXBException {
    AltoParser parser = new AltoParser();
    File f3 = new File("test-data/alto/xml/scan-0001.tif.xml");
    List<AltoWord> list = parser.parse(f3);
    assertTrue(list != null);
  }

  @Test
  public void testParse02() throws JAXBException {
    AltoParser parser = new AltoParser();
    File f02 = new File("test-data/alto/xml/scan-0001.tif.xml");
    List<AltoWord> list = parser.parse(f02);
    boolean b = false;
    for (AltoWord word : list) {
      if (word.getText().equals("making")) {
        b = true;
      }
    }
    assertTrue(b);
  }

}
