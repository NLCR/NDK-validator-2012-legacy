/**
 *
 */
package com.logica.ndk.tm.utilities.transformation;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.xerces.util.DOMUtil;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.METSException;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.premis.GeneratePremisKrameriusImportImpl;
import com.logica.ndk.tm.utilities.transformation.em.CreateEmConfigImpl;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvHelper;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord.EmPageType;
import com.logica.ndk.tm.utilities.transformation.xsl.XSLTransformationImpl;

/**
 * @author Rudolf Daco
 */
public class CreateMetsK4Impl extends AbstractUtility {
  private static final String K3_FILE_SUFFIX = "k3";
  private static final String K4_DATA_XML_FOLDER = "xml";
  private static final String K4_DATA_XML_FILE_EXT = ".xml";
  private static final String MONOGRAPH_TYPE = "Monograph";
  private static final String PERIODICAL_TYPE = "Periodical";
  private final static int MAX_UNSPLITTED_SECTIONS = 3;

  public final static String ID_MODS_VOLUME = "MODS_VOLUME_0001";
  public final static String ID_MODS_TITLE = "MODS_TITLE_0001";
  public final static String ID_MODS_ISSUE = "MODS_ISSUE_0001";

  private static final String FOLDER_IMG_AMD = TmConfig.instance().getString("import.kramerius.djvuAmdFolder");
  private static final String FOLDER_JPEG_AMD = TmConfig.instance().getString("import.kramerius.jpegAmdFolder");

  private File mainFile;
  private File volumeFile;
  private File issueFile;

  private HashMap<String, String> nsMap;

  private String mainUuid;
  private String volumeUuid;
  private String issueUuid;

  public String execute(String cdmId) throws IOException {
    checkNotNull(cdmId, "cdmId must not be null");
    log.info("CreateMetsK4 started.");

    new GeneratePremisKrameriusImportImpl().execute(cdmId);
    CDM cdm = new CDM();
    CDMMetsHelper helper = new CDMMetsHelper();

    try {
      File metsFile = cdm.getMetsFile(cdmId);
      setUuidsFromK3File(cdm, cdmId);
      mainFile = getXMLFileByUUID(mainUuid, cdm, cdmId);

      Document modsTitleDoc = new XSLTransformationImpl().execute(mainFile.getAbsolutePath(), "xsl/foxml2mods.xsl");
      Element modsTitle = DOMUtil.getFirstChildElement(modsTitleDoc);

      List<Document> modsList = new ArrayList<Document>();

      if (getType().equals(MONOGRAPH_TYPE)) {
        modsTitle.setAttribute("ID", ID_MODS_VOLUME);
        modsList.add(modsTitleDoc);
      }
      else {
        volumeFile = getXMLFileByUUID(volumeUuid, cdm, cdmId);
        issueFile = getXMLFileByUUID(issueUuid, cdm, cdmId);

        if (volumeFile == null) {
          throw new SystemException("FOXML file for volume not found. Id:" + volumeUuid, ErrorCodes.FILE_NOT_FOUND);
        }
        if (issueFile == null) {
          throw new SystemException("FOXML file for issue not found. Id:" + issueUuid, ErrorCodes.FILE_NOT_FOUND);
        }
        Document modsVolumeDoc = new XSLTransformationImpl().execute(volumeFile.getAbsolutePath(), "xsl/foxml2mods.xsl");
        Element modsVolume = DOMUtil.getFirstChildElement(modsVolumeDoc);

        Document modsIssueDoc = new XSLTransformationImpl().execute(issueFile.getAbsolutePath(), "xsl/foxml2mods.xsl");
        Element modsIssue = DOMUtil.getFirstChildElement(modsIssueDoc);

        modsTitle.setAttribute("ID", ID_MODS_TITLE);
        modsList.add(modsTitleDoc);

        modsVolume.setAttribute("ID", ID_MODS_VOLUME);
        modsList.add(modsVolumeDoc);

        modsIssue.setAttribute("ID", ID_MODS_ISSUE);
        modsList.add(modsIssueDoc);
      }

      // print to console before update
      // printDocument(modsList.get(0), System.out);
      // update the main METS
      modsList = updateDataInMods(modsList, cdm, cdmId, getType());
      // print to console after update
      // printDocument(modsList.get(0), System.out);

      helper.createK4Mets(new FileOutputStream(metsFile), cdm, cdmId, modsList, getType());
      XMLHelper.pretyPrint(metsFile, true);

      // add amdSec fileGrp
      Collection<File> amdSecFiles = FileUtils.listFiles(cdm.getAmdDir(cdmId), FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter());
      int counter = 1;
      for (File f : amdSecFiles) {
        helper.addFileGroups(f, cdm, cdmId, counter);
        helper.addDummyStructMaps(f, cdm, cdmId);
        helper.prettyPrint(f);
        counter++;
      }

      // update main METS
      helper.removeFileSec(metsFile);
      helper.addFileGroups(metsFile, cdm, cdmId, 1);
      // update structMap
      helper.removeStructs(metsFile, null);
      // format
      helper.prettyPrint(metsFile);
      String type = getType();
      if (type != null && !type.isEmpty() && type.equals(MONOGRAPH_TYPE)) {
        helper.addIdentifier(cdmId, helper.IDENTIFIER_UUID, mainUuid);
        addSectionUUID(CDMMetsHelper.DMDSEC_ID_PREFIX_MODSMD_ISSUE, cdmId);
      }
      else
      {
        repairUuidInDc(metsFile);
      }

      XMLHelper.pretyPrint(metsFile, true);
      // helper.addDummyStructMaps(metsFile, cdm, cdmId);
      // add logical structMap

      CreateEmConfigImpl emConfigImpl = new CreateEmConfigImpl();
      if (cdm.getEmConfigFile(cdmId).exists()) {
        retriedDeleteFile(cdm.getEmConfigFile(cdmId));
      }
      emConfigImpl.create(cdmId, null);
      updateEM(cdmId);
      UpdateMetsFilesImpl updateMetsFilesImpl = new UpdateMetsFilesImpl();
      updateMetsFilesImpl.execute(cdmId);
    }
    catch (Exception e) {
      log.error("Error while creating mets.", e);
      throw new SystemException("Error while creating mets.", ErrorCodes.CREATING_METS_FAILED);
    }

    log.info("CreateMetsK4 finished.");
    return ResponseStatus.RESPONSE_OK;
  }

  private void repairUuidInDc(File metsFile) {
    CDMMetsHelper helper = new CDMMetsHelper();
    SAXReader reader = new SAXReader();
    org.dom4j.Document metsDocument = null;
    try {
      metsDocument = reader.read(metsFile);
      repairUuidInDcSection("MODSMD_VOLUME_0001", "DCMD_VOLUME_0001", helper, metsDocument);
      repairUuidInDcSection("MODSMD_TITLE_0001", "DCMD_TITLE_0001", helper, metsDocument);
      repairUuidInDcSection("MODSMD_ISSUE_0001", "DCMD_ISSUE_0001", helper, metsDocument);

      XMLWriter xmlWriter = new XMLWriter(new FileWriterWithEncoding(metsFile, "UTF-8"), OutputFormat.createPrettyPrint());
      xmlWriter.write("\ufeff");
      xmlWriter.write(metsDocument);
      xmlWriter.close();
    }
    catch (DocumentException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void repairUuidInDcSection(String modsID, String dcId, CDMMetsHelper helper, org.dom4j.Document metsDocument)
  {
    String xPathString = "mets:mets/mets:dmdSec[@ID='" + modsID + "']/mets:mdWrap/mets:xmlData/mods:mods/mods:identifier[@type='uuid']";
    Node nodeMods = helper.getNodeFromMets(xPathString, cdm, metsDocument);
    if (nodeMods != null) {
      String valueMods = nodeMods.getText();
      xPathString = "mets:mets/mets:dmdSec[@ID='" + dcId + "']/mets:mdWrap/mets:xmlData/oai_dc:dc/dc:identifier";
      List<Node> list = helper.getNodesFromMets(xPathString, cdm, metsDocument);
      if (list != null)
      {
        for (Node node : list) {
          if (node.getText().equals(valueMods)) {
            node.setText("uuid: " + valueMods);
            log.info(node.getText());
            return;
          }
        }
      }
    }
  }

  private void updateEM(String cdmId)
  {
    File emFile = cdm.getEmConfigFile(cdmId);
    List<EmCsvRecord> emRecords = EmCsvHelper.getRecords(EmCsvHelper.getCsvReader(emFile.getAbsolutePath()));
    HashMap<String, String[]> hashMap = new HashMap<String, String[]>();
    File xmlDir = new File(cdm.getRawDataDir(cdmId), K4_DATA_XML_FOLDER);
    File[] listFox = xmlDir.listFiles();
    for (int i = 0; i < listFox.length; i++) {
      String[] data = getNumberFromXml(listFox[i]);
      if (data != null)
      {
        hashMap.put(data[2], new String[] { data[0], data[1] }); //pageNumber, type
      }
    }
    HashMap<String, EmPageType> map = iniatializeMap();
    for (EmCsvRecord record : emRecords) {
      String[] pageNumberType = hashMap.get(record.getPageId());
      if (pageNumberType != null) {
        record.setPageOrderLabel(pageNumberType[0]);
        record.setPageType(map.get(pageNumberType[1]));
      }
    }
    try {
      EmCsvHelper.writeCsvFile(emRecords, cdmId, false, true);
    }
    catch (IOException e) {
      log.error("Error while creating file " + e.getMessage());
    }
  }

  private HashMap<String, EmPageType> iniatializeMap()
  {
    HashMap<String, EmPageType> map = new HashMap<String, EmCsvRecord.EmPageType>();
    map.put("NormalPage", EmPageType.normalPage);
    map.put("Spine", EmPageType.spine);
    map.put("Advertisement", EmPageType.advertisement);
    map.put("Cover", EmPageType.cover);
    map.put("Blank", EmPageType.blank);
    map.put("FrontCover", EmPageType.frontCover);
    map.put("FrontEndSheet", EmPageType.frontEndSheet);
    map.put("Index", EmPageType.index);
    map.put("ListOfIllustrations", EmPageType.listOfIllustrations);
    map.put("ListOfMaps", EmPageType.listOfMaps);
    map.put("ListOfTables", EmPageType.listOfTables);
    map.put("TableOfContents", EmPageType.tableOfContents);
    map.put("Table", EmPageType.table);
    map.put("TitlePage", EmPageType.titlePage);
    map.put("FlyLeaf", EmPageType.flyLeaf);
    map.put("BackCover", EmPageType.backCover);
    map.put("BackEndSheet", EmPageType.backEndSheet);
    map.put("CustomInclude", EmPageType.customInclude);
    map.put("ForDeletion", EmPageType.forDeletion);
    map.put("Jacket", EmPageType.jacket);
    map.put("FrontJacket", EmPageType.frontJacket);
    map.put("Map", EmPageType.map);
    return map;
  }

  private String[] getNumberFromXml(File xmlFile)
  {
    try {
      Document xml = XMLHelper.parseXML(xmlFile, false);
      NodeList list = xml.getElementsByTagName("foxml:contentLocation");
      for (int j = 0; j < list.getLength(); j++) {
        NamedNodeMap map = list.item(j).getAttributes();
        if (map != null)
        {
          for (int k = 0; k < map.getLength(); k++) {
            if (map.item(k).getLocalName().equals("REF"))
            {
              String text = map.item(k).getTextContent();
              if (text.startsWith("file"))
              {
                return getPageNumberTypeAndNumberFromXml(xml, text.substring(text.lastIndexOf('/') + 1, text.lastIndexOf('.')));
              }
            }
          }
        }
      }
    }
    catch (Exception e) {
      log.error("Error getting number from foxml:contentLocation", e);
    }
    return null;
  }

  private String[] getPageNumberTypeAndNumberFromXml(Document xml, String number)
  {
    NodeList list = xml.getElementsByTagName("mods:part");
    String type = null;
    String pageNumber = null;
    for (int j = 0; j < list.getLength(); j++) {
      if (type == null) {
        NamedNodeMap map = list.item(j).getAttributes();
        if (map != null)
        {
          for (int k = 0; k < map.getLength(); k++) {
            if (map.item(k).getLocalName().equals("type"))
            {
              type = map.item(k).getTextContent();
              break;
            }
          }
        }
      }
      if (pageNumber == null)
      {
        NodeList listChild = list.item(j).getChildNodes();
        for (int i = 0; i < listChild.getLength(); i++) {
          if (listChild.item(i).getLocalName() != null && listChild.item(i).getLocalName().equals("detail"))
          {
            NamedNodeMap map = listChild.item(i).getAttributes();
            if (map != null)
            {
              for (int k = 0; k < map.getLength(); k++) {
                if (map.item(k).getLocalName().equals("type") && map.item(k).getTextContent().equals("pageNumber"))
                {
                  NodeList listChildNumber = listChild.item(i).getChildNodes();
                  for (int l = 0; l < listChildNumber.getLength(); l++) {
                    if (listChildNumber.item(l).getLocalName() != null && listChildNumber.item(l).getLocalName().equals("number"))
                    {
                      pageNumber = listChildNumber.item(l).getTextContent();
                      break;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return new String[] { pageNumber, type, number };
  }

  /**
   * @param modsList
   * @param cdm
   * @param cdmId
   * @param type
   * @return
   */
  /**
   * @param modsList
   * @param cdm
   * @param cdmId
   * @param type
   * @return
   */
  private List<Document> updateDataInMods(List<Document> modsList, CDM cdm, String cdmId, String type) {
    log.debug("UpdateDataInMods started");
    if (type != null && !type.isEmpty() && type.equals(MONOGRAPH_TYPE)) {
      log.debug("Monograph");//***********************************************
      // MONOGRAPH - should have exactly one Document

      Document doc = modsList.get(0);
      Element mods = doc.getDocumentElement();

      // add genre element
      Element genre = doc.createElementNS("http://www.loc.gov/mods/v3", "genre");
      genre.setPrefix("mods");
      genre.setTextContent("volume");
      mods.appendChild(genre);

      // move issuance element
      Element originInfo1 = DOMUtil.getFirstChildElementNS(mods, "http://www.loc.gov/mods/v3", "originInfo");
      while (originInfo1.hasAttributes()) {
        originInfo1 = DOMUtil.getNextSiblingElementNS(originInfo1, "http://www.loc.gov/mods/v3", "originInfo");
      }
      Element originInfo2 = DOMUtil.getFirstChildElementNS(mods, "http://www.loc.gov/mods/v3", "originInfo");
      while (!originInfo2.hasAttributes()) {
        originInfo2 = DOMUtil.getNextSiblingElementNS(originInfo2, "http://www.loc.gov/mods/v3", "originInfo");
      }
      Element issuance = DOMUtil.getFirstChildElementNS(originInfo1, "http://www.loc.gov/mods/v3", "issuance");
      Element newIssuance = doc.createElementNS("http://www.loc.gov/mods/v3", "issuance");
      newIssuance.setPrefix("mods");
      newIssuance.setTextContent(issuance.getTextContent());
      originInfo2.appendChild(newIssuance);
      mods.removeChild(originInfo1);

      // adding recordInfo element
      File amdFile = null;
      File amdDir = new File(cdm.getRawDataDir(cdmId) + File.separator + FOLDER_JPEG_AMD);
      if (amdDir.exists() && amdDir.isDirectory()) {
        File[] files = amdDir.listFiles();
        if (files.length > 0) {
          amdFile = files[0];
        }
      }
      else {
        amdDir = new File(cdm.getRawDataDir(cdmId) + File.separator + FOLDER_IMG_AMD);
        if (amdDir.exists() && amdDir.isDirectory()) {
          File[] files = amdDir.listFiles();
          if (files.length > 0) {
            amdFile = files[0];
          }
        }
      }
      if (amdFile == null) {
        log.error("No image metadata file found in JPG/DJVU rawData folders");
        throw new SystemException("No image metadata file found in JPG/DJVU rawData folders");
      }
      SAXReader reader2 = new SAXReader();
      org.dom4j.Document amdDoc = null;
      try {
        amdDoc = reader2.read(amdFile);
      }
      catch (DocumentException e) {
        throw new SystemException("Error while reading XML file.", ErrorCodes.ERROR_WHILE_READING_FILE);
      }
      XPath xPath;
      xPath = DocumentHelper.createXPath("//*[local-name()='dateCreatedByApplication']");
      xPath.setNamespaceURIs(nsMap);
      Node node = xPath.selectSingleNode(amdDoc);
      String date = node.getText();

      Element recordInfo = doc.createElementNS("http://www.loc.gov/mods/v3", "recordInfo");
      recordInfo.setPrefix("mods");
      Element recordCreationDate = doc.createElementNS("http://www.loc.gov/mods/v3", "recordCreationDate");
      recordCreationDate.setPrefix("mods");
      recordCreationDate.setAttribute("encoding", "iso8601");
      recordCreationDate.setTextContent(date);
      recordInfo.appendChild(recordCreationDate);
      mods.appendChild(recordInfo);

      // remove empty identifiers
      NodeList identifiers = mods.getElementsByTagNameNS("http://www.loc.gov/mods/v3", "identifier");
      for (int i = 0; i < identifiers.getLength(); i++) {
        org.w3c.dom.Node n = identifiers.item(i);
        if (n.getTextContent() == null || n.getTextContent().isEmpty()) {
          mods.removeChild(n);
        }
      }

      // remove all calssification elements with attribute 'authority' and
      // value 'ddc'
      NodeList classifications = mods.getElementsByTagNameNS("http://www.loc.gov/mods/v3", "classification");
      for (int i = 0; i < classifications.getLength(); i++) {
        org.w3c.dom.Node n = classifications.item(i);
        Element e = (Element) n;
        if (e.hasAttribute("authority") && e.getAttribute("authority").equals("ddc")) {
          mods.removeChild(n);
        }
      }

      // restructure physicalDescription
      NodeList pDescs = mods.getElementsByTagNameNS("http://www.loc.gov/mods/v3", "physicalDescription");
      List<org.w3c.dom.Node> allChildren = new ArrayList<org.w3c.dom.Node>();
      for (int i = 0; i < pDescs.getLength(); i++) {
        org.w3c.dom.Node n = pDescs.item(i);
        NodeList nChildren = n.getChildNodes();
        for (int j = 0; j < nChildren.getLength(); j++) {
          allChildren.add(nChildren.item(j));
        }
      }

      while (pDescs.getLength() > 0) {
        org.w3c.dom.Node n = pDescs.item(0);
        mods.removeChild(n);
      }

      Element physDesc = doc.createElementNS("http://www.loc.gov/mods/v3", "physicalDescription");
      physDesc.setPrefix("mods");
      for (org.w3c.dom.Node x : allChildren) {
        physDesc.appendChild(x);
      }
      mods.appendChild(physDesc);
      removeElementWithAtribute("identifier", "http://www.loc.gov/mods/v3", mods, "type", "urn");
      updateSigla(mods);
      removeTransliterationPublisher(mods);
      List<Document> newList = new ArrayList<Document>();
      newList.add(doc);
      addToMetsNorway(newList);
      return newList;//***********************************************
    }
    else {
      log.debug("Periodical");
      // PERIODICAL - should have three Documents
      log.debug("Main uuid file name: " + mainUuid);
      log.debug("Volume uuid file name: " + volumeUuid);
      log.debug("Issue uuid file name: " + issueUuid);

      Document titleDoc = modsList.get(0);
      Element titleMods = titleDoc.getDocumentElement();

      Document volumeDoc = modsList.get(1);
      Element volumeMods = volumeDoc.getDocumentElement();

      Document issueDoc = modsList.get(2);
      Element issueMods = issueDoc.getDocumentElement();

      // add genre elements to title/volume/issue
      Element titleGenre = titleDoc.createElementNS("http://www.loc.gov/mods/v3", "genre");
      titleGenre.setPrefix("mods");
      titleGenre.setTextContent("title");
      titleMods.appendChild(titleGenre);
      Element volumeGenre = volumeDoc.createElementNS("http://www.loc.gov/mods/v3", "genre");
      volumeGenre.setPrefix("mods");
      volumeGenre.setTextContent("volume");
      volumeMods.appendChild(volumeGenre);
      Element issueGenre = issueDoc.createElementNS("http://www.loc.gov/mods/v3", "genre");
      issueGenre.setPrefix("mods");
      issueGenre.setAttribute("type", "normal");
      issueGenre.setTextContent("issue");
      issueMods.appendChild(issueGenre);

      // add titleInfo to volume from title
      Element titleInfoFromVolume = DOMUtil.getFirstChildElementNS(volumeMods, "http://www.loc.gov/mods/v3", "titleInfo");
      if (titleInfoFromVolume != null) {
        titleInfoFromVolume.getParentNode().removeChild(titleInfoFromVolume);
      }
      Element titleInfoFromTitle = DOMUtil.getFirstChildElementNS(titleMods, "http://www.loc.gov/mods/v3", "titleInfo");
      Element newTitleInfo = (Element) titleInfoFromTitle.cloneNode(true);
      volumeDoc.adoptNode(newTitleInfo);
      volumeMods.appendChild(newTitleInfo);

      // add dateIssued to volume and issue from title **
      // no longer - ATRD-32
      NodeList childNodes = titleMods.getChildNodes();
//      for (int i = 0; i < childNodes.getLength(); i++) {
//        Element dateIssuedFromTitle = DOMUtil.getFirstChildElementNS(childNodes.item(i), "http://www.loc.gov/mods/v3", "dateIssued");
//        if (dateIssuedFromTitle != null) {
//          Element newDate = (Element) dateIssuedFromTitle.cloneNode(true);
//          String dateString = newDate.getTextContent();
//          if (dateString.indexOf(' ') != -1) {
//            dateString = dateString.substring(0, dateString.indexOf(' '));
//            newDate.setTextContent(dateString);
//          }
//          volumeDoc.adoptNode(newDate);
//          Element originInfoVolume = DOMUtil.getFirstChildElementNS(volumeMods, "http://www.loc.gov/mods/v3", "originInfo");
//          if (originInfoVolume != null) {
//            originInfoVolume.appendChild(newDate);
//          }
//          else {
//            volumeMods.appendChild(newDate);
//          }
//          break;
//        }
//      }

      // add language to issue and volume from title
      Element languageFromTitle = DOMUtil.getFirstChildElementNS(titleMods, "http://www.loc.gov/mods/v3", "language");
      Element newLanguage = (Element) languageFromTitle.cloneNode(true);
      issueDoc.adoptNode(newLanguage);
      issueMods.appendChild(newLanguage);

      Element newLanguage2 = (Element) languageFromTitle.cloneNode(true);
      volumeDoc.adoptNode(newLanguage2);
      volumeMods.appendChild(newLanguage2);

      // add publisher to volume from title
      for (int i = 0; i < childNodes.getLength(); i++) {
        Element publisherFromTitle = DOMUtil.getFirstChildElementNS(childNodes.item(i), "http://www.loc.gov/mods/v3", "publisher");
        if (publisherFromTitle != null) {
          Element newPublisher = (Element) publisherFromTitle.cloneNode(true);
          volumeDoc.adoptNode(newPublisher);
          volumeMods.appendChild(newPublisher);
          break;
        }
      }

      // add note to volume from title
      Element noteFromTitle = DOMUtil.getFirstChildElementNS(titleMods, "http://www.loc.gov/mods/v3", "note");
      Element newNote = (Element) noteFromTitle.cloneNode(true);
      volumeDoc.adoptNode(newNote);
      volumeMods.appendChild(newNote);

      // join originInfo for issuance, dateIssued title **
      Element originInfoFirst = DOMUtil.getFirstChildElementNS(titleMods, "http://www.loc.gov/mods/v3", "originInfo");
      Element originInfoLast = DOMUtil.getLastChildElementNS(titleMods, "http://www.loc.gov/mods/v3", "originInfo");
      while (originInfoFirst != originInfoLast) {
        while (originInfoLast.hasChildNodes()) {
          originInfoFirst.appendChild(originInfoLast.getFirstChild());
        }
        originInfoLast.getParentNode().removeChild(originInfoLast);
        originInfoFirst = DOMUtil.getFirstChildElementNS(titleMods, "http://www.loc.gov/mods/v3", "originInfo");
        originInfoLast = DOMUtil.getLastChildElementNS(titleMods, "http://www.loc.gov/mods/v3", "originInfo");
      }

      // add date to title from issue
      Element date = DOMUtil.getFirstChildElementNS(issueMods, "http://www.loc.gov/mods/v3", "part");
      NodeList dateChild = date.getChildNodes();
      for (int i = 0; i < dateChild.getLength(); i++) {
        if (dateChild.item(i).getNodeName().equals(dateChild.item(i).getPrefix() + ":date")) {
          Element dateFromPart = (Element) dateChild.item(i);
          if (dateFromPart != null) {
            Element newDate = (Element) dateFromPart.cloneNode(true);
            titleDoc.adoptNode(newDate);
            titleMods.appendChild(newDate);
            break;
          }
        }
      }
      // create partNumber from number volume titleInfo/partNumber
      NodeList volumeNumber = volumeMods.getElementsByTagNameNS("http://www.loc.gov/mods/v3", "number");
      if (volumeNumber.getLength() > 0) {
        Element titleInfoVolume = DOMUtil.getFirstChildElementNS(volumeMods, "http://www.loc.gov/mods/v3", "titleInfo");
        Element newPart = (Element) volumeNumber.item(0).cloneNode(true);
        volumeDoc.adoptNode(newPart);
        titleInfoVolume.appendChild(newPart);

        NodeList nodes = volumeMods.getElementsByTagNameNS("http://www.loc.gov/mods/v3", "number");
        for (int i = 0; i < nodes.getLength(); i++) {
          if (nodes.item(i).getParentNode().equals(titleInfoVolume)) {
            volumeDoc.renameNode(nodes.item(i), "http://www.loc.gov/mods/v3", "partNumber");
            nodes.item(i).setPrefix("mods");
            break;
          }
        }
      }

      // rename urn to uuid
      renameUrnToUuid(volumeMods);
      renameUrnToUuid(titleMods);
      renameUrnToUuid(issueMods);
      // add recordInfo-recordCreationDate to title
      Element recordInfo = titleDoc.createElementNS("http://www.loc.gov/mods/v3", "recordInfo");
      recordInfo.setPrefix("mods");
      Element recordCreationDate = titleDoc.createElementNS("http://www.loc.gov/mods/v3", "recordCreationDate");
      recordCreationDate.setPrefix("mods");
      DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
      Date today = Calendar.getInstance().getTime();
      String formatedDate = df.format(today);
      recordCreationDate.setTextContent(formatedDate);
      recordCreationDate.setAttribute("encoding", "iso8601");
      recordInfo.appendChild(recordCreationDate);
      titleMods.appendChild(recordInfo);
      updateSigla(titleMods);
      removeTransliterationPublisher(titleMods);
      removeTransliterationPublisher(volumeMods);
      removeTransliterationPublisher(issueMods);
      // create partNumber from number issue titleInfo/partNumber
      NodeList issueNumber = issueMods.getElementsByTagNameNS("http://www.loc.gov/mods/v3", "number");
      if (issueNumber.getLength() > 0) {
        Element titleInfoIssue = DOMUtil.getFirstChildElementNS(issueMods, "http://www.loc.gov/mods/v3", "titleInfo");
        Element newPart = (Element) issueNumber.item(0).cloneNode(true);
        issueDoc.adoptNode(newPart);
        titleInfoIssue.appendChild(newPart);

        NodeList nodes = issueMods.getElementsByTagNameNS("http://www.loc.gov/mods/v3", "number");
        for (int i = 0; i < nodes.getLength(); i++) {
          if (nodes.item(i).getParentNode().equals(titleInfoIssue)) {
            issueDoc.renameNode(nodes.item(i), "http://www.loc.gov/mods/v3", "partNumber");
            nodes.item(i).setPrefix("mods");
            issueNumber.item(0).getParentNode().removeChild(issueNumber.item(0));
            break;
          }
        }
      }

      // create dateIssue from date Issue and remove issuance **
//      NodeList issueDate = issueMods.getElementsByTagNameNS("http://www.loc.gov/mods/v3", "date");
//      if (issueDate.getLength() > 0) {
//        Element originInfoIssue = DOMUtil.getFirstChildElementNS(issueMods, "http://www.loc.gov/mods/v3", "originInfo");
//        Element newDate = (Element) issueDate.item(0).cloneNode(true);
//        issueDoc.adoptNode(newDate);
//        originInfoIssue.appendChild(newDate);
//
//        NodeList nodes = issueMods.getElementsByTagNameNS("http://www.loc.gov/mods/v3", "date");
//        for (int i = 0; i < nodes.getLength(); i++) {
//          if (nodes.item(i).getParentNode().equals(originInfoIssue)) {
//            issueDoc.renameNode(nodes.item(i), "http://www.loc.gov/mods/v3", "dateIssued");
//            nodes.item(i).setPrefix("mods");
//            issueDate.item(0).getParentNode().removeChild(issueDate.item(0));
//            break;
//          }
//        }
//      }

      //add physicalDescription /form/authority to title if missing
      addphysicalDescription(titleMods, titleDoc);
      //remove issuance from ISSUE/originInfo
      try {
        Element originInfoIssue = DOMUtil.getFirstChildElementNS(issueMods, "http://www.loc.gov/mods/v3", "originInfo");
        Element issuanceIssue = DOMUtil.getFirstChildElementNS(originInfoIssue, "http://www.loc.gov/mods/v3", "issuance");
        issuanceIssue.getParentNode().removeChild(issuanceIssue);
      }
      catch (Exception e) {
        log.error("Problem with remove issuance: ", e);
      }
      //get date IssueDate from date  
      getDateIssueFromDate(issueMods, issueDoc);
      getDateIssueFromDate(titleMods, titleDoc);
      getDateIssueFromDate(volumeMods, volumeDoc);
      // remove date
      removeElement("date", "http://www.loc.gov/mods/v3", issueMods);
      removeElement("date", "http://www.loc.gov/mods/v3", titleMods);
      removeElement("date", "http://www.loc.gov/mods/v3", volumeMods);

      // remove all calssification elements with attribute 'authority' and value 'ddc'   
      removeClassificationDdc(titleMods);
      addToMetsNorway(modsList);
      //  System.exit(0);
      return modsList;
    }

  }

  private void getDateIssueFromDate(Element mods, Document document)
  {
    NodeList dateIssuedList = mods.getElementsByTagNameNS("http://www.loc.gov/mods/v3", "dateIssued");
    if (dateIssuedList.getLength() == 0) {
      NodeList issueDate = mods.getElementsByTagNameNS("http://www.loc.gov/mods/v3", "date");
      if (issueDate.getLength() > 0) {
        Element originInfoIssue = DOMUtil.getFirstChildElementNS(mods, "http://www.loc.gov/mods/v3", "originInfo");
        Element newDate = (Element) issueDate.item(0).cloneNode(true);
        document.adoptNode(newDate);
        originInfoIssue.appendChild(newDate);

        NodeList nodes = mods.getElementsByTagNameNS("http://www.loc.gov/mods/v3", "date");
        for (int i = 0; i < nodes.getLength(); i++) {
          if (nodes.item(i).getParentNode().equals(originInfoIssue)) {
            document.renameNode(nodes.item(i), "http://www.loc.gov/mods/v3", "dateIssued");
            nodes.item(i).setPrefix("mods");
            issueDate.item(0).getParentNode().removeChild(issueDate.item(0));
            break;
          }
        }
      }
    }
  }

  private void addphysicalDescription(Element titleMods, Document titleDoc)
  {
    NodeList formList = titleMods.getElementsByTagNameNS("http://www.loc.gov/mods/v3", "form");
    if (formList.getLength() > 0) {
      for (int i = 0; i < formList.getLength(); i++) {//authority="marcform"
        if (formList.item(i).getParentNode().getNodeName().equals("mods:physicalDescription") && formList.item(i).hasAttributes() &&
            formList.item(i).getAttributes().getNamedItem("authority") != null && "marcform".equals(formList.item(i).getAttributes().getNamedItem("authority").getTextContent()))
        {
          return;
        }
      }
    }

    NodeList physicalDescriptionList = titleMods.getElementsByTagNameNS("http://www.loc.gov/mods/v3", "physicalDescription");
    if (physicalDescriptionList.getLength() > 0)
    {
      Element form = titleDoc.createElementNS("http://www.loc.gov/mods/v3", "form");
      form.setPrefix("mods");
      form.setTextContent("print");
      form.setAttribute("authority", "marcform");
      physicalDescriptionList.item(0).appendChild(form);

    }
    else
    {
      Element physicalDescription = titleDoc.createElementNS("http://www.loc.gov/mods/v3", "physicalDescription");
      physicalDescription.setPrefix("mods");
      Element form = titleDoc.createElementNS("http://www.loc.gov/mods/v3", "form");
      form.setPrefix("mods");
      form.setTextContent("print");
      form.setAttribute("authority", "marcform");
      physicalDescription.appendChild(form);
      titleMods.appendChild(physicalDescription);
    }

  }

  private void removeClassificationDdc(Element mods)
  {
//remove all calssification elements with attribute 'authority' and
    // value 'ddc'
    NodeList classifications = mods.getElementsByTagNameNS("http://www.loc.gov/mods/v3", "classification");
    for (int i = 0; i < classifications.getLength(); i++) {
      org.w3c.dom.Node n = classifications.item(i);
      Element e = (Element) n;
      if (e.hasAttribute("authority") && e.getAttribute("authority").equals("ddc")) {
        mods.removeChild(n);
      }
    }
  }

  private void removeTransliterationPublisher(Element mods)
  {
    NodeList list = mods.getElementsByTagNameNS("http://www.loc.gov/mods/v3", "originInfo");
    if (list != null)
    {
      for (int i = 0; i < list.getLength(); i++) {
        if (list.item(i).hasAttributes())
        {
          NamedNodeMap attributes = list.item(i).getAttributes();
          if (attributes != null) {
            for (int j = 0; j < attributes.getLength(); j++) {
              if ("transliteration".equals(attributes.item(j).getLocalName()) && "publisher".equals(attributes.item(j).getTextContent()))
              {
                Element element = (Element) list.item(i);
                element.removeAttribute("transliteration");
                log.info("transliteration removed");
                break;
              }
            }
          }
        }
      }
    }
  }

  private void updateSigla(Element mods) {
    NodeList list = mods.getElementsByTagNameNS("http://www.loc.gov/mods/v3", "physicalLocation");
    if (list != null)
    {
      for (int i = 0; i < list.getLength(); i++) {
        if (list.item(i).hasAttributes())
        {
          NamedNodeMap attributes = list.item(i).getAttributes();
          for (int j = 0; j < attributes.getLength(); j++) {
            if (attributes.item(i).getLocalName().equals("authority"))
              return;
          }
          Element element = (Element) list.item(i);
          element.setAttribute("authority", "siglaADR");
        }
        else {
          Element element = (Element) list.item(i);
          element.setAttribute("authority", "siglaADR");
        }

      }
    }
  }

  private void addToMetsNorway(List<Document> list) {
    File file = list.size() == 3 ? volumeFile : mainFile; //monograph=main, periodical=volume
    Document doc = loadDom(file);
    NodeList identifierList = doc.getElementsByTagName("dc:identifier");
    if (identifierList != null && identifierList.getLength() > 0) {
      String handle = null;
      String contract = null;
      for (int i = 0; i < identifierList.getLength(); i++) {
        String textContext = identifierList.item(i).getTextContent();
        if (textContext.startsWith("handle")) {
          handle = textContext;
          continue;
        }
        if (textContext.startsWith("contract")) {
          contract = textContext;
        }
      }
      if (handle != null && contract != null) {
        if (handle.indexOf(':') != -1)
          handle = handle.substring(handle.indexOf(':') + 1);
        if (contract.indexOf(':') != -1)
          contract = contract.substring(contract.indexOf(':') + 1);
        log.info("handle: " + handle + " contract: " + contract);
        if (list.size() == 1) {
          log.debug("Adding hangle and contract to monograf");
          addHandleAndContractToDocument(list.get(0), handle, contract);
        }
        else {
          log.debug("Adding hangle and contract to periodical");
          addHandleAndContractToDocument(list.get(1), handle, contract);
          addHandleAndContractToDocument(list.get(2), handle, contract);
        }
      }
    }
  }

  private void addHandleAndContractToDocument(Document doc, String handle, String contract) {
    Element root = doc.getDocumentElement();
    Element handleElement = doc.createElementNS("http://www.loc.gov/mods/v3", "identifier");
    handleElement.setPrefix("mods");
    handleElement.setTextContent(handle);
    handleElement.setAttribute("type", "handle");
    root.appendChild(handleElement);
    Element contractElement = doc.createElementNS("http://www.loc.gov/mods/v3", "identifier");
    contractElement.setPrefix("mods");
    contractElement.setTextContent(contract);
    contractElement.setAttribute("type", "contract");
    root.appendChild(contractElement);
  }

  private Document loadDom(File file) {
    try {
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(file);
      doc.getDocumentElement().normalize();
      return doc;
    }
    catch (SAXException e) {
      log.debug("Error reading foxml file: " + e.getStackTrace().toString());
    }
    catch (IOException e) {
      log.debug("Error reading foxml file: " + e.getStackTrace().toString());
    }
    catch (ParserConfigurationException e) {
      log.debug("Error reading foxml file: " + e.getStackTrace().toString());
    }
    return null;
  }

  private void renameUrnToUuid(Element mods) {
    NodeList list = mods.getElementsByTagNameNS("http://www.loc.gov/mods/v3", "identifier");
    for (int i = 0; i < list.getLength(); i++) {
      NamedNodeMap map = list.item(i).getAttributes();
      for (int j = 0; j < map.getLength(); j++) {
        if (map.item(j).getNodeValue().equals("urn")) {
          map.item(j).setNodeValue("uuid");
        }
      }
    }

  }

  private void removeElement(String elementName, String namespace, Element from) {
    Element date = DOMUtil.getFirstChildElementNS(from, namespace, elementName);
    if (date != null)
      from.removeChild(date);
  }

  private void removeElementWithAtribute(String elementName, String namespace, Element from, String atributeName, String atributeValue) {
    NodeList list = from.getElementsByTagNameNS(namespace, elementName);
    for (int i = 0; i < list.getLength(); i++) {
      NamedNodeMap map = list.item(i).getAttributes();
      for (int j = 0; j < map.getLength(); j++) {
        if (map.item(j).getLocalName().equals(atributeName) && map.item(j).getTextContent().equals(atributeValue))
        {
          list.item(i).getParentNode().removeChild(list.item(i));
        }
      }
    }
  }

  private void setUuidsFromK3File(CDM cdm, String cdmId) throws SAXException, IOException, ParserConfigurationException {

    // find K3 file
    File rawDataDir = cdm.getRawDataDir(cdmId);
    File[] listFiles = rawDataDir.listFiles((FileFilter) FileFilterUtils.suffixFileFilter(K3_FILE_SUFFIX));
    if (listFiles == null || listFiles.length != 1) {
      log.error("Incorrect number of K3 files in dir: " + rawDataDir.getAbsolutePath());
      throw new TransformationException("Incorrect number of K3 files in dir: " + rawDataDir.getAbsolutePath());
    }
    File xmlDir = new File(cdm.getRawDataDir(cdmId), K4_DATA_XML_FOLDER);
    File[] listFox = xmlDir.listFiles();
    TreeSet<String> setFox = new TreeSet<String>();
    for (int i = 0; i < listFox.length; i++) {
      setFox.add(listFox[i].getName().substring(0, listFox[i].getName().lastIndexOf('.')));
    }
    File k3File = listFiles[0];
    log.debug("K3 file to parse: " + k3File.getAbsolutePath());
    Document xml = XMLHelper.parseXML(k3File, false);
    NodeList childNodes = xml.getDocumentElement().getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      if (childNodes.item(i) instanceof Element) {
        Element element = (Element) childNodes.item(i);
        if ("UniqueIdentifier".equals(element.getLocalName()) && (mainUuid == null)) {
          Element uuidElement = (Element) element.getElementsByTagName("UniqueIdentifierURNType").item(0);
          mainUuid = uuidElement.getTextContent();
        }
        if ("PeriodicalVolume".equals(element.getLocalName()) && (volumeUuid == null)) {
          Element volumeUuidElement = (Element) element.getElementsByTagName("UniqueIdentifier").item(0);
          volumeUuid = volumeUuidElement.getElementsByTagName("UniqueIdentifierURNType").item(0).getTextContent();
          if (issueUuid == null) {// ***************toto je skarede treba pouzit xpath
            NodeList volumeChildNodes = element.getChildNodes();
            for (int j = 0; j < volumeChildNodes.getLength(); j++) {
              if (volumeChildNodes.item(j) instanceof Element) {
                if (volumeChildNodes.item(j).getAttributes().item(0) != null)
                  if (volumeChildNodes.item(j).getAttributes().item(0) != null && volumeChildNodes.item(j).getAttributes().item(0).getNodeValue().equals("PeriodicalIssue")) {
                    Element piElement = (Element) volumeChildNodes.item(j);
                    if (piElement.hasChildNodes()) {
                      NodeList piNodeList = piElement.getChildNodes();
                      for (int k = 0; k < piNodeList.getLength(); k++) {
                        if (piNodeList.item(k).getNodeName().equals("UniqueIdentifier")) {
                          if (piNodeList.item(k).hasChildNodes()) {
                            NodeList uiNodeList = piNodeList.item(k).getChildNodes();
                            for (int m = 0; m < uiNodeList.getLength(); m++) {
                              if (uiNodeList.item(m).getNodeName().equals("UniqueIdentifierURNType")) {
                                if (setFox.contains(uiNodeList.item(m).getTextContent())) {
                                  issueUuid = uiNodeList.item(m).getTextContent();
                                  break;
                                }
                              }
                            }
                          }
                          break;
                        }
                      }
                    }
                  }
              }
            }// ***************toto je skarede treba pouzit xpath
          }
        }
      }
      if ((mainUuid != null) && (volumeUuid != null) && (issueUuid != null)) {
        break;
      }
    } // end of for
    if (mainUuid == null) {
      log.error("Can't find main uuid in K3 file: " + k3File.getAbsolutePath());
      throw new TransformationException("Can't find main uuid in K3 file: " + k3File.getAbsolutePath());
    }
  }

  private File getXMLFileByUUID(String uuid, CDM cdm, String cdmId) {
    String fileName = uuid + K4_DATA_XML_FILE_EXT;
    File xmlDir = new File(cdm.getRawDataDir(cdmId), K4_DATA_XML_FOLDER);
    File[] listFiles = xmlDir.listFiles((FileFilter) FileFilterUtils.nameFileFilter(fileName));
    if (listFiles == null || listFiles.length != 1) {
      log.error("Incorrect number of XML files in dir: " + xmlDir.getAbsolutePath() + " for name: " + fileName);
      throw new TransformationException("Incorrect number of XML files in dir: " + xmlDir.getAbsolutePath() + " for name: " + fileName);
    }
    return listFiles[0];
  }

  private String getType() {
    nsMap = new HashMap<String, String>();
    nsMap.put("foxml", "info:fedora/fedora-system:def/foxml#");
    nsMap.put("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
    nsMap.put("dc", "http://purl.org/dc/elements/1.1/");

    SAXReader reader = new SAXReader();
    org.dom4j.Document mainXMLDoc = null;
    try {
      mainXMLDoc = reader.read(this.mainFile);
    }
    catch (DocumentException e) {
      throw new SystemException("Error while reading XML file.", ErrorCodes.ERROR_WHILE_READING_FILE);
    }
    org.dom4j.XPath xPath;
    xPath = DocumentHelper.createXPath("//foxml:digitalObject/foxml:datastream/foxml:datastreamVersion/foxml:xmlContent/oai_dc:dc/dc:type");
    xPath.setNamespaceURIs(nsMap);
    Node node = xPath.selectSingleNode(mainXMLDoc);

    String type = node.getText();

    if (type.toLowerCase().contains(MONOGRAPH_TYPE.toLowerCase())) {
      return MONOGRAPH_TYPE;
    }
    else {
      return PERIODICAL_TYPE;
    }
  }

  private void initializeNamespaceMap() {
    nsMap = new HashMap<String, String>();
    nsMap.put("mods", "http://www.loc.gov/mods/v3");
  }

  private void addSectionUUID(String prefix, String cdmId) throws CDMException, XPathExpressionException, IOException, DocumentException, ParserConfigurationException, SAXException {
    DecimalFormat f = new DecimalFormat("0000");
    CDMMetsHelper metsHelper = new CDMMetsHelper();
    for (int i = 1; i < MAX_UNSPLITTED_SECTIONS; i++) {
      String sufix = f.format(i);
      String sectionId = prefix + sufix;
      try {
        log.debug("Looking for sectionId " + sectionId + " to set UUID " + cdmId);
        metsHelper.addIdentifier(cdmId, sectionId, sectionId, "uuid", cdmId);
        log.debug("UUID set");
      }
      catch (METSException e) {
        log.debug("Sectionid " + sectionId + " not found");
        // Do nothing, section does not need to exist
      }
    }
  }

  public static void main(String[] args) throws IOException {

    // new CreateMetsK4Impl().execute("69f3def0-26df-11e4-9660-00505682629d");//periodical	
    new CreateMetsK4Impl().execute("18ed1a30-33fa-11e4-a460-0050568209d3");
    //  new CreateMetsK4Impl().execute("8a279170-23b1-11e4-a390-00505682629d");
    //new CreateMetsK4Impl().execute("7d924390-26d3-11e4-94c7-00505682629d"); //norway 
    // new CreateMetsK4Impl().execute("dabcd530-326d-11e4-811b-0050568209d3");
  }

  public void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer transformer = tf.newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

    transformer.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(out, "UTF-8")));
  }

}
