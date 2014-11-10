package com.logica.ndk.tm.utilities.urnnbn;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;

import com.google.common.collect.ImmutableMap;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.ResponseStatus;

import static java.lang.String.format;

/**
 * @author korvasm
 */
public class CreateDigitalInstanceImpl extends UrnNbnClient {

  private final String URL_TEMPLATE = TmConfig.instance().getString("utility.urnNbn.serverUrls.createDigitalInstance");

  public String execute(String uuid, String urnNbn, String profile, String accessibility, Boolean createInstance) {

    log.info("creating digital instance started for urn:nbn: " + urnNbn + ", profile: " + profile + ", accesibility: " + accessibility);

    // TODO Temporary import hack - will be removed once handled properly by WF
    if ("import".equals(urnNbn) || urnNbn == null) {
      log.debug("Ignoring for urnnbn " + urnNbn);
      return ResponseStatus.RESPONSE_OK;
    }

    if(!createInstance){
      log.info("Ignoring export process for " + profile + " not processed. Ending");
      return ResponseStatus.RESPONSE_OK;
    }
    
    final String realUrl = URL_TEMPLATE.replace("${urnNbn}", urnNbn);

    log.info("Going to prepare and send digital instance for profile: " + profile);

    String format = TmConfig.instance().getString(format("utility.urnNbn.k4.%s.format", profile));
    Integer digitalLibraryId = TmConfig.instance().getInt(format("utility.urnNbn.k4.%s.instance", profile));
    log.info("Digital library id = " + digitalLibraryId);

    String uuidForK4Url = getUuidForK4Url(uuid);
    log.info("uuid for k4URL: "+uuidForK4Url);
    String k4Url = TmConfig.instance().getString(format("utility.urnNbn.k4.%s.url", profile)).replace("${uuid}", uuidForK4Url);
    log.info("k4URL = " + k4Url);

    DigitalInstance digitalInstance = new DigitalInstance();
    digitalInstance.setUrl(k4Url);
    digitalInstance.setDigitalLibraryId(BigInteger.valueOf(digitalLibraryId.longValue()));
    digitalInstance.setFormat(format);
    digitalInstance.setAccessibility(accessibility);

    try {
      postDigitalInstance(digitalInstance, realUrl, uuid);
    }
    catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
//    sendAndReceive(digitalInstance, realUrl);

    log.info("creating digital instance finished");
    return ResponseStatus.RESPONSE_OK;
  }

  private String getUuidForK4Url(String cdmId)
  {
    if ("K4".equals(cdm.getCdmProperties(cdmId).getProperty("importType")))
    {
      Document doc = DocumentHelper.createDocument();
      org.dom4j.Document metsDocument = null;
      SAXReader reader = new SAXReader();
      try {
        metsDocument = reader.read(cdm.getMetsFile(cdmId));
      }
      catch (Exception e) {
        log.error("Problem with getting mets file", e);
        return cdmId;
      }
      XPath xPath = DocumentHelper.createXPath("//mods:mods");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
      Node node = xPath.selectSingleNode(metsDocument);
      doc.add((Node) node.clone());
      String documentType = null;
      try {
        documentType = new CDMMetsHelper().getDocumentType(cdmId);
      }
      catch (Exception e) {
        log.error("Problem with getting document type", e);
      }
      if (documentType == null)
      {
        log.error("Document type is null, returning cdmId");
        return cdmId;
      }
      if (documentType.equals("Periodical")) {
        String issue2 = "MODS_ISSUE_0001";
        String issue1 = "MODSMD_ISSUE_0001";
        xPath = DocumentHelper.createXPath("//mets:dmdSec[@ID='" + issue1 + "']/mets:mdWrap/mets:xmlData/mods:mods[@ID='" + issue2 + "']/mods:identifier[@type='uuid']");
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("mods", "http://www.loc.gov/mods/v3");
        namespaces.put("mets", "http://www.loc.gov/METS/");
        xPath.setNamespaceURIs(namespaces);
        node = xPath.selectSingleNode(metsDocument);
        if (node != null) {
          return node.getText();
        }
        else {
          log.error("UUID for digital instance in " + issue2 + " not found return cdmId");
        }
      }
      else {
        String MODS_ID_VOLUME = "MODS_VOLUME_0001";
        xPath = DocumentHelper.createXPath("//mods:mods[@ID='" + MODS_ID_VOLUME + "']/mods:identifier[@type='uuid']");
        xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
        node = xPath.selectSingleNode(doc);
        if (node != null) {
          return node.getText();
        }
        else {
          log.error("UUID for digital instance in " + MODS_ID_VOLUME + " not found return cdmId");        }
      }
    }    
    return cdmId;
  } 
  public static void main(String[] args) {
   // System.out.println((new CreateDigitalInstanceImpl().getUuidForK4Url("ad53aa50-3435-11e4-a25c-0050568209d4")));
   // System.out.println((new CreateDigitalInstanceImpl().getUuidForK4Url("7d924390-26d3-11e4-94c7-00505682629d")));
  }
}
