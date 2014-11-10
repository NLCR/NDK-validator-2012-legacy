package com.logica.ndk.tm.utilities.transformation.em;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.dom4j.DocumentException;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.METSException;

import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.utilities.SystemException;

public class SplitByIntEntityImplIT {
  @Ignore
  public void test() throws SystemException, CDMException, IOException, DocumentException, SAXException, ParserConfigurationException, METSException  {
    String cdmId = "8fddce90-1c4b-11e3-917c-00505682629d";
    List<String> execute = new SplitByIntEntityImpl().execute(cdmId);
    for (String string : execute) {
      System.out.println(string);
    }
  }
  
  /*@Test
  public void testMatch() {
    String s = "TXT/1_1_testrenat_00009.tif.txt";
    String pageId = "1_1_testrenat_00009"; 
    String p = ".*" + pageId + "[\\.\\w]*";
    System.out.println("pattern: " + p);
    System.out.println(s.matches(p));
  }*/
}
