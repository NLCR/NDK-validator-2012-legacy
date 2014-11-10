/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

import static java.lang.String.format;
import gov.loc.standards.premis.v2.ObjectFactory;
import gov.loc.standards.premis.v2.PremisComplexType;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.log4j.spi.ErrorCode;
import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.jhove.MixHelper;
import com.logica.ndk.tm.utilities.jhove.element.BasicDigitalObjectInformationType.ObjectIdentifier;
import com.logica.ndk.tm.utilities.premis.GeneratePremisImpl;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord;

/**
 * @author kovalcikm
 *         Obohati amdSec o agentov alebo eventy na zaklade novovytvorenych csv
 */
public class UpdateAmdSecImpl extends AbstractUtility {

  private static final String MC_CSV = CDMSchemaDir.MC_DIR.getDirName() + ".csv";
  private static final String ALTO_CSV = CDMSchemaDir.ALTO_DIR.getDirName() + ".csv";
  private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
  protected Namespace nsPremis = new Namespace("premis", "info:lc/xmlns/premis-v2");
  protected Namespace nsMets = new Namespace("mets", "http://www.loc.gov/METS/");
  protected Namespace nsMix = new Namespace("mix", "http://www.loc.gov/mix/v20");
  private final static String OBJ_ID_FORMAT = "OBJ_%03d";
  private final static String MIX_ID_FORMAT = "MIX_%03d";
  private final static String AGENT_ID_FORMAT = "AGENT_%03d";
  private final static String EVT_ID_FORMAT = "EVT_%03d";
  private final static String MC_EVENT_ID_FORMAT = "masterCopy_%03d";

  public String execute(String cdmId) {
    log.info("UpdateAmdSec started.");

    final PremisComplexType premis = new PremisComplexType();

    GeneratePremisImpl generatePremis = new GeneratePremisImpl();
    final File mcCsv = new File(cdm.getTransformationsDir(cdmId) + File.separator + MC_CSV);
    final File altoCsv = new File(cdm.getTransformationsDir(cdmId) + File.separator + ALTO_CSV);
    final Multimap<String, PremisCsvRecord> records = HashMultimap.<String, PremisCsvRecord> create();
    File premisFile = null;
    if (mcCsv.exists()) {
      log.info("MC.csv exists. Generating premis for MC");
      records.putAll(generatePremis.readFile(mcCsv, cdmId));
      for (final String key : records.keySet()) {
        premisFile = generatePremis.processPage(key, records.get(key), cdmId, mcCsv.getName());
        updateMC(cdmId, premisFile, records.get(key).toArray(new PremisCsvRecord[0])[0]);
      }
    }

    if (altoCsv.exists()) {
      log.info("ALTO.csv exists. Generating premis for ALTO");
      records.putAll(generatePremis.readFile(altoCsv, cdmId));
      for (final String key : records.keySet()) {
        premisFile = generatePremis.processPage(key, records.get(key), cdmId, mcCsv.getName());
        updateALTO(cdmId, premisFile, records.get(key).toArray(new PremisCsvRecord[0])[0]);
      }
    }
    return ResponseStatus.RESPONSE_OK;
  }

  private void updateMC(String cdmId, File premisFile, PremisCsvRecord record) {

    org.dom4j.Document premisDocument = null;
    org.dom4j.Document mixDocument = null;
    try {
      //premisDocument = DocumentHelper.parseText(FileUtils.readFileToString(premisFile));
      premisDocument = DocumentHelper.parseText(retriedReadFileToString(premisFile));
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    String nameId = FilenameUtils.getBaseName(FilenameUtils.getBaseName(record.getFile().getName()));
    nameId = nameId.substring(nameId.indexOf("_") + 1, nameId.length());
    File amdSecFile = null;
    //najdem prislusny amdSec
    final Collection<File> amdSecFiles = FileUtils.listFiles(cdm.getAmdDir(cdmId), FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter());
    for (File file : amdSecFiles) {
      if (file.getName().contains(nameId)) {
        amdSecFile = file;
        break;
      }
    }

    if (amdSecFile == null) {
      throw new SystemException("amdSec file not found for file:" + record.getFile(), ErrorCodes.RECORD_FILE_NOT_FOUND);
    }

    String[] mixExtension = { "mix" };
    File mixFile = null;
    //najdem prislusny mix
    final Collection<File> mixFiles = FileUtils.listFiles(new File(cdm.getMixDir(cdmId) + File.separator + cdm.getMasterCopyDir(cdmId).getName()), mixExtension, false);
    for (File file : mixFiles) {
      if (file.getName().contains(nameId)) {
        mixFile = file;
        break;
      }
    }

    SAXReader saxReader = new SAXReader();
    Document document = null;

    try {
      document = saxReader.read(amdSecFile);
//      //ziskanie OBJ_001
      XPath xPath = DocumentHelper.createXPath("//mets:techMD[@ID='OBJ_001']");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mets", "http://www.loc.gov/METS/"));
      Node nodeToRemoveObj002 = xPath.selectSingleNode(document);
//
//      //ziskanie stareho MIX_002
//      xPath = DocumentHelper.createXPath("//mets:techMD[@ID='MIX_002']");
//      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mets", "http://www.loc.gov/METS/"));
//      Node nodeToRemoveMix002 = xPath.selectSingleNode(document);
//
//      //ziskanie povodneho identifikatoru
//      xPath = DocumentHelper.createXPath("//mets:techMD[@ID='OBJ_002']/mets:mdWrap/mets:xmlData/premis:object/premis:objectIdentifier/premis:objectIdentifierValue");
//      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mets", "http://www.loc.gov/METS/", "premis", nsPremis.getStringValue()));
//      Node objIdentifierValue = xPath.selectSingleNode(document);
//      String oldObjIdentifier = null;
//      if (objIdentifierValue != null) {
//        oldObjIdentifier = objIdentifierValue.getText();
//      }

      Element parent = nodeToRemoveObj002.getParent();
//      parent.remove(nodeToRemoveObj002); //odobratie stareho OBJ_002
//      parent.remove(nodeToRemoveMix002);//odobratie stareho MIX_002

      @SuppressWarnings("unchecked")
      List<Branch> list = parent.content();

      xPath = premisDocument.createXPath("//premis:event");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("premis", nsPremis.getStringValue()));
      List<org.dom4j.Node> eventNodes = xPath.selectNodes(document);
      int eventCount = eventNodes.size();//pocet eventov v amdSec

      xPath = premisDocument.createXPath("//premis:eventIdentifierValue[starts-with(text(),'masterCopy_')]");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("premis", nsPremis.getStringValue()));
      List<org.dom4j.Node> eventMCNodes = xPath.selectNodes(document);
      int eventMCCount = eventMCNodes.size();//pocet eventov pre masterCopy v amdSec

      int newObjIndex = 0;
      int objCount = 0;
      int newMixIndex = 0;
      int mixCount = 0;
      int newEventIndex = 0;
      for (int i = 0; i < list.size(); i++) { //najdenie miesta(indexu) pre OBJ_002 a novy EVENT (agent ide na koniec)
        try {
          Element e = (Element) list.get(i);
          if (e.attributeValue("ID").startsWith("OBJ_")) {
            newObjIndex = i + 1;
            objCount++;
          }
          if (e.attributeValue("ID").startsWith("MIX_")) {
            newMixIndex = i + 1;
            mixCount++;
          }
          if (e.attributeValue("ID").startsWith("EVT_")) {
            eventCount--;
            if (eventCount == 0) {
              newEventIndex = i + 1;
            }
          }
        }
        catch (Exception e) {
          continue;
        }
      }

      //nastavenie identifierValue 

      xPath = premisDocument.createXPath("//premis:object/premis:objectIdentifier/premis:objectIdentifierValue");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("premis", nsPremis.getStringValue()));
      org.dom4j.Element objIdenValue = (org.dom4j.Element) xPath.selectSingleNode(premisDocument);
      objIdenValue.setText(record.getId());

      xPath = premisDocument.createXPath("//premis:relationship/premis:relatedObjectIdentification/premis:relatedObjectIdentifierValue");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("premis", nsPremis.getStringValue()));
      org.dom4j.Element objRelatedIdenValue = (org.dom4j.Element) xPath.selectSingleNode(premisDocument);
      if (objRelatedIdenValue != null) {
        objRelatedIdenValue.setText(record.getId().replace("MC", "PS"));
      }

      //do MIX_002
      if (mixFile == null) {
        throw new SystemException("MIX file not found for file:" + record.getFile(), ErrorCodes.RECORD_FILE_NOT_FOUND);
      }
      else {
        MixHelper mixHelper = new MixHelper(mixFile.getAbsolutePath());
        ObjectIdentifier objectIdentifier = mixHelper.getObjectInformation();
        mixHelper.setObjectInformation(objectIdentifier.getObjectIdentifierType().getValue(), record.getId());
        mixHelper.writeToFile(mixHelper.getMix(), mixFile.getAbsolutePath());
        try {
          //mixDocument = DocumentHelper.parseText(FileUtils.readFileToString(mixFile));
          mixDocument = DocumentHelper.parseText(retriedReadFileToString(mixFile));
        }
        catch (Exception e) {
          throw new SystemException("Error while MIX file to XML: " + mixFile.getAbsolutePath(), ErrorCodes.ERROR_WHILE_WRITING_FILE);
        }
      }

      xPath = mixDocument.createXPath("//ObjectIdentifier/objectIdentifierValue");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mix", nsMix.getStringValue()));
      org.dom4j.Element mixIdenValue = (org.dom4j.Element) xPath.selectSingleNode(premisDocument);
      if (mixIdenValue != null) {
        mixIdenValue.setText(record.getId());
      }

      //vytvorenie noveho MIX_002
      XMLHelper.qualify(mixDocument, nsMix);
      org.dom4j.Element mixTechMDElement = DocumentFactory.getInstance().createElement(new QName("techMD", nsMets));
      mixTechMDElement.addAttribute("ID", format(MIX_ID_FORMAT, ++mixCount));

      org.dom4j.Element mixMdWrapElement = mixTechMDElement.addElement(new QName("mdWrap", nsMets));
      mixMdWrapElement.addAttribute("MDTYPE", "NISOIMG");
      mixMdWrapElement.addAttribute("MIMETYPE", "text/xml");
      Namespace mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("");
      mixDocument.getRootElement().remove(mixNamespace);
      mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("mix");
      mixDocument.getRootElement().remove(mixNamespace);
      org.dom4j.Element mixXmlDataElement = mixMdWrapElement.addElement(new QName("xmlData", nsMets));
      mixXmlDataElement.add(mixDocument.getRootElement());

      //vytvorenie noveho OBJ_002 z premisu
      org.dom4j.Element techMd = DocumentFactory.getInstance().createElement(new QName("techMD", nsMets));
      org.dom4j.Element mdWrapElement = techMd.addElement(new QName("mdWrap", nsMets));
      mdWrapElement.addAttribute("MDTYPE", "PREMIS");
      mdWrapElement.addAttribute("MIMETYPE", "text/xml");

      XMLHelper.qualify(premisDocument, nsPremis);
      xPath = premisDocument.createXPath("//premis:object");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("premis", nsPremis.getStringValue()));
      org.dom4j.Element premisObject = (org.dom4j.Element) xPath.selectSingleNode(premisDocument);

      //nastavenie relatedEventIdentifierValue - podla eventIdentifierValue
      xPath = premisDocument.createXPath("//premis:relatedEventIdentifierValue");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("premis", nsPremis.getStringValue()));
      Node relatedEventIdentifierValue = xPath.selectSingleNode(premisObject);
      if (relatedEventIdentifierValue != null) {
        relatedEventIdentifierValue.setText(format(MC_EVENT_ID_FORMAT, eventMCCount + 1));
      }

      techMd.addAttribute("ID", format(OBJ_ID_FORMAT, ++objCount));
      org.dom4j.Element xmlDataElement = mdWrapElement.addElement(new QName("xmlData", nsMets));
      xmlDataElement.add(premisObject.createCopy());

      CDMMetsHelper helper = new CDMMetsHelper();
      list.add(newObjIndex, techMd); //vlozenie OBJ_
      list.add(newMixIndex, mixTechMDElement); //vlozenie MIX_

      //pridanie agenta (nakoniec)

      xPath = premisDocument.createXPath("//premis:agent");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("premis", nsPremis.getStringValue()));
      Node agentNode = xPath.selectSingleNode(premisDocument);

      xPath = premisDocument.createXPath("//premis:agent");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("premis", nsPremis.getStringValue()));
      List<org.dom4j.Node> agentNodes = xPath.selectNodes(document);

      org.dom4j.Element digiprovMDElementAgent = DocumentFactory.getInstance().createElement(new QName("digiprovMD", nsMets));
      digiprovMDElementAgent.addAttribute("ID", format(AGENT_ID_FORMAT, agentNodes.size() + 1));
      org.dom4j.Element mdWrapElementAgent = digiprovMDElementAgent.addElement(new QName("mdWrap", nsMets));
      mdWrapElementAgent.addAttribute("MDTYPE", "PREMIS");
      mdWrapElementAgent.addAttribute("MIMETYPE", "text/xml");
      org.dom4j.Element xmlDataElementAgent = mdWrapElementAgent.addElement(new QName("xmlData", nsMets));
      xmlDataElementAgent.add(((Element) agentNode).createCopy());
      list.add(digiprovMDElementAgent);

      //pridanie eventu
      xPath = premisDocument.createXPath("//premis:event");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("premis", nsPremis.getStringValue()));
      Node eventNode = xPath.selectSingleNode(premisDocument);

      //prenesenie povodneho identifikatora
      xPath = premisDocument.createXPath("//premis:linkingObjectIdentifierValue");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("premis", nsPremis.getStringValue()));
      Node eventLinkingIdenNode = xPath.selectSingleNode(eventNode);
      if (eventLinkingIdenNode != null) {
        eventLinkingIdenNode.setText(record.getId());
      }

      //nastavenie eventIdentifierValue
      xPath = premisDocument.createXPath("//premis:eventIdentifierValue");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("premis", nsPremis.getStringValue()));
      Node eventIdentifierValue = xPath.selectSingleNode(eventNode);
      if (eventIdentifierValue != null) {
        eventIdentifierValue.setText(format(MC_EVENT_ID_FORMAT, eventMCCount + 1));
      }

      org.dom4j.Element digiprovMDElementEvent = DocumentFactory.getInstance().createElement(new QName("digiprovMD", nsMets));
      digiprovMDElementEvent.addAttribute("ID", format(EVT_ID_FORMAT, eventNodes.size() + 1));
      org.dom4j.Element mdWrapElementEvent = digiprovMDElementEvent.addElement(new QName("mdWrap", nsMets));
      mdWrapElementEvent.addAttribute("MDTYPE", "PREMIS");
      mdWrapElementEvent.addAttribute("MIMETYPE", "text/xml");
      org.dom4j.Element xmlDataElementEvent = mdWrapElementEvent.addElement(new QName("xmlData", nsMets));
      xmlDataElementEvent.add(((Element) eventNode).createCopy());
      list.add(newEventIndex, digiprovMDElementEvent);

      parent.setContent(list);

      helper.writeToFile(document, amdSecFile);

    }
    catch (Exception e) {
      throw new SystemException("Error while udpating masterCopy record in amdSec file.", e, ErrorCodes.AMD_SEC_UPDATE_FAILED);
    }
  }

  private void updateALTO(String cdmId, File premisFile, PremisCsvRecord record) {

    org.dom4j.Document premisDocument = null;
    try {
      //premisDocument = DocumentHelper.parseText(FileUtils.readFileToString(premisFile));
      premisDocument = DocumentHelper.parseText(retriedReadFileToString(premisFile));
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    String nameId = FilenameUtils.getBaseName(FilenameUtils.getBaseName(record.getFile().getName()));
    File amdSecFile = null;
    //najdem prislusny amdSec
    final Collection<File> amdSecFiles = FileUtils.listFiles(cdm.getAmdDir(cdmId), FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter());
    for (File file : amdSecFiles) {
      if (file.getName().contains(nameId)) {
        amdSecFile = file;
        break;
      }
    }

    if (amdSecFile == null) {
      throw new SystemException("amdSec file not found for file:" + record.getFile(), ErrorCodes.RECORD_FILE_NOT_FOUND);
    }

    SAXReader saxReader = new SAXReader();
    //nahradenie OBJ_003
    try {
      Document document = saxReader.read(amdSecFile);
      XPath xPath = DocumentHelper.createXPath("//mets:techMD[@ID='OBJ_003']");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mets", "http://www.loc.gov/METS/"));
      Node nodeToRemove = xPath.selectSingleNode(document);
      Element parent = nodeToRemove.getParent();

      parent.remove(nodeToRemove); //odobratie stareho OBJ_003

      @SuppressWarnings("unchecked")
      List<Branch> list = parent.content();

      xPath = premisDocument.createXPath("//premis:event");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("premis", nsPremis.getStringValue()));
      List<org.dom4j.Node> eventNodes = xPath.selectNodes(document);
      int eventCount = eventNodes.size();//pocet eventov v amdSec
      int obj3Index = 0;
      int newEventIndex = 0;
      for (int i = 0; i < list.size(); i++) { //najdenie miesta(indexu) pre OBJ_003 a novy EVENT (agent ide na koniec)
        try {
          Element e = (Element) list.get(i);
          if (e.attributeValue("ID").equals("OBJ_002")) {
            obj3Index = i + 1;
          }
          if (e.attributeValue("ID").startsWith("EVT_")) {
            eventCount--;
            if (eventCount == 0) {
              newEventIndex = i + 1;
            }
          }
        }
        catch (Exception e) {
          continue;
        }
      }

      //vytvorenie noveho OBJ_ z premisu
      org.dom4j.Element techMd = DocumentFactory.getInstance().createElement(new QName("techMD", nsMets));
      org.dom4j.Element mdWrapElement = techMd.addElement(new QName("mdWrap", nsMets));
      mdWrapElement.addAttribute("MDTYPE", "PREMIS");
      mdWrapElement.addAttribute("MIMETYPE", "text/xml");
      XMLHelper.qualify(premisDocument, nsPremis);
      xPath = premisDocument.createXPath("//premis:object");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("premis", nsPremis.getStringValue()));
      org.dom4j.Element premisObject = (org.dom4j.Element) xPath.selectSingleNode(premisDocument);
      techMd.addAttribute("ID", format(OBJ_ID_FORMAT, 3));
      org.dom4j.Element xmlDataElement = mdWrapElement.addElement(new QName("xmlData", nsMets));
      xmlDataElement.add(premisObject.createCopy());

      CDMMetsHelper helper = new CDMMetsHelper();
      list.set(obj3Index, techMd); //vlozenie OBJ_

      //pridanie agenta (nakoniec)

      xPath = premisDocument.createXPath("//premis:agent");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("premis", nsPremis.getStringValue()));
      Node agentNode = xPath.selectSingleNode(premisDocument);

      xPath = premisDocument.createXPath("//premis:agent");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("premis", nsPremis.getStringValue()));
      List<org.dom4j.Node> agentNodes = xPath.selectNodes(document);

      org.dom4j.Element digiprovMDElementAgent = DocumentFactory.getInstance().createElement(new QName("digiprovMD", nsMets));
      digiprovMDElementAgent.addAttribute("ID", format(AGENT_ID_FORMAT, agentNodes.size() + 1));
      org.dom4j.Element mdWrapElementAgent = digiprovMDElementAgent.addElement(new QName("mdWrap", nsMets));
      mdWrapElementAgent.addAttribute("MDTYPE", "PREMIS");
      mdWrapElementAgent.addAttribute("MIMETYPE", "text/xml");
      org.dom4j.Element xmlDataElementAgent = mdWrapElementAgent.addElement(new QName("xmlData", nsMets));
      xmlDataElementAgent.add(((Element) agentNode).createCopy());
      list.add(digiprovMDElementAgent);

      //pridanie eventu
      xPath = premisDocument.createXPath("//premis:event");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("premis", nsPremis.getStringValue()));
      Node eventNode = xPath.selectSingleNode(premisDocument);

      org.dom4j.Element digiprovMDElementEvent = DocumentFactory.getInstance().createElement(new QName("digiprovMD", nsMets));
      digiprovMDElementEvent.addAttribute("ID", format(EVT_ID_FORMAT, eventNodes.size()));
      org.dom4j.Element mdWrapElementEvent = digiprovMDElementEvent.addElement(new QName("mdWrap", nsMets));
      mdWrapElementEvent.addAttribute("MDTYPE", "PREMIS");
      mdWrapElementEvent.addAttribute("MIMETYPE", "text/xml");
      org.dom4j.Element xmlDataElementEvent = mdWrapElementEvent.addElement(new QName("xmlData", nsMets));
      xmlDataElementEvent.add(((Element) eventNode).createCopy());
      list.add(newEventIndex, digiprovMDElementEvent);

      parent.setContent(list);

      helper.writeToFile(document, amdSecFile);

    }
    catch (Exception e) {
      throw new SystemException("Error while udpating ALTO record in amdSec file.", ErrorCodes.AMD_SEC_UPDATE_FAILED);
    }
  }
  
  @RetryOnFailure(attempts = 3)
  private String retriedReadFileToString(File file) throws IOException {
    return FileUtils.readFileToString(file, "UTF-8");
  }
}
