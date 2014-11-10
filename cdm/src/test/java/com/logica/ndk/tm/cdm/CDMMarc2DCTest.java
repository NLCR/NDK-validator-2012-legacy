package com.logica.ndk.tm.cdm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.xerces.util.DOMUtil;
import org.junit.AfterClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CDMMarc2DCTest {

  private static final Set<File> junkFiles = new HashSet<File>();

  @AfterClass
  public static void cleanUpAfterClass() {
    for (File f : junkFiles) {
      //FileUtils.deleteQuietly(f);
    }
  }

  private static File tmpFile(String sign) throws IOException {
    final File f = File.createTempFile("CDMMarc2DCTest-" + sign + "-", ".xml");
    junkFiles.add(f);
    return f;
  }

  @Test
  public void testTransformOaiMarcToMarc21() throws Exception {
    // zkopiruj aleph do temp suboru
    final InputStream aleph = new XMLHelper.Input("Aleph_Marc_Test.xml");
    final File tmpAleph = tmpFile("1-aleph");
    FileUtils.copyInputStreamToFile(aleph, tmpAleph);
    // transformace do mods documentu
    final Document dcDoc = CDMMarc2DC.transformAlephMarcToDC(tmpAleph);
    // validace
    //XMLHelper.validateXML(new XMLHelper.Input(mods2), new XMLHelper.Input("mods-3-4.xsd"));
    // 
//    final Element titleInfo = DOMUtil.getFirstChildElementNS(dcDoc.getDocumentElement(), "http://www.loc.gov/mods/v3", "title");
//    titleInfo.setAttribute("ID", "MODS_TITLE");
    final File dc3 = tmpFile("2-dc");
    XMLHelper.writeXML(dcDoc, dc3);
  }

}
