package com.logica.ndk.tm.utilities.premis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.csvreader.CsvWriter;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.jhove.MixHelper;
import com.logica.ndk.tm.utilities.transformation.em.EmConstants;

/**
 * @author brizat
 */
public class TransformAMDMetsToPremisImpl extends AbstractUtility {

  private static String MIX_XPATH = "mets:mets/mets:amdSec/mets:techMD[@ID='${MIX_TYPE}']/mets:mdWrap/mets:xmlData/mix:mix";
  private static String PREMIS_XPATH = "mets:mets/mets:amdSec/mets:techMD[@ID='${OBJ_TYPE}']/mets:mdWrap/mets:xmlData/premis:object";
  private static String PREMIS_OBJ001_EVENT_XPATH_ID = "mets:mets/mets:amdSec/mets:techMD[@ID='${OBJ_TYPE}']/mets:mdWrap/mets:xmlData/premis:object/premis:linkingEventIdentifier/premis:linkingEventIdentifierValue";
  private static String PREMIS_OBJ0023_EVENT_XPATH_ID = "mets:mets/mets:amdSec/mets:techMD[@ID='${OBJ_TYPE}']/mets:mdWrap/mets:xmlData/premis:object/premis:relationship/premis:relatedEventIdentification/premis:relatedEventIdentifierValue";
  private static String PREMIS_EVENT_XPATH = "mets:mets/mets:amdSec/mets:digiprovMD/mets:mdWrap/mets:xmlData/premis:event[premis:eventIdentifier/premis:eventIdentifierValue/text() = '${evetnIdentifier}']";
  private static String PREMIS_AGENT_XPATH_ID = "mets:mets/mets:amdSec/mets:digiprovMD/mets:mdWrap/mets:xmlData/premis:event[premis:eventIdentifier/premis:eventIdentifierValue/text() = '${evetnIdentifier}']/premis:linkingAgentIdentifier/premis:linkingAgentIdentifierValue";
  private static String PREMIS_AGENT_XPATH = "mets:mets/mets:amdSec/mets:digiprovMD/mets:mdWrap/mets:xmlData/premis:agent[premis:agentIdentifier/premis:agentIdentifierValue/text() = '${agentIdentifier}']";

  private CDM cdm = new CDM();
  private XPath xPath = XPathFactory.newInstance().newXPath();
  private DocumentBuilder documentBuilder;
  private HashMap<String, String> renamePrefixForAmdSecFile;
  
  private void init() {
    //init xPath setting (namespace atc.)
    xPath.setNamespaceContext(new NamespaceContext() {

      @Override
      public Iterator getPrefixes(String arg0) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public String getPrefix(String arg0) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public String getNamespaceURI(String arg0) {
        if (arg0.equalsIgnoreCase("mets")) {
          return "http://www.loc.gov/METS/";
        }
        if (arg0.equalsIgnoreCase("mix")) {
          return "http://www.loc.gov/mix/v20";
        }
        if (arg0.equals("premis")) {
          return "info:lc/xmlns/premis-v2";
        }
        return null;
      }
    });

    //init document builder
    try {
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      documentBuilderFactory.setNamespaceAware(false);
      documentBuilder = documentBuilderFactory.newDocumentBuilder();
    }
    catch (ParserConfigurationException e) {
      log.error("Error while init document builder: " , e);
      throw new SystemException(e);
    }
  }

  /**
   * @param cdmId
   * @return
   * @throws TransformerException
   */
  public String execute(String cdmId) {
    log.info("Utility TransformAMDMetsToPremisImpl for cdmId: " + cdmId);

    init();

    File amdMetsDir = cdm.getAmdDir(cdmId);
    if (!amdMetsDir.exists() || !amdMetsDir.isDirectory()) {
      log.error(String.format("Dir (%s)with AMD mets files does not exist", amdMetsDir.getAbsolutePath()));
      throw new BusinessException(String.format("Dir (%s)with AMD mets files does not exist", amdMetsDir.getAbsolutePath()), ErrorCodes.IMPORT_LTP_AMDMETS_DIR_NOT_EXIST);
    }

    //Check if number of images is same as number of admMets files.
    File[] amdMetsFiles = amdMetsDir.listFiles();
    int numberOfFilesInMC = cdm.getMasterCopyDir(cdmId).listFiles().length;
    if (amdMetsFiles.length != numberOfFilesInMC) {
      log.error(String.format("Wrong number of files in amdMetsDir (%d) and masterCopy dir (%d)", amdMetsFiles.length, numberOfFilesInMC));
      throw new BusinessException(String.format("Wrong number of files in amdMetsDir (%d) and masterCopy dir (%d)", amdMetsFiles.length, numberOfFilesInMC), ErrorCodes.IMPORT_LTP_WRONG_NUMBER_OF_FILES);
    }

    //Target mix files
    File mixODDir = new File(cdm.getWorkspaceDir(cdmId) + File.separator + "mix" + File.separator + CDMSchemaDir.ORIGINAL_DATA.getDirName());
    File mixFDDir = new File(cdm.getWorkspaceDir(cdmId) + File.separator + "mix" + File.separator + CDMSchemaDir.FLAT_DATA_DIR.getDirName());
    File mixPDDir = new File(cdm.getWorkspaceDir(cdmId) + File.separator + "mix" + File.separator + CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName());
    File mixMCDir = new File(cdm.getWorkspaceDir(cdmId) + File.separator + "mix" + File.separator + CDMSchemaDir.MC_DIR.getDirName());
    File premisFolder = cdm.getPremisDir(cdmId);

    //Prepare target folders
    if (!mixODDir.exists()) {
      mixODDir.mkdirs();
    }
    if (!mixFDDir.exists()) {
      mixFDDir.mkdirs();
    }
    if (!mixPDDir.exists()) {
      mixPDDir.mkdirs();
    }
    if (!mixMCDir.exists()) {
      mixMCDir.mkdirs();
    }
    if (!premisFolder.exists()) {
      premisFolder.mkdir();
    }

    //Create file which will hold origins of each image file.
    log.debug("Creating originsFile.csv started...");
    File fileOrigins = new File(cdm.getWorkspaceDir(cdmId) + File.separator + "fileOrigins.csv");
    CsvWriter csvWriter = null;
    try {
      csvWriter = new CsvWriter(new FileWriterWithEncoding(fileOrigins, "UTF-8", true), EmConstants.CSV_COLUMN_DELIMITER);
      csvWriter.setTextQualifier(EmConstants.CSV_TEXT_QUALIFIER);
      csvWriter.setForceQualifier(true);
      String[] HEADER = { "fileName","origin" };
      csvWriter.writeRecord(HEADER);
      String origin = "";
      String fileName = "";
      
      for (File amdMetsFile : amdMetsFiles) {
        log.info("Transform file: " + amdMetsFile.getAbsolutePath());
        Document amdMetsDoc;
        try {
          amdMetsDoc = XMLHelper.parseXML(amdMetsFile);
        }
        catch (Exception ex) {
          log.error(String.format("Exception while parsiong amdMets file (%s): ", amdMetsFile.getAbsolutePath()) + ex);
          throw new BusinessException(String.format("Exception (%s) while parsiong amdMets file (%s).", ex.getMessage(), amdMetsFile.getAbsolutePath()), ErrorCodes.IMPORT_LTP_PARSING_ADMMETS_FAILED);
        }
        
        //determine if there are more than three objects
        if(isMoreThanThreeObjects(amdMetsDoc)) {
          origin = "format-migration";
          //get mix files
          fileName = FilenameUtils.getBaseName(amdMetsFile.getName());
          String shortFileName = fileName.substring("AMD_METS_".length());
          getPrefixForxAmdSecFile(fileName, cdmId);
          fileName = getPrefixForxAmdSecFile(fileName, cdmId) + fileName.substring("AMD_METS_".length());         
          getMix(amdMetsDoc, "MIX_001", new File(mixODDir, shortFileName + ".tif.xml.mix"));        
          getMix(amdMetsDoc, "MIX_002", new File(mixFDDir, fileName + ".tif.xml.mix"));
          getMix(amdMetsDoc, "MIX_003", new File(mixPDDir, fileName + ".tif.xml.mix"));
          getMix(amdMetsDoc, "MIX_004", new File(mixMCDir, fileName + ".tif.jp2.xml.mix"));
          //get premis files
          transformObjsToPremiss(amdMetsDoc, "OBJ_001", new File(premisFolder, "PREMIS_originalData_" + fileName + ".xml"), PREMIS_OBJ001_EVENT_XPATH_ID);
          transformObjsToPremissFm(amdMetsDoc, "OBJ_002", new File(premisFolder, "PREMIS_flatData_" + fileName + ".xml"), PREMIS_OBJ001_EVENT_XPATH_ID, PREMIS_OBJ0023_EVENT_XPATH_ID);
          transformObjsToPremissFm(amdMetsDoc, "OBJ_003", new File(premisFolder, "PREMIS_postprocessingData_" + fileName + ".xml"), PREMIS_OBJ001_EVENT_XPATH_ID, PREMIS_OBJ0023_EVENT_XPATH_ID);
          transformObjsToPremiss(amdMetsDoc, "OBJ_004", new File(premisFolder, "PREMIS_masterCopy_" + fileName + ".xml"), PREMIS_OBJ0023_EVENT_XPATH_ID);
          transformObjsToPremiss(amdMetsDoc, "OBJ_005", new File(premisFolder, "PREMIS_ALTO_" + fileName + ".xml"), PREMIS_OBJ0023_EVENT_XPATH_ID);
        } 
        else {        
          origin = "scanner-device";
          //get mix files
          fileName = FilenameUtils.getBaseName(amdMetsFile.getName());          
          fileName = getPrefixForxAmdSecFile(fileName, cdmId) + fileName.substring("AMD_METS_".length());
          getMix(amdMetsDoc, "MIX_001", new File(mixPDDir, fileName + ".tif.xml.mix"));
          getMix(amdMetsDoc, "MIX_002", new File(mixMCDir, fileName + ".tif.jp2.xml.mix"));
          //get premis files
          transformObjsToPremiss(amdMetsDoc, "OBJ_001", new File(premisFolder, "PREMIS_postprocessingData_" + fileName + ".xml"), PREMIS_OBJ001_EVENT_XPATH_ID);
          transformObjsToPremiss(amdMetsDoc, "OBJ_002", new File(premisFolder, "PREMIS_masterCopy_" + fileName + ".xml"), PREMIS_OBJ0023_EVENT_XPATH_ID);
          transformObjsToPremiss(amdMetsDoc, "OBJ_003", new File(premisFolder, "PREMIS_ALTO_" + fileName + ".xml"), PREMIS_OBJ0023_EVENT_XPATH_ID);
        }
  
        String[] record = { fileName, origin };
        log.debug("Adding record: " + Arrays.deepToString(record));
        csvWriter.writeRecord(record);
      }
      }
    catch (IOException e) {
      log.error("Creating csv file error", e);
      throw new SystemException("Creating csv file error", e, ErrorCodes.CREATING_FILE_ERROR);
    } finally {
      csvWriter.close();
    }
    return ResponseStatus.RESPONSE_OK;
  }
  
  private  boolean isMoreThanThreeObjects(Document doc) {
    Node obj004 = getElementFromDoc(doc, PREMIS_XPATH.replace("${OBJ_TYPE}", "OBJ_004"));
    if (obj004 != null) {
      return true;
    }
    return false;
  }
  private String getPrefixForxAmdSecFile(String amdSecfileName,String cdmId)
  {
    if(renamePrefixForAmdSecFile==null)
    {
      renamePrefixForAmdSecFile=new HashMap<String, String>();
      File[] masterCopyFiles=cdm.getMasterCopyDir(cdmId).listFiles();
      for (File file : masterCopyFiles) {
        String fileName=file.getName();
        renamePrefixForAmdSecFile.put(fileName.substring(0,fileName.indexOf(".")).substring(fileName.indexOf("_")+1), fileName.substring(0,fileName.indexOf("_")+1));        
      }
    }   
    return renamePrefixForAmdSecFile.get(amdSecfileName.substring("AMD_METS_".length()));
  }
  private void getMix(Document document, String mixType, File targetFile) {
    try {
      Node mix = getElementFromDoc(document, MIX_XPATH.replace("${MIX_TYPE}", mixType));
      MixHelper mixHelper = new MixHelper(mix);
      mixHelper.writeToFile(mixHelper.getMix(), targetFile.getAbsolutePath());
    }
    catch (Exception e) {
      log.error(String.format("Error while writing mix file %s: ", targetFile.getAbsolutePath()) , e);
      throw new SystemException(String.format("Error while writing mix file %s", targetFile.getAbsolutePath()), e);
    }
  }

  private void transformObjsToPremiss(Document document, String objType, File targetFile, String eventIdXpath) {
    Document premisDoc = documentBuilder.newDocument();
    Element root = premisDoc.createElement("premis");
    root.setAttribute("xmlns", "info:lc/xmlns/premis-v2");
    root.setAttribute("xmlns:ns2", "http://www.w3.org/1999/xlink");
    root.setAttribute("xmlns:ns3", "http://www.loc.gov/mix/v20");
    root.setAttribute("version", "2.1");
    premisDoc.appendChild(root);
    List<Node> events = new ArrayList<Node>();
    List<Node> agents = new ArrayList<Node>();
    Element obj001 = (Element) getElementFromDoc(document, PREMIS_XPATH.replace("${OBJ_TYPE}", objType));
    obj001.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
    obj001.setAttribute("xsi:type", "file");
    List<Node> eventsIds = getElementsFromDoc(document, eventIdXpath.replace("${OBJ_TYPE}", objType));

    //resolve event and agents
    for (Node eventId : eventsIds) {
      String eventIdValue = eventId.getTextContent();
      Node eventNode = getElementFromDoc(document, PREMIS_EVENT_XPATH.replace("${evetnIdentifier}", eventIdValue));
      String agentIdValue = getElementFromDoc(document, PREMIS_AGENT_XPATH_ID.replace("${evetnIdentifier}", eventIdValue)).getTextContent();
      Node agentNode = getElementFromDoc(document, PREMIS_AGENT_XPATH.replace("${agentIdentifier}", agentIdValue));
      if (eventNode != null && agentNode != null) {
        events.add(eventNode);
        agents.add(agentNode);
      }
    }

    premisDoc.adoptNode(obj001);
    premisDoc.getDocumentElement().appendChild(obj001);
    for (Node event : events) {
      premisDoc.adoptNode(event);
      premisDoc.getDocumentElement().appendChild(event);
    }
    for (Node agent : agents) {
      Node newAgentNode = premisDoc.importNode(agent, true);
      premisDoc.getDocumentElement().appendChild(newAgentNode);
    }
    try {
      XMLHelper.writeXML(premisDoc, targetFile);
    }
    catch (Exception e) {
      log.error(String.format("Error while writing mets file %s: ", targetFile.getAbsolutePath()), e);
      throw new SystemException(String.format("Error while writing mets file %s", targetFile.getAbsolutePath()), e);
    }

  }
  
  private void transformObjsToPremissFm(Document document, String objType, File targetFile, String eventIdXpath, String eventIdXpath2) {
    Document premisDoc = documentBuilder.newDocument();
    Element root = premisDoc.createElement("premis");
    root.setAttribute("xmlns", "info:lc/xmlns/premis-v2");
    root.setAttribute("xmlns:ns2", "http://www.w3.org/1999/xlink");
    root.setAttribute("xmlns:ns3", "http://www.loc.gov/mix/v20");
    root.setAttribute("version", "2.1");
    premisDoc.appendChild(root);
    List<Node> events = new ArrayList<Node>();
    List<Node> agents = new ArrayList<Node>();
    Element obj001 = (Element) getElementFromDoc(document, PREMIS_XPATH.replace("${OBJ_TYPE}", objType));
    obj001.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
    obj001.setAttribute("xsi:type", "file");
    List<Node> eventsIds = getElementsFromDoc(document, eventIdXpath.replace("${OBJ_TYPE}", objType));
    eventsIds.addAll(getElementsFromDoc(document, eventIdXpath2.replace("${OBJ_TYPE}", objType)));

    //resolve event and agents
    for (Node eventId : eventsIds) {
      String eventIdValue = eventId.getTextContent();
      Node eventNode = getElementFromDoc(document, PREMIS_EVENT_XPATH.replace("${evetnIdentifier}", eventIdValue));
      String agentIdValue = getElementFromDoc(document, PREMIS_AGENT_XPATH_ID.replace("${evetnIdentifier}", eventIdValue)).getTextContent();
      Node agentNode = getElementFromDoc(document, PREMIS_AGENT_XPATH.replace("${agentIdentifier}", agentIdValue));
      if (eventNode != null && agentNode != null) {
        events.add(eventNode);
        agents.add(agentNode);
      }
    }

    premisDoc.adoptNode(obj001);
    premisDoc.getDocumentElement().appendChild(obj001);
    for (Node event : events) {
      premisDoc.adoptNode(event);
      premisDoc.getDocumentElement().appendChild(event);
    }
    for (Node agent : agents) {
      Node newAgentNode = premisDoc.importNode(agent, true);
      premisDoc.getDocumentElement().appendChild(newAgentNode);
    }
    try {
      XMLHelper.writeXML(premisDoc, targetFile);
    }
    catch (Exception e) {
      log.error(String.format("Error while writing mets file %s: ", targetFile.getAbsolutePath()), e);
      throw new SystemException(String.format("Error while writing mets file %s", targetFile.getAbsolutePath()), e);
    }

  }

  private Node getElementFromDoc(Document doc, String xPathString) {
    try {
      System.out.println(xPathString);
      XPathExpression xPathExpression = xPath.compile(xPathString);
      return (Node) xPathExpression.evaluate(doc, XPathConstants.NODE);
    }
    catch (XPathExpressionException e) {
      log.error("Error in xPath expression " , e);
      throw new SystemException("Error in xPath expression " + e.getMessage(), e);
    }
  }
  
  private List<Node> getElementsFromDoc(Document doc, String xPathString) {
    try {
      System.out.println(xPathString);
      XPathExpression xPathExpression = xPath.compile(xPathString);
      NodeList nodeSet = (NodeList) xPathExpression.evaluate(doc, XPathConstants.NODESET);
      List<Node> result = new ArrayList<Node>();
      for (int i = 0; i < nodeSet.getLength(); i++) {
        result.add((Node) nodeSet.item(i));
      }
      return result;
    }
    catch (XPathExpressionException e) {
      log.error("Error in xPath expression " , e);
      throw new SystemException("Error in xPath expression " + e.getMessage(), e);
    }
  }
  public static void main(String[] args) {
    new TransformAMDMetsToPremisImpl().execute("bdd61540-4248-11e4-8cd0-00505682629d");
  }

}
