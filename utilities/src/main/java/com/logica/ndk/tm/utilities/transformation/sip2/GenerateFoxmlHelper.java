/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.sip2;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import info.fedora.foxml.v1.ContentLocationType;
import info.fedora.foxml.v1.DatastreamType;
import info.fedora.foxml.v1.DatastreamVersionType;
import info.fedora.foxml.v1.DigitalObject;
import info.fedora.foxml.v1.ObjectPropertiesType;
import info.fedora.foxml.v1.PropertyType;
import info.fedora.foxml.v1.StateType;
import info.fedora.foxml.v1.XmlContentType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.mule.api.registry.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.Div;
import au.edu.apsr.mtk.base.METS;
import au.edu.apsr.mtk.base.METSWrapper;
import au.edu.apsr.mtk.base.StructMap;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.commons.utils.DateUtils;
import com.logica.ndk.commons.uuid.UUID;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.JAXBContextPool;
import com.logica.ndk.tm.cdm.PerThreadDocBuilderFactory;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.K4NorwayDocHelper;

/**
 * @author kovalcikm
 */
public class GenerateFoxmlHelper extends AbstractUtility {
  private static final Logger LOG = LoggerFactory.getLogger(GenerateFoxmlForSIP2Impl.class);
  public static final String STREAM_VERSION_SUFFIX = ".0";
  public static final String NS_DC = "http://purl.org/dc/elements/1.1/";
  public static final String NS_ADM = "http://www.qbizm.cz/kramerius-fedora/image-adm-description";
  public static final String NS_MODS = "http://www.loc.gov/mods/v3";
  public static final String NS_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
  public static final String NS_FEDORA = "info:fedora/fedora-system:def/model#";
  public static final String NS_KRAMERIUS = "http://www.nsdl.org/ontologies/relationships#";
  public static final String NS_OAI = "http://www.openarchives.org/OAI/2.0/";
  public static final String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance";
  private static final String CUSTOM_MODEL_PREFIX = "kramerius";
  private static final String CUSTOM_MODEL_PREFIX_K4 = "kramerius4";
  protected static final String LOGICAL_STRUCT_LABEL = "Logical_Structure";
  private static final String PHYSICAL_STRUCT_LABEL = "Physical_Structure";
  protected static final String XPATH_TO_FILEID = "/mets:fptr[starts-with(@FILEID, 'UC_')]/@FILEID";
  protected static final String XPATH_TO_FILE_NAME = "/mets:mets/mets:fileSec/mets:fileGrp[@ID='UC_IMGGRP']/mets:file[@ID='{fileId}']/mets:FLocat/@xlink:href";
  protected static final String UC_PREFIX = "userCopy/UC_";
  protected static final String USERCOPY_PREFIX = "userCopy/";
  private static final String DMDSEC_MODS_PREFIX = "MODSMD_";
  private static final String DMDSEC_DC_PREFIX = "DCMD_";
  private String DONATOR;
  private String OTHER_INFO_CONFIG_PATH;
  private String PROFILE_PATH;
  private String DOC_BASE;
  private String PREVIEW;
  private String THUMB;
  private String IMG_FULL;
  private String ALTO;
  private String OCR_TEXT;
  private String PREVIEW_CONT_GRP;
  private String THUMB_CONT_GRP;
  private String IMG_FULL_CONT_GRP;
  private String ALTO_CONT_GRP;
  private String OCR_TEXT_CONT_GRP;
  private boolean GENERATE_ALTO;
  private boolean RENAMING_ALTO;
  private boolean IMGFULL_FULL_NAME;
  private boolean GENERATE_TILES;
  private String TILES_URL;
  public static final String DC_TYPE = "model:page";
  public String policy = "policy:";
  protected List<String> fileUuids;
  protected CDMMetsHelper metsHelper;
  private Document document;
  private DocumentBuilder docBuilder = null;

  private String actualYear;
  private String actualMonth;
  
  public GenerateFoxmlHelper() {
    metsHelper = new CDMMetsHelper();
    DocumentBuilderFactory documentBuilderFactory = PerThreadDocBuilderFactory.getDocumentBuilderFactory();
    try {
      docBuilder = documentBuilderFactory.newDocumentBuilder();
      document = docBuilder.newDocument();
    }
    catch (ParserConfigurationException e) {
      LOG.error("Cannot create document builder");
      throw new SystemException("Cannot initializace FOXML generator");
    }
  }

  protected void initializeStrings(String locality,String cdmId, String actualYear, String actualMonth){
    if(cdm.getCdmDataDir(cdmId).exists()){
      PROFILE_PATH="K4".equals(cdm.getCdmProperties(cdmId).getProperty("importType")) ?"utility.sip2.profile.{place}.generateFoxml.K4.".replace("{place}", locality):"utility.sip2.profile.{place}.generateFoxml.".replace("{place}", locality);
    }else{
      PROFILE_PATH = "utility.sip2.profile.{place}.generateFoxml.".replace("{place}", locality);
    }
    OTHER_INFO_CONFIG_PATH = PROFILE_PATH + "therImagingInformation";

    DONATOR = PROFILE_PATH + "generateFoxml.donator";

    DOC_BASE = TmConfig.instance().getString(PROFILE_PATH + "docBase");
    
    PREVIEW = TmConfig.instance().getString(PROFILE_PATH + "preview").replace("{docBase}", DOC_BASE).replace("{year}", actualYear).replace("{month}", actualMonth);
    PREVIEW_CONT_GRP = TmConfig.instance().getString(PROFILE_PATH + "previewContGrp");

    THUMB = TmConfig.instance().getString(PROFILE_PATH + "thumb").replace("{docBase}", DOC_BASE).replace("{year}", actualYear).replace("{month}", actualMonth);
    THUMB_CONT_GRP = TmConfig.instance().getString(PROFILE_PATH + "thumbContGrp");

    IMG_FULL = TmConfig.instance().getString(PROFILE_PATH + "imgFull").replace("{docBase}", DOC_BASE).replace("{year}", actualYear).replace("{month}", actualMonth);
    IMG_FULL_CONT_GRP = TmConfig.instance().getString(PROFILE_PATH + "imgFullContGrp");

    ALTO = TmConfig.instance().getString(PROFILE_PATH + "alto").replace("{docBase}", DOC_BASE).replace("{year}", actualYear).replace("{month}", actualMonth);
    ALTO_CONT_GRP = TmConfig.instance().getString(PROFILE_PATH + "altoContGrp");

    OCR_TEXT = TmConfig.instance().getString(PROFILE_PATH + "ocrTxt").replace("{docBase}", DOC_BASE).replace("{year}", actualYear).replace("{month}", actualMonth);
    OCR_TEXT_CONT_GRP = TmConfig.instance().getString(PROFILE_PATH + "ocrTxtContGrp");

    GENERATE_ALTO = TmConfig.instance().getBoolean(PROFILE_PATH + "createAlto");
    RENAMING_ALTO = TmConfig.instance().getBoolean(PROFILE_PATH + "renamingAlto");
    GENERATE_TILES = TmConfig.instance().getBoolean(PROFILE_PATH + "createTiles");
    IMGFULL_FULL_NAME = TmConfig.instance().getBoolean(PROFILE_PATH + "fullImageFullName");

    TILES_URL = TmConfig.instance().getString(PROFILE_PATH + "tilesUrl").replace("{docBase}", DOC_BASE).replace("{year}", actualYear).replace("{month}", actualMonth);
  }
  
  protected void initializeStrings(String locality,String cdmId){
   // PROFILE_PATH = "utility.sip2.profile.{place}.generateFoxml.".replace("{place}", locality);
    //Kontorla zda existuje cdm, pro pripade ze se vola delete z ltp-wfm kdy neexistuje cdm
      initializeStrings(locality, cdmId, getActualYear(), getActualMonth());
  }

  protected String getActualYear() {
    return Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
  }

  protected String getActualMonth() {
    String month = Integer.toString(Calendar.getInstance().get(Calendar.MONTH) + 1);
    month = (month.length() == 1) ? "0" + month : month;
    return month;
  }

  protected String generateFoxmlForFile(final String cdmId, String pageLabel, final String fileName, final File outDir, final String locality, final int index, final String pageType) {
    String uuid;
    try {
      uuid = generateUuid(cdmId);
    }
    catch (Exception e1) {
      throw new SystemException("Reading METS failed.", ErrorCodes.ERROR_WHILE_READING_FILE);
    }
    return generateFoxmlForFile(cdmId, pageLabel, fileName, outDir, locality, index, pageType, uuid);
  }

  protected String generateFoxmlForFile(final String cdmId, String pageLabel, final String fileName, final File outDir, final String locality, final int index, final String pageType, final String uuid) {
    checkNotNull(fileName, "inFile must not be null");
    checkNotNull(outDir, "outDir must not be null");

    DigitalObject digitalObject = new DigitalObject();

    String pid = "uuid:" + uuid;

    //set object properties
    /*
         * String pageTitle = getPageTitle(cdmId);
         */

    setObjectProperties(digitalObject, pid, pageLabel);
    try {

      //DC
      Element dc = createDublinCoreElement(pageLabel, uuid, DC_TYPE, this.policy, cdmId, null, null);
      DatastreamType datastreamDC = createDublinCoreStream(dc);
      digitalObject.getDatastream().add(datastreamDC);

//      File mixDirPostProcessingData = new File(cdm.getWorkspaceDir(cdmId).getAbsolutePath() + "/mix/" + CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName());
//      File mix002File = FileUtils.getFile(mixDirPostProcessingData, FilenameUtils.getBaseName(inFile.getName()) + ".xml.mix");
//      MixHelper mix002Helper = MixHelper.getInstance(mix002File.getAbsolutePath());
//      
//      File mixDirFlatData = new File(cdm.getWorkspaceDir(cdmId).getAbsolutePath() + "/mix/" + CDMSchemaDir.FLAT_DATA_DIR.getDirName());
//      File mix001File = FileUtils.getFile(mixDirPostProcessingData, FilenameUtils.getBaseName(inFile.getName()) + ".xml.mix");
//      MixHelper mix001Helper = MixHelper.getInstance(mix001File.getAbsolutePath());
//
//      File mixDirUC = new File(cdm.getWorkspaceDir(cdmId).getAbsolutePath() + "/mix/" + CDMSchemaDir.UC_DIR.getDirName());
//      File jHoveFile = FileUtils.getFile(mixDirUC, inFile.getName() + ".xml");
      //JHoveHelper jHoveHelper = new JHoveHelper(jHoveFile.getAbsolutePath());

      //TODO scannerManufacturer is not in postprocessing data mix (it is in flat data)
      //String scanningDevice = mix001Helper.getScannerManufacturer();
      //TODO get this data only from mix
      //String scanningParameters = "a) " + mix002Helper.getVerticalDpi() + " DPI; b) " + jHoveHelper.getColorDepthJPEG2000() + " bit; c) " + jHoveHelper.getColorSchemeJPEG2000();
      String otherInfo = TmConfig.instance().getString(OTHER_INFO_CONFIG_PATH);

      //ADM
      String admUUID = "";//UUID.timeUUID().toString();
      DatastreamType datastreamADM = createImageMetaStream(admUUID, null, null, null, otherInfo);
      digitalObject.getDatastream().add(datastreamADM);

      //if (locality.equalsIgnoreCase("nkcr")) {
      //IMG_THUMB
      //DatastreamType imgThumb = createStream("IMG_THUMB", THUMB.replace("{uuid}", cdmId).replace("{file}", getFileName(inFile.getName()) + ".jpg"), "image/jpeg");
      DatastreamType imgThumb = createStream("IMG_THUMB", THUMB.replace("{uuid}", cdmId).replace("{file}", getFileName(fileName)), "image/jpeg", THUMB_CONT_GRP);
      digitalObject.getDatastream().add(imgThumb);

      //IMG_PREVIEW

      //DatastreamType imgPrev = createStream("IMG_PREVIEW", PREVIEW.replace("{uuid}", cdmId).replace("{file}", getFileName(inFile.getName()) + ".jpg"), "image/jpeg");
      DatastreamType imgPrev = createStream("IMG_PREVIEW", PREVIEW.replace("{uuid}", cdmId).replace("{file}", getFileName(fileName)), "image/jpeg", PREVIEW_CONT_GRP);
      digitalObject.getDatastream().add(imgPrev);
      //}

      //IMG_FULL
      String fileNameTemp;
      if (IMGFULL_FULL_NAME) {
        fileNameTemp = fileName;
      }
      else {
        fileNameTemp = getFileName(fileName);
      }
      DatastreamType imgFull = createStream("IMG_FULL", IMG_FULL.replace("{uuid}", cdmId).replace("{file}", fileNameTemp), "image/jpeg", IMG_FULL_CONT_GRP);
      digitalObject.getDatastream().add(imgFull);

      String txtUuid = "";//UUID.timeUUID().toString();
      DatastreamType datastreamTXT = createTxtMetaStream(txtUuid);
      digitalObject.getDatastream().add(datastreamTXT);

      //TXT
      //StringBuilder stringBuilder = new StringBuilder(inFile.getName());
      //String txtFileName = stringBuilder.substring(0, stringBuilder.indexOf("."));
      String onlyFileName = fileName;
      if ("K4".equals(cdm.getCdmProperties(cdmId).getProperty("importType")))
      {
        onlyFileName = getFileNameOnly(onlyFileName);
      }else if(onlyFileName.startsWith("UC_")){
          onlyFileName=onlyFileName.substring(3);
        }
      
      DatastreamType txt = createStream("TEXT_OCR", OCR_TEXT.replace("{uuid}", cdmId).replace("{file}", getFileName(onlyFileName) + ".txt"), "text/plain", OCR_TEXT_CONT_GRP);
      digitalObject.getDatastream().add(txt);

      //ALTO
      if (GENERATE_ALTO) {
        String filePath = ALTO.replace("{uuid}", cdmId).replace("{file}", getFileName(onlyFileName) + ".xml");
        if (RENAMING_ALTO) {
          filePath += ".alto";
        }
        DatastreamType alto = createStream("ALTO", filePath, "text/plain", ALTO_CONT_GRP);
        digitalObject.getDatastream().add(alto);
      }

      //MODS

      if (pageLabel == null) {
        pageLabel = Integer.toString(index);
      }
      Element mods = createModsElement(uuid, "text", pageLabel, String.valueOf(index), pageType);
      //Element rootMods = mods.getOwnerDocument().createElementNS(NS_MODS, "mods:modsCollection");
      DatastreamType datastreamMods = createModsStream(mods);
      digitalObject.getDatastream().add(datastreamMods);

      //RELS-EXT

      String tilesUrl = null;
      if (GENERATE_TILES) {
        tilesUrl = TILES_URL.replace("{uuid}", cdmId).replace("{file}", getFileName(fileName));
      }
      Element relsExt = createRelsExtElement(pid, fileName, tilesUrl);
      DatastreamType datastreamRelsExt = createRelsExtStream(relsExt);
      digitalObject.getDatastream().add(datastreamRelsExt);

      DatastreamType datastreamPolicy = createPolicyStream(this.policy);
      digitalObject.getDatastream().add(datastreamPolicy);

      writeFoxmlFile(digitalObject, outDir, uuid);
    }
    catch (Exception e) {
      LOG.error("Error occured while generating FOXML files", e);
      throw new BusinessException("Exception occured while generating FOXMl file", e, ErrorCodes.GENERATE_FO_XML_FOR_SIP2_OCCURED);
    }

    return uuid;
  }

  protected void generateFoxmlForDir(final File xmlDir, String cdmId, String uuid, String documentType, String childType, String dmdType, List<String> uuids) {
    checkNotNull(xmlDir, "xmlDir must not be null");
    
    try {

      DigitalObject digitalObject = new DigitalObject();

      String pid = "uuid:" + uuid;

      //set object properties
      String title = this.getPageTitle(cdmId);
      setObjectProperties(digitalObject, pid, title);

      String donator = TmConfig.instance().getString(DONATOR);

      //DC
      Element dc = (Element) metsHelper.getDmdSec(new CDM(), cdmId, DMDSEC_DC_PREFIX + dmdType);
      dc = updateRightsInDC(dc, policy);
      dc = (Element) document.importNode(dc, true);
      //we need to hack the element as Kramerius need something little bit different
      
      String documentTypeSheetMusicAndMap=null;
      try {
        documentTypeSheetMusicAndMap= metsHelper.getTypeOfResource(cdm,cdmId);
        if(!("cartographic".equals(documentTypeSheetMusicAndMap) || "sheetmusic".equals(documentTypeSheetMusicAndMap)))
        {
          documentTypeSheetMusicAndMap=null;
        }else{
          log.info("documentType changed to: "+documentTypeSheetMusicAndMap);
          documentType=documentTypeSheetMusicAndMap;
        }
      }
      catch (Exception e) {
        log.info("typeOfResource not found");
      }

      updateDCElement(dc, documentType, this.policy);

      DatastreamType datastreamDC = createDublinCoreStream(dc);
      digitalObject.getDatastream().add(datastreamDC);

      ///MODS
      Element mods = (Element) metsHelper.getDmdSec(new CDM(), cdmId, DMDSEC_MODS_PREFIX + dmdType);
      Element rootMods = mods.getOwnerDocument().createElementNS(NS_MODS, "mods:modsCollection");
      rootMods.appendChild(mods);
      DatastreamType datastreamMods = createModsStream(rootMods);
      digitalObject.getDatastream().add(datastreamMods);

      //EXT
      Element relsExt = createMainRelsExtElement(pid, documentType, donator, childType, uuids, cdmId);
      DatastreamType datastreamRelsExt = createRelsExtStream(relsExt);
      digitalObject.getDatastream().add(datastreamRelsExt);

      DatastreamType datastreamPolicy = createPolicyStream(this.policy);
      digitalObject.getDatastream().add(datastreamPolicy);

      writeFoxmlFile(digitalObject, xmlDir, uuid);
    }
    catch (Exception e) {
      LOG.error("Error occured while generating FOXML files", e);
      throw new BusinessException("Exception occured while generating FOXMl file", e, ErrorCodes.GENERATE_FO_XML_FOR_SIP2_OCCURED);
    }
    //               
  }

  protected void generateFoXmlForIssue(File xmlDir, String cdmId, String uuid, String modsDmdId, int order, String type, List<String> uuids) {
    DigitalObject digitalObject = new DigitalObject();

    try {
      String pid = "uuid:" + uuid;
      String donator = TmConfig.instance().getString(DONATOR);
      String dcDmdId = modsDmdId.replace("MODS", "DC");

      String model = "";
      if ("ISSUE".equalsIgnoreCase(type)) {
        model += "periodicalitem";
      }
      else {
        model += "supplement";
      }

      setObjectProperties(digitalObject, pid, "");
      String issueDate = getIsuueDate(cdmId, modsDmdId);

      //DC
      Element dc = (Element) metsHelper.getDmdSec(cdm, cdmId, dcDmdId);
      if (dc == null) {
        String dcModel = "model:" + model;
        dc = createDublinCoreElement(Integer.toString(order), uuid, dcModel, this.policy, cdmId, issueDate, null);
      }
      else {
        //dc = updateRightsInDC(dc, policy);
        dc = updateDCElement(dc, model, policy);
      }
      DatastreamType datastreamDC = createDublinCoreStream(dc);
      digitalObject.getDatastream().add(datastreamDC);

      ///MODS
      Element mods = (Element) metsHelper.getDmdSec(cdm, cdmId, modsDmdId);

      Element rootMods = mods.getOwnerDocument().createElementNS(NS_MODS, "mods:modsCollection");

      //Recomendet field
      /*
             * Element modsPart =
             * mods.getOwnerDocument().createElementNS(NS_MODS, "mods:part");
             * modsPart.setAttribute("type", "PeriodicalIssue");
             *
             * Element detailType =
             * mods.getOwnerDocument().createElementNS(NS_MODS, "mods:detail");
             * detailType.setAttribute("type", "issue"); Element modsNumber =
             * mods.getOwnerDocument().createElementNS(NS_MODS, "mods:number");
             * modsNumber.setTextContent(getIssuePartNumber(cdmId, modsDmdId));
             * detailType.appendChild(modsNumber);
             * modsPart.appendChild(detailType); Element modsDate =
             * mods.getOwnerDocument().createElementNS(NS_MODS, "mods:date");
             * modsDate.setTextContent(issueDate);
             * modsPart.appendChild(modsDate);
             *
             * mods.appendChild(modsPart);
             */

      //Remove surplus title
      removeTitleFromMods(mods, "mods:titleInfo", false);

      rootMods.appendChild(mods);
      DatastreamType datastreamMods = createModsStream(rootMods);
      digitalObject.getDatastream().add(datastreamMods);

      //EXT
      Element relsExt = createMainRelsExtElement(pid, model, donator, "kramerius:hasPage", uuids, cdmId);
      DatastreamType datastreamRelsExt = createRelsExtStream(relsExt);
      digitalObject.getDatastream().add(datastreamRelsExt);

      DatastreamType datastreamPolicy = createPolicyStream(this.policy);
      digitalObject.getDatastream().add(datastreamPolicy);

      writeFoxmlFile(digitalObject, xmlDir, uuid);
    }
    catch (Exception e) {
      LOG.error("Error occured while generating FOXML files", e);
      throw new BusinessException("Exception occured while generating FOXMl file", e, ErrorCodes.GENERATE_FO_XML_FOR_SIP2_OCCURED);
    }
  }

  protected void removeTitleFromMods(Element root, String tagName, boolean remove) {
    NodeList childs = root.getChildNodes();
    for (int i = 0; i < childs.getLength(); i++) {
      Node element = childs.item(i);

      if (element.getNodeName().equalsIgnoreCase(tagName)) {
        if (remove) {
          root.removeChild(element);
        }
        else {
          removeTitleFromMods((Element) element, "mods:title", true);
        }
      }
    }
  }

  protected String generateFoXmlForVolume(File xmlDir, String cdmId, List<String> issuesUuids, String type) {
    DigitalObject digitalObject = new DigitalObject();
    CDMMetsHelper helper = new CDMMetsHelper();

    String childType;
    if ("ISSUE".equalsIgnoreCase(type)) {
      childType = "kramerius:hasItem";
    }
    else {
      childType = "kramerius:hasIntCompPart";
    }

    try {
      String uuid = getVolumeUuid(cdmId);
      String pid = "uuid:" + uuid;
      String donator = TmConfig.instance().getString(DONATOR);

      setObjectProperties(digitalObject, pid, "");

      //DC
      String volumeDate = getVolumeDate(cdmId);
      Element dc = (Element) helper.getDmdSec(cdm, cdmId, "DCMD_VOLUME_0001");
      if (dc == null) {
        dc = createDublinCoreElement("", uuid, "model:periodicalvolume", this.policy, cdmId, volumeDate, null);
      }
      else {
        //dc = updateRightsInDC(dc, policy);
        dc = updateDCElement(dc, "periodicalvolume", policy);
      }
      DatastreamType datastreamDC = createDublinCoreStream(dc);
      digitalObject.getDatastream().add(datastreamDC);

      ///MODS
      Element mods = (Element) helper.getDmdSec(cdm, cdmId, "MODSMD_VOLUME_0001");
      Element rootMods = mods.getOwnerDocument().createElementNS(NS_MODS, "mods:modsCollection");
      Element modsPart = mods.getOwnerDocument().createElementNS(NS_MODS, "mods:part");
      Element modsDate = mods.getOwnerDocument().createElementNS(NS_MODS, "mods:date");

      modsDate.setTextContent(volumeDate);
      modsPart.appendChild(modsDate);
      mods.appendChild(modsPart);
      rootMods.appendChild(mods);

      DatastreamType datastreamMods = createModsStream(rootMods);
      digitalObject.getDatastream().add(datastreamMods);

      //EXT

      Element relsExt = createMainRelsExtElement(pid, "periodicalvolume", donator, childType, issuesUuids, cdmId);
      DatastreamType datastreamRelsExt = createRelsExtStream(relsExt);
      digitalObject.getDatastream().add(datastreamRelsExt);

      DatastreamType datastreamPolicy = createPolicyStream(this.policy);
      digitalObject.getDatastream().add(datastreamPolicy);

      writeFoxmlFile(digitalObject, xmlDir, uuid);

      return uuid;
    }
    catch (Exception e) {
      LOG.error("Error occured while generating FOXML files", e);
      throw new BusinessException("Exception occured while generating FOXMl file", e, ErrorCodes.GENERATE_FO_XML_FOR_SIP2_OCCURED);
    }

  }

  @RetryOnFailure(attempts = 2)
  protected void writeFoxmlFile(final DigitalObject digitalObject, final File outDir, final String uuid) {
    checkNotNull(digitalObject, "digitalObject must not be null");
    checkNotNull(outDir, "outDir must not be null");
    checkNotNull(uuid, "uuid must not be null");

    if (!outDir.exists()) {
      outDir.mkdirs();
    }
    try {
      JAXBContext context = JAXBContextPool.getContext("info.fedora.foxml.v1");
      Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.setProperty(Marshaller.JAXB_ENCODING, "utf-8");
      marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "info:fedora/fedora-system:def/foxml# http://www.fedora.info/definitions/1/0/foxml1-1.xsd");
      marshaller.marshal(digitalObject, new File(outDir, uuid + SIP2Constants.XML_FILE_EXTENSION));
    }
    catch (JAXBException e) {
      log.error(format("DigitalObject marshaling to %s failed", outDir), e);
      throw new SystemException(format("DigitalObject marshaling to %s failed", outDir), e, ErrorCodes.JAXB_MARSHALL_ERROR);
    }
  }

  /**
   * Nastavi atributy spolecne pro vsechny digitalni objekty
   * 
   * @param digitalObject
   */
  protected void setObjectProperties(DigitalObject digitalObject, String pid, String title) {
    digitalObject.setPID(pid);
    digitalObject.setVERSION("1.1");

    if (digitalObject.getObjectProperties() == null) {
      digitalObject.setObjectProperties(new ObjectPropertiesType());
    }

    setProperty(digitalObject, "info:fedora/fedora-system:def/model#label", title != null ? title.substring(0, Math.min(255, title.length())) : "null");
    setProperty(digitalObject, "info:fedora/fedora-system:def/model#state", "Active");
    setProperty(digitalObject, "info:fedora/fedora-system:def/model#ownerId", "fedoraAdmin");

    String timestamp = DateUtils.toXmlDateTime(new Date()).toString();
    setProperty(digitalObject, "info:fedora/fedora-system:def/model#createdDate", timestamp);
    setProperty(digitalObject, "info:fedora/fedora-system:def/view#lastModifiedDate", timestamp);
  }

  /**
   * Prida property digitalnimu objektu
   * 
   * @param digitalObject
   * @param name
   * @param value
   */
  private void setProperty(DigitalObject digitalObject, String name, String value) {
    PropertyType pt = new PropertyType();
    pt.setNAME(name);
    pt.setVALUE(value);
    digitalObject.getObjectProperties().getProperty().add(pt);
  }

  private DatastreamType createDublinCoreStream(Element dc) {
    DatastreamType stream = new DatastreamType();
    stream.setID("DC");
    stream.setSTATE(StateType.A);
    stream.setVERSIONABLE(false);
    stream.setCONTROLGROUP("X");

    DatastreamVersionType version = new DatastreamVersionType();
    version.setCREATED(DateUtils.toXmlDateTime(new Date()));
    version.setFORMATURI("http://www.openarchives.org/OAI/2.0/oai_dc/");
    version.setID("DC" + STREAM_VERSION_SUFFIX);
    version.setLABEL("Dublin Core Record for this object");
    version.setMIMETYPE("text/xml");
    stream.getDatastreamVersion().add(version);

    XmlContentType xmlContent = new XmlContentType();
    version.setXmlContent(xmlContent);

    xmlContent.getAny().add(dc);

    return stream;
  }

  private DatastreamType createImageMetaStream(String urn, String sici, String scanningDevice, String scanningParameters, String otherInfo) {
    DatastreamType stream = new DatastreamType();
    stream.setID("IMG_FULL_ADM");
    stream.setCONTROLGROUP("X");
    stream.setSTATE(StateType.A);
    stream.setVERSIONABLE(false);

    DatastreamVersionType version = new DatastreamVersionType();
    version.setID("IMG_FULL_ADM" + STREAM_VERSION_SUFFIX);
    version.setLABEL("Image administrative metadata");
    version.setMIMETYPE("text/xml");
    version.setCREATED(DateUtils.toXmlDateTime(new Date()));

    XmlContentType xmlContent = new XmlContentType();
    version.setXmlContent(xmlContent);

    Element root = document.createElementNS(NS_ADM, "adm:Description");

    if (urn != null) {
      this.appendChildNS(document, root, NS_ADM, "adm:URN", urn);
    }
    if (sici != null) {
      this.appendChildNS(document, root, NS_ADM, "adm:SICI", sici);
    }
    if (scanningDevice != null) {
      this.appendChildNS(document, root, NS_ADM, "adm:ScanningDevice", scanningDevice);
    }
    if (scanningParameters != null) {
      this.appendChildNS(document, root, NS_ADM, "adm:ScanningParameters", scanningParameters);
    }
    if (otherInfo != null) {
      this.appendChildNS(document, root, NS_ADM, "adm:OtherImagingInformation", otherInfo);
    }

    xmlContent.getAny().add(root);

    stream.getDatastreamVersion().add(version);
    return stream;
  }

  private DatastreamType createTxtMetaStream(String urn) {
    DatastreamType stream = new DatastreamType();
    stream.setID("TEXT_OCR_ADM");
    stream.setCONTROLGROUP("X");
    stream.setSTATE(StateType.A);
    stream.setVERSIONABLE(false);

    DatastreamVersionType version = new DatastreamVersionType();
    version.setID("TEXT_OCR_ADM" + STREAM_VERSION_SUFFIX);
    version.setLABEL("Image administrative metadata");
    version.setMIMETYPE("text/xml");
    version.setCREATED(DateUtils.toXmlDateTime(new Date()));

    XmlContentType xmlContent = new XmlContentType();
    version.setXmlContent(xmlContent);

    Element root = document.createElementNS(NS_ADM, "adm:Description");

    if (urn != null) {
      this.appendChildNS(document, root, NS_ADM, "adm:URN", urn);
    }

    xmlContent.getAny().add(root);

    stream.getDatastreamVersion().add(version);
    return stream;
  }

  private DatastreamType createStream(String imgId, String filePath, String mimeType, String controlGroup) {
    //log.debug("filePath : " + filePath);
    DatastreamType stream = new DatastreamType();
    stream.setID(imgId);
    /*
     * if((imgId.equalsIgnoreCase("ALTO") ||
     * imgId.equalsIgnoreCase("TEXT_OCR")) &&
     * locality.equalsIgnoreCase("mzk")){ stream.setCONTROLGROUP("M");
     * }else{ stream.setCONTROLGROUP("E");
    }
     */
    stream.setCONTROLGROUP(controlGroup == null ? "E" : controlGroup);
    stream.setVERSIONABLE(false);
    stream.setSTATE(StateType.A);

    DatastreamVersionType version = new DatastreamVersionType();
    version.setCREATED(DateUtils.toXmlDateTime(new Date()));
    version.setID(imgId + STREAM_VERSION_SUFFIX);

    //version.setMIMETYPE("image/jpeg");
    version.setMIMETYPE(mimeType);

    // long start = System.currentTimeMillis();

    //String binaryDirectory = getConfig().getExportFolder() + System.getProperty("file.separator") + "thumbnail";
    // Destination directory
    //File dir = IOUtils.checkDirectory(binaryDirectory);
    // Move file to new directory
    //File target = new File(dir, filename.substring(filename.lastIndexOf(System.getProperty("file.separator")), filename.lastIndexOf('.'))+".jpg");
//          FileUtils.writeByteArrayToFile(target, binaryContent);

    ContentLocationType cl = new ContentLocationType();
    cl.setREF(filePath);
    cl.setTYPE("URL");
    version.setContentLocation(cl);

    stream.getDatastreamVersion().add(version);

    return stream;

  }

  private DatastreamType createModsStream(Element mods) throws ServiceException {
    DatastreamType stream = new DatastreamType();
    stream.setID("BIBLIO_MODS");
    stream.setCONTROLGROUP("X");
    stream.setSTATE(StateType.A);
    stream.setVERSIONABLE(false);

    DatastreamVersionType version = new DatastreamVersionType();
    version.setID("BIBLIO_MODS" + STREAM_VERSION_SUFFIX);
    version.setLABEL("BIBLIO_MODS description of current object");
    version.setFORMATURI("http://www.loc.gov/mods/v3");
    version.setMIMETYPE("text/xml");
    version.setCREATED(DateUtils.toXmlDateTime(new Date()));

    if (mods != null) {
      XmlContentType xmlContent = new XmlContentType();
      xmlContent.getAny().add(mods);
      version.setXmlContent(xmlContent);
      stream.getDatastreamVersion().add(version);
    }

    return stream;
  }

  private DatastreamType createRelsExtStream(Element root) throws ServiceException {
    DatastreamType stream = new DatastreamType();
    stream.setID("RELS-EXT");
    stream.setCONTROLGROUP("X");
    stream.setVERSIONABLE(false);
    stream.setSTATE(StateType.A);

    DatastreamVersionType version = new DatastreamVersionType();
    version.setCREATED(DateUtils.toXmlDateTime(new Date()));
    version.setFORMATURI("info:fedora/fedora-system:FedoraRELSExt-1.0");
    version.setLABEL("RDF Statements about this object");
    version.setMIMETYPE("application/rdf+xml");
    version.setID("RELS-EXT" + STREAM_VERSION_SUFFIX);

    XmlContentType xmlContent = new XmlContentType();
    version.setXmlContent(xmlContent);

    xmlContent.getAny().add(root);

    stream.getDatastreamVersion().add(version);
    return stream;
  }

  protected DatastreamType createPolicyStream(String policyID) {
    DatastreamType stream = new DatastreamType();
    stream.setID("POLICY");
    stream.setCONTROLGROUP("E");
    stream.setVERSIONABLE(false);
    stream.setSTATE(StateType.A);

    DatastreamVersionType version = new DatastreamVersionType();
    version.setCREATED(DateUtils.toXmlDateTime(new Date()));
    version.setID("POLICY" + STREAM_VERSION_SUFFIX);
    version.setMIMETYPE("application/rdf+xml");
    ContentLocationType location = new ContentLocationType();
    location.setTYPE("URL");
    location.setREF("http://local.fedora.server/fedora/get/" + policyID + "/" + "POLICYDEF");
    version.setContentLocation(location);
    stream.getDatastreamVersion().add(version);

    return stream;
  }

  private Element createDublinCoreElement(String title, String uuid, String type, String rights, String cdmId, String date, String partNumber) throws ParserConfigurationException {

    Element root = document.createElementNS("http://www.openarchives.org/OAI/2.0/oai_dc/", "oai_dc:dc");

    root.setAttribute("xmlns:dc", NS_DC);

    appendChildNS(document, root, NS_DC, "dc:title", title);
    appendChildNS(document, root, NS_DC, "dc:identifier", "uuid:" + uuid);
    String ccnb = metsHelper.getCcnb(cdmId);
    if ((ccnb != null) && (!ccnb.isEmpty())) {
      appendChildNS(document, root, NS_DC, "dc:identifier", "ccnb:" + ccnb);
    }
    appendChildNS(document, root, NS_DC, "dc:type", type);
    appendChildNS(document, root, NS_DC, "dc:rights", rights);
    if (date != null) {
      appendChildNS(document, root, NS_DC, "dc:date", date);
    }

    if (partNumber != null) {
    }
    return root;
  }

  private Element createModsElement(String urn, String typeOfResource, String pgNumber, String pgIndex, String pgType) throws ServiceException {

    Element root = document.createElementNS(NS_MODS, "mods:modsCollection");
    root.setAttribute("xmlns:mods", NS_MODS);
    root.setAttribute("xmlns:xsi", NS_XSI);

    Element mods = document.createElementNS(NS_MODS, "mods:mods");
    mods.setAttribute("version", "3.4");

    Element ident = appendChildNS(document, mods, NS_MODS, "mods:identifier", urn);
    ident.setAttribute("type", "urn");
    appendChildNS(document, mods, NS_MODS, "mods:typeOfResource", typeOfResource);

    Element modsPart = document.createElementNS(NS_MODS, "mods:part");
    modsPart.setAttribute("type", pgType);

    Element pageNumber = document.createElementNS(NS_MODS, "mods:detail");
    pageNumber.setAttribute("type", "pageNumber");
    appendChildNS(document, pageNumber, NS_MODS, "mods:number", pgNumber);

    modsPart.appendChild(pageNumber);

    Element pageIndex = document.createElementNS(NS_MODS, "mods:detail");
    pageIndex.setAttribute("type", "pageIndex");
    appendChildNS(document, pageIndex, NS_MODS, "mods:number", pgIndex);

    modsPart.appendChild(pageIndex);

    mods.appendChild(modsPart);

    root.appendChild(mods);

    return root;
  }

  private Element createMainRelsExtElement(String uuid, String documentType, String donator, String childType, List<String> uuids, String cdmId) throws SAXException, IOException, ParserConfigurationException {
    Element root = document.createElementNS(NS_RDF, "rdf:RDF");

    root.setAttribute("xmlns:fedora-model", NS_FEDORA);
    root.setAttribute("xmlns:" + CUSTOM_MODEL_PREFIX, NS_KRAMERIUS);
    root.setAttribute("xmlns:" + CUSTOM_MODEL_PREFIX_K4, NS_KRAMERIUS);
    root.setAttribute("xmlns:oai", NS_OAI);

    Element description = this.appendChildNS(document, root, NS_RDF, "rdf:Description", "");
    description.setAttributeNS(NS_RDF, "rdf:about", "info:fedora/" + uuid);

    Element el = appendChildNS(document, description, NS_FEDORA, "fedora-model:hasModel", "");
    el.setAttributeNS(NS_RDF, "rdf:resource", "info:fedora/model:" + documentType);

    appendChildNS(document, description, NS_OAI, "oai:itemID", uuid);
    appendChildNS(document, description, NS_KRAMERIUS, "kramerius:policy", this.policy);
    String[] handleContract = getHandleAndContractFromMEts(cdmId);
    appendChildNS(document, description, NS_KRAMERIUS, "kramerius:handle", handleContract[0]);
    appendChildNS(document, description, NS_KRAMERIUS, "kramerius:contract", handleContract[1]);

    for (String childUiid : uuids) {
      Element kramerius = appendChildNS(document, description, NS_KRAMERIUS, childType, "");
      kramerius.setAttributeNS(NS_RDF, "rdf:resource", "info:fedora/uuid:" + childUiid);
    }
    if (("K4".equals(cdm.getCdmProperties(cdmId).getProperty("importType"))) && K4NorwayDocHelper.isNorwayDoc(cdmId, cdm))
    {
      Element kramerius = appendChildNS(document, description, NS_KRAMERIUS, "hasDonator", "");
      kramerius.setAttributeNS(NS_RDF, "rdf:resource", "info:fedora/donator:norway");
    }
    return root;
  }

  private String[] getHandleAndContractFromMEts(String cdmId) throws SAXException, IOException, ParserConfigurationException
  {
    String handle = null;
    String contract = null;
    File mets = cdm.getMetsFile(cdmId);
    Document xml = XMLHelper.parseXML(mets, false);
    NodeList list = xml.getElementsByTagName("mods:identifier");
    for (int i = 0; i < list.getLength(); i++) {
      if (handle == null || contract == null) {
        NamedNodeMap map = list.item(i).getAttributes();
        if (map != null)
        {
          for (int j = 0; j < map.getLength(); j++) {
            if (map.item(j).getLocalName().equals("type"))
            {
              if (map.item(j).getTextContent().equals("handle"))
              {
                handle = list.item(i).getTextContent();
              }
              if (map.item(j).getTextContent().equals("contract"))
              {
                contract = list.item(i).getTextContent();
              }
            }
          }
        }
      }
    }
    return new String[] { handle, contract };
  }

  protected Element createRelsExtElement(String uuid, String file, String tilesUrl) {
    Element root = document.createElementNS(NS_RDF, "rdf:RDF");

    root.setAttribute("xmlns:fedora-model", NS_FEDORA);
    root.setAttribute("xmlns:" + CUSTOM_MODEL_PREFIX, NS_KRAMERIUS);
    root.setAttribute("xmlns:" + CUSTOM_MODEL_PREFIX_K4, NS_KRAMERIUS);
    root.setAttribute("xmlns:oai", NS_OAI);

    Element description = this.appendChildNS(document, root, NS_RDF, "rdf:Description", "");
    description.setAttributeNS(NS_RDF, "rdf:about", "info:fedora/" + uuid);

    Element el = appendChildNS(document, description, NS_FEDORA, "fedora-model:hasModel", "");
    el.setAttributeNS(NS_RDF, "rdf:resource", "info:fedora/model:page");

    appendChildNS(document, description, NS_OAI, "oai:itemID", uuid);

    appendChildNS(document, description, NS_KRAMERIUS, "kramerius:file", file);

    appendChildNS(document, description, NS_KRAMERIUS, "kramerius:policy", this.policy);

    if (GENERATE_TILES) {
      appendChildNS(document, description, NS_KRAMERIUS, "kramerius4:tiles-url", tilesUrl);
    }

    return root;
  }

  private Element appendChildNS(Document d, Node parent, String prefix, String name, String value) {
    Element e = d.createElementNS(prefix, name);
    e.setTextContent(value);
    parent.appendChild(e);
    return e;
  }

  private Element updateDCElement(Element dc, String documentType, String policy) {

    NodeList types = dc.getElementsByTagName("dc:type");
    if (types != null && types.getLength() > 0) {
      Element dcType = (Element) types.item(0);
      dcType.setTextContent("model:" + documentType);
    }
    else {
      //appendChildNS(document, dc, NS_DC, "dc:type", "model:"+documentType);
      LOG.debug("!!!!!");
    }

    dc = updateRightsInDC(dc, policy);

    //appendChildNS(document, dc, NS_DC, "dc:rights", policy);

    return dc;
  }

  private Element updateRightsInDC(Element dc, String policy) {
    dc = (Element) document.importNode(dc, true);
    appendChildNS(document, dc, NS_DC, "dc:rights", policy);

    return dc;
  }

  private String getFileName(String fileName) {
    String result;
    if (fileName != null && fileName.lastIndexOf(".") > -1) {
      result = fileName.substring(0, fileName.lastIndexOf("."));
    }
    else {
      result = fileName;
    }
    return result;
  }
  private String getFileNameOnly(String fileName) {
    String result;
    if (fileName != null && fileName.indexOf(".") > -1) {
      result = fileName.substring(0, fileName.indexOf("."));
    }
    else {
      result = fileName;
    }
    return result;
  }
  private List<String> generateXmlUuid(String cdmId) throws Exception {
    //uuid is generated as MD5 from cdmId and id in Logical_Structure
    List<String> uuids = new ArrayList<String>();
    Document metsDocument = XMLHelper.parseXML(cdm.getMetsFile(cdmId));
    METSWrapper mw = new METSWrapper(metsDocument);
    METS mets = mw.getMETSObject();
    List<StructMap> maps = mets.getStructMaps();

    StructMap logicalStructMap = null;
    StructMap physicalStructMap = null;

    for (StructMap map : maps) {
      if (map.getLabel().equalsIgnoreCase(LOGICAL_STRUCT_LABEL)) {
        logicalStructMap = map;
      }
      if (map.getLabel().equalsIgnoreCase(PHYSICAL_STRUCT_LABEL)) {
        physicalStructMap = map;
      }
    }

    List<Div> logicalMapDivs = logicalStructMap.getDivs().get(0).getDivs().get(0).getDivs();

    for (Div div : logicalMapDivs) {
      //uuids.add(generateUuid(cdmId, div.getID()));
    }
    return uuids;
  }

  protected String generateUuid(String cdmId) {
    return UUID.timeUUID().toString();
    //return DigestUtils.md5DigestAsHex((cdmId + System.currentTimeMillis()).getBytes());
  }

  private String getPageTitle(String cdmId) {
    Document metsDocument;
    METSWrapper mw;
    try {
      metsDocument = XMLHelper.parseXML(cdm.getMetsFile(cdmId));
      mw = new METSWrapper(metsDocument);
    }
    catch (Exception e) {
      throw new SystemException("METS parsing error.", ErrorCodes.XML_PARSING_ERROR);
    }
    METS mets = mw.getMETSObject();
    return mets.getLabel();
  }

  protected String getIssuePartNumber(String cdmId, String issueDmdId) {
    return getValueFromMets(cdmId, "//mets:mets/mets:dmdSec[@ID='" + issueDmdId + "']/mets:mdWrap/mets:xmlData/mods:mods/mods:titleInfo/mods:partNumber/text()");
  }

  protected String getpageNumber(String cdmId, String pageId) {
    return getValueFromMets(cdmId, "//mets:mets/mets:structMap[@TYPE='PHYSICAL']/mets:div/mets:div[" + pageId + "]/@ORDERLABEL");
  }

  protected List<org.dom4j.Node> getPhysicalMapPages(String cdmId) {
    return getNodesFromMets(cdmId, "//mets:mets/mets:structMap[@TYPE='PHYSICAL']/mets:div/mets:div");
  }

  private String getIsuueDate(String cdmId, String issueDmdId) {
    String date = getValueFromMets(cdmId, "//mets:mets/mets:dmdSec[@ID='" + issueDmdId + "']/mets:mdWrap/mets:xmlData/mods:mods/mods:originInfo/mods:dateIssued/text()");
    return date;
  }

  private String getVolumeDate(String cdmId) {
    return getValueFromMets(cdmId, "//mets:mets/mets:dmdSec[@ID='MODSMD_VOLUME_0001']/mets:mdWrap/mets:xmlData/mods:mods/mods:originInfo/mods:dateIssued");
  }

  private String getVolumeUuid(String cdmId) {
    return getValueFromMets(cdmId, "//mets:mets/mets:dmdSec[@ID='MODSMD_VOLUME_0001']/mets:mdWrap/mets:xmlData/mods:mods/mods:identifier[@type='uuid']");
  }

  protected String getUuid(String cdmId, String dmdId) {
    return getValueFromMets(cdmId, "//mets:mets/mets:dmdSec[@ID='" + dmdId + "']/mets:mdWrap/mets:xmlData/mods:mods/mods:identifier[@type='uuid']");
  }

  private List<org.dom4j.Node> getNodesFromMets(String cdmId, String xPathString) {
    return new CDMMetsHelper().getNodesFromMets(xPathString, cdm, cdmId);
  }

  

  protected String getValueFromMets(String cdmId, String xPathString) {
    CDMMetsHelper helper = new CDMMetsHelper();
    return helper.getValueFormMets(xPathString, cdm, cdmId);

    /*
     * File metsFile = cdm.getMetsFile(cdmId); SAXReader saxReader = new
     * SAXReader(); org.dom4j.Document metsDoc; try { metsDoc =
     * saxReader.read(metsFile); //XPath xPath =
     * DocumentHelper.createXPath("//mets:mets/mets:dmdSec[@ID='MODSMD_ISSUE_0001']/mets:mdWrap/mets:xmlData/mods:mods/mods:titleInfo/mods:partNumber/text()");
     * XPath xPath = DocumentHelper.createXPath(xPathString); Map<String,
     * String> namespaces = new HashMap<String, String>();
     * namespaces.put("mods", "http://www.loc.gov/mods/v3");
     * namespaces.put("mets", "http://www.loc.gov/METS/");
     * xPath.setNamespaceURIs(namespaces);
     *
     * org.dom4j.Node node = xPath.selectSingleNode(metsDoc); if (node ==
     * null) { return null; } log.debug(node.getText()); return
     * node.getText(); } catch (DocumentException e) {
     * log.error(e.getMessage()); return null;
    }
     */

  }

  @RetryOnFailure(attempts = 3)
  protected void retriedCleanDirectory(File directory) throws IOException {
    FileUtils.cleanDirectory(directory);
  }
}
