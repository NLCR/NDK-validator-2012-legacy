package com.logica.ndk.tm.utilities.transformation.mets;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.transformation.mets.exception.ElementNotFoundException;
import com.logica.ndk.tm.utilities.transformation.mets.exception.METSPasrsingFailedException;

public class GetUUIDFromMetsImpl extends AbstractUtility {

  public String execute(String metsFilePath) throws METSPasrsingFailedException, ElementNotFoundException {
    checkNotNull(metsFilePath);
    log.info("Getting uuid from METS file: " + metsFilePath);
    
    File metsFile = new File(metsFilePath);
    return metsFile.getName().replace("METS_", "").replace(".xml", "");
    /* We dont use the inner UUID, rather use the package name
    File metsFile = new File(metsFilePath);
    SAXReader saxReader = new SAXReader();
    Document metsDoc;
    try {
      metsDoc = saxReader.read(metsFile);
      XPath xPath = DocumentHelper.createXPath("//mets:mets/mets:dmdSec[1]/mets:mdWrap/mets:xmlData/mods:mods/mods:identifier[@type='uuid']/text()");
      Map<String,String> namespaces = new HashMap<String,String>();
      namespaces.put("mods", "http://www.loc.gov/mods/v3");
      namespaces.put("mets", "http://www.loc.gov/METS/");
      xPath.setNamespaceURIs(namespaces);
      
      Node node = xPath.selectSingleNode(metsDoc);
      if (node == null) {
        log.warn("UUID missing in METS " + metsFilePath);
        throw new ElementNotFoundException("mods:identifier elemet of type uuid not found in the METS file.");
      }
      log.debug(node.toString());
      String uuid = node.getStringValue().replace("{", "").replace("}", "");
      log.info("uuid: " + uuid);
      return uuid;
    }
    catch (DocumentException e) {
      log.error(e.getMessage());
      throw new METSPasrsingFailedException("Unable to parse mets: " + e.getMessage());
    }
    */
  }
  
}
