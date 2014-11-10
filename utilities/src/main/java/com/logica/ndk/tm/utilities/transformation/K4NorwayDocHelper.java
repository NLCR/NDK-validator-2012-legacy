package com.logica.ndk.tm.utilities.transformation;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.XMLHelper;

public class K4NorwayDocHelper {
  private static final String K3_FILE_SUFFIX = "k3";
  public static boolean isNorwayDoc(String cdmId, CDM cdm) {
    File rawDataDir = cdm.getRawDataDir(cdmId);
    File[] listFiles = rawDataDir.listFiles((FileFilter) FileFilterUtils.suffixFileFilter(K3_FILE_SUFFIX));
    if (listFiles == null || listFiles.length != 1) {
      throw new TransformationException("Incorrect number of K3 files in dir: " + rawDataDir.getAbsolutePath());
    }
    File k3File = listFiles[0];
    Document doc;
    try {
      doc = XMLHelper.parseXML(k3File, false);
      NodeList donator = doc.getElementsByTagName("CreatorSurname");
      for (int i = 0; i < donator.getLength(); i++) {
        if (donator.item(i).getTextContent().equals("***Donator NF***")) {
          return true;
        }
      }
    } catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ParserConfigurationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return false;
  }
}
