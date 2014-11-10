package com.logica.ndk.tm.cdm;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.io.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.activation.MimetypesFileTypeMap;
import javax.validation.Valid;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.xerces.util.DOMUtil;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.*;

import com.csvreader.CsvReader;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.commons.utils.DateUtils;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.cdm.metsHelper.DateCreatedStrategy;
import com.logica.ndk.tm.cdm.metsHelper.DefaultDateCreatedStrategy;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.em.EmConstants;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord.EmPageType;

public class CDMMetsHelper {
  private static final Logger LOG = LoggerFactory.getLogger(CDMMetsHelper.class);

  public final static String DOCUMENT_TYPE_MONOGRAPH = "Monograph";
  public final static String DOCUMENT_TYPE_PERIODICAL = "Periodical";
  public final static String DOCUMENT_TYPE_MANUSCRIPTORIUM = "Manuscript";
  public final static String DOCUMENT_LABEL_MANUSCRIPTORIUM = "Manuscriptorium";

  public final static String MONOGRAPH_PAGE = "MONOGRAPH_PAGE";
  public final static String PERIODICAL_PAGE = "PERIODICAL_PAGE";
  public final static String STRUCT_MAP_TYPE_PHYSICAL = "PHYSICAL";
  public final static String STRUCT_MAP_TYPE_LOGICAL = "LOGICAL";

  public final static String DMDSEC_ID_MODS_VOLUME = "MODSMD_VOLUME_0001";
  public final static String DMDSEC_ID_MODS_TITLE = "MODSMD_TITLE_0001";
  public final static String DMDSEC_ID_MODS_ISSUE = "MODSMD_ISSUE_0001";
  public final static String DMDSEC_ID_MODS_SUPPLEMENT = "MODSMD_SUPPLEMENT_0001";
  public final static String DMDSEC_ID_DC_VOLUME = "DCMD_VOLUME_0001";
  public final static String DMDSEC_ID_DC_TITLE = "DCMD_TITLE_0001";
  public final static String DMDSEC_ID_DC_ISSUE = "DCMD_ISSUE_0001";
  public final static String DMDSEC_ID_DC_SUPPLEMENT = "DCMD_SUPPLEMENT_0001";
  public final static String DMDSEC_ID_PREFIX_MODSMD_TITLE = "MODSMD_TITLE_";
  public final static String DMDSEC_ID_PREFIX_MODSMD_ISSUE = "MODSMD_ISSUE_";
  public final static String DMDSEC_ID_PREFIX_MODSMD_VOLUME = "MODSMD_VOLUME_";
  public final static String DMDSEC_ID_PREFIX_MODSMD_SUPPL = "MODSMD_SUPPLEMENT_";
  public final static String DMDSEC_ID_PREFIX_MODSMD_ART = "MODSMD_ARTICLE_";
  public final static String DMDSEC_ID_PREFIX_MODSMD_PICT = "MODSMD_PICTURE_";
  public final static String DMDSEC_ID_PREFIX_DCMD_TITLE = "DCMD_TITLE_";
  public final static String DMDSEC_ID_PREFIX_DCMD_ISSUE = "DCMD_ISSUE_";
  public final static String DMDSEC_ID_PREFIX_DCMD_VOLUME = "DCMD_VOLUME_";
  public final static String DMDSEC_ID_PREFIX_DCMD_SUPPL = "DCMD_SUPPLEMENT_";
  public final static String DMDSEC_ID_PREFIX_DCMD_ART = "DCMD_ARTICLE_";
  public final static String DMDSEC_ID_PREFIX_DCMD_PICT = "DCMD_PICTURE_";

  public final static String K4_AMDSEC_ID = "K4";
  private final static char MONOGRAPH_TYPE = 'm';
  private final static char PERIODICAL_TYPE_1 = 's';
  private final static char PERIODICAL_TYPE_2 = 'i';

  private final static String DEFAULT_CREATOR = TmConfig.instance().getString("cdm.mets.defaultCreator", "");
  private final static String DEFAULT_ARCHIVIST = TmConfig.instance().getString("cdm.mets.defaultArchivist", "");

  private final static String OBJ_ID_FORMAT = "OBJ_%03d";
  private final static String EVT_ID_FORMAT = "EVT_%03d";
  private final static String AMD_SEC_ID_FORMAT = "PAGE_%04d";
  private final static String AGENT_ID_FORMAT = "AGENT_%03d";

  private final static String STRUCT_MAP_PHYSICAL_DIV_ID_FORMAT = "DIV_P_PAGE_%04d";
  //private final static String STRUCT_MAP_LOGICAL_DIV_ID_FORMAT = "ART_0001_%04d";

  public final static String FILE_GRP_ID_MC = "MC_IMGGRP";
  public final static String FILE_GRP_ID_UC = "UC_IMGGRP";
  public final static String FILE_GRP_ID_ALTO = "ALTOGRP";
  public final static String FILE_GRP_ID_TXT = "TXTGRP";
  public final static String FILE_GRP_ID_AMD = "TECHMDGRP";
  public final static String FILE_GRP_ID_ORIGINAL_DATA = "ORIGINAL_DATAGRP";

  public final static String FILE_ID_PREFIX_AMD = "AMD_";

  public final static String DOCUMENT_TYPE_K4 = "K4";
  public final static String DOCUMENT_LABEL_K4 = "Kramerius 4";

  public final static String IDENTIFIER_UUID = "uuid";

  public final static String PREMIS_PREFIX = "PREMIS_";

  public static final String MODS_ID_ISSUE = "MODS_ISSUE_";
  public static final String MODS_ID_SUPPLEMENT = "MODS_SUPPLEMENT_";

  public final static String AMD_METS_FILE_PREFIX = "AMD_METS_";

  private final MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();

  protected Namespace nsMets = new Namespace("mets", "http://www.loc.gov/METS/");
  protected Namespace nsPremis = new Namespace("premis", "info:lc/xmlns/premis-v2");
  protected Namespace nsXsi = new Namespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
  protected Namespace nsMix = new Namespace("mix", "http://www.loc.gov/mix/v20");

  public CDMMetsHelper() {
    mimeTypesMap.addMimeTypes(TmConfig.instance().getString("cdm.mimeTypesJp2"));
  }

  /**
   * Creates METS form CDM direcotry content.
   * 
   * @param fos
   *          Output is serialized there.
   * @param cdmId
   *          CDM ID
   * @throws METSException
   * @throws CDMException
   * @throws IOException
   * @throws ParserConfigurationException
   * @throws DocumentException
   */

  public METSWrapper exportToMETS(FileOutputStream fos, CDM cdm, String cdmId, Document mods) throws METSException, CDMException,
      IOException, SAXException, ParserConfigurationException, DocumentException {
    LOG.debug("Exporting to METS: " + cdmId);

    METSWrapper mw = new METSWrapper();
    METS mets = mw.getMETSObject();
    //String documentType = getDocumentTypeFromAleph(DocumentHelper.parseText(FileUtils.readFileToString(cdm.getAlephFile(cdmId), "UTF-8")));
    String documentType = getDocumentTypeFromAleph(DocumentHelper.parseText(retriedReadFileToString(cdm.getAlephFile(cdmId))));

    //setDocumentType(cdmId);

    // mets.setProfile("http://localhost/profiles/scientific-datasets-profile");
    mets.setType(documentType);
    cdm.updateProperty(cdmId, "documentType", documentType);
    mets.setLabel(getDocumentLabel(mods, cdmId));
    MetsHdr mh = mets.newMetsHdr();
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    String currentTime = df.format(cal.getTime());
    mh.setCreateDate(currentTime);
    mh.setLastModDate(currentTime);
    Agent agent = mh.newAgent();
    agent.setRole("CREATOR");
    agent.setType("ORGANIZATION");
    //agent.setName(getLibrarySigla(mods));
    String siglaFromAleph = getSiglaFromAleph(cdmId);
    agent.setName(siglaFromAleph);
    mh.addAgent(agent);
    Agent agent2 = mh.newAgent();
    agent2.setRole("ARCHIVIST");
    agent2.setType("ORGANIZATION");
    agent2.setName(siglaFromAleph);

    mh.addAgent(agent2);
    mets.setMetsHdr(mh);
    // add dmdSecs
    if (mods != null) {
      addDmdSecs(mw, cdm, cdmId, mods);
    }
    else {
      LOG.warn("MODS file not included");
    }
    // addn Dublin Core Record (DC)
    Document dcDoc = createDCElementFromMods(mods, false);
    Element dcEl = dcDoc.getDocumentElement();
    //Element dcEl = createDCElement(cdmId);

    addDocumentTypeToDc(dcEl, documentType, dcDoc);

    addDCSecs(mets, cdm, cdmId, dcEl);
    // add files
    addFileGroups(mets, null, cdm, cdmId, new DefaultDateCreatedStrategy());
    // add struct map
    addStructMap(mets, cdm, cdmId);
    // mw.validate();
    mw.write(fos);

    // generate METS for all images
    /*if (cdm.getEmConfigFile(cdmId).exists()) {
      String label = getDocumentLabel(mods, cdmId);
      Collection<File> ppFiles = FileUtils.listFiles(cdm.getPostprocessingDataDir(cdmId), FileFilterUtils.trueFileFilter(), FileFilterUtils.falseFileFilter());
      Collection<File> flatFiles = FileUtils.listFiles(cdm.getFlatDataDir(cdmId), FileFilterUtils.trueFileFilter(), FileFilterUtils.falseFileFilter());
      createMETSForImages(cdmId, label, cdm.getPostprocessingDataDir(cdmId), ppFiles, flatFiles);
    }*/
    return mw;
  }

  public void updateLastModDate(File metsFile) {
    METSWrapper mw;
    try {
      mw = new METSWrapper(XMLHelper.parseXML(metsFile));
    }
    catch (Exception ex) {
      LOG.error("Exception while parsing mets file!", ex);
      throw new SystemException("Exception while parsing mets file!", ex);
    }
    METS metsObject = mw.getMETSObject();
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    try {
      MetsHdr metsHdr = metsObject.getMetsHdr();
      metsHdr.setLastModDate(df.format(new Date()));
      metsObject.setMetsHdr(metsHdr);
    }
    catch (METSException e) {
      LOG.error("Exception while updating last mod date!", e);
      throw new SystemException("Exception while updating last mod date!", e);
    }

    FileOutputStream metsFileOutputStream = null;
    try {
      metsFileOutputStream = new FileOutputStream(metsFile);
      mw.write(metsFileOutputStream);
    }
    catch (FileNotFoundException e) {
      LOG.error("Exception while writing updated mets file!", e);
      throw new SystemException("Exception while writing updated mets file!", e);
    }
    finally {
      IOUtils.closeQuietly(metsFileOutputStream);
    }
  }

  public void createMnsMets(FileOutputStream fos, CDM cdm, String cdmId, Document mods) throws METSException, CDMException,
      IOException, SAXException, ParserConfigurationException, DocumentException {
    LOG.debug("Exporting to METS: " + cdmId);
    cdm.updateProperty(cdmId, "documentType", DOCUMENT_TYPE_MANUSCRIPTORIUM);

    METSWrapper mw = new METSWrapper();
    METS mets = mw.getMETSObject();

    //TODO ake su udaje v METSe pri manuskriptoriu?

    // Mets element attirbutes
    mets.setType(DOCUMENT_TYPE_MANUSCRIPTORIUM);
    mets.setLabel(DOCUMENT_LABEL_MANUSCRIPTORIUM);

    // Mets header
    MetsHdr mh = mets.newMetsHdr();
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    String currentTime = df.format(cal.getTime());
    mh.setCreateDate(currentTime);
    mh.setLastModDate(currentTime);
    Agent agent = mh.newAgent();
    agent.setRole("CREATOR");
    agent.setType("ORGANIZATION");
    agent.setName(TmConfig.instance().getString("meta.creator.name"));
    mh.addAgent(agent);
    Agent agent2 = mh.newAgent();
    agent2.setRole("ARCHIVIST");
    agent2.setType("ORGANIZATION");
    agent2.setName(TmConfig.instance().getString("meta.archivist.name"));
    mh.addAgent(agent2);
    mets.setMetsHdr(mh);

    // add dmdSecs
    if (mods != null) {
      addDmdSecs(mw, cdm, cdmId, mods);

    }
    else {
      LOG.warn("MODS file not included");
    }
    String label = getDocumentLabel(mods, cdmId);
    Collection<File> ppFiles = FileUtils.listFiles(cdm.getPostprocessingDataDir(cdmId), FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter());
    Collection<File> flatFiles = FileUtils.listFiles(cdm.getFlatDataDir(cdmId), FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter());

    createMETSForImages(cdmId, label, cdm.getPostprocessingDataDir(cdmId), ppFiles, flatFiles);
    addFileGroups(mets, null, cdm, cdmId, new DefaultDateCreatedStrategy());

    // Save
    mw.write(fos);
    IOUtils.closeQuietly(fos);
  }

  public void createK4Mets(FileOutputStream fos, CDM cdm, String cdmId, List<Document> modsList, String type) throws METSException, CDMException,
      IOException, SAXException, ParserConfigurationException, DocumentException, XPathExpressionException {
    LOG.debug("Exporting to METS: " + cdmId);
    cdm.updateProperty(cdmId, "documentType", type);

    METSWrapper mw = new METSWrapper();
    METS mets = mw.getMETSObject();
    mets.setType(type);

    //monograph,periodical header     
    mets.setLabel(getTitle(cdm, cdmId, modsList.get(0)));
    // Mets header
    MetsHdr mh = mets.newMetsHdr();
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // TODO to Zetko ve specifikaci neni
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    String currentTime = df.format(cal.getTime());
    mh.setCreateDate(currentTime);
    mh.setLastModDate(currentTime);
    // creator
    Agent agent = mh.newAgent();
    agent.setRole("CREATOR");
    agent.setType("ORGANIZATION");
    agent.setName(DEFAULT_CREATOR);
    mh.addAgent(agent);
    // archivist
    Agent agent2 = mh.newAgent();
    agent2.setRole("ARCHIVIST");
    agent2.setType("ORGANIZATION");
    String sigla = getDocumentSiglaForKrameriusImport(cdm, cdmId, modsList.get(0));
    agent2.setName(sigla);
    mh.addAgent(agent2);

    mets.setMetsHdr(mh);

    if (modsList != null) {
      addDmdSecsK4(mets, cdm, cdmId, modsList);
      List<Element> dcElementList = new ArrayList<Element>();

      for (Document mods : modsList) {
        // addn Dublin Core Record (DC)
        Document dcDoc = createDCElementFromMods(mods, true);
        Element dcEl = dcDoc.getDocumentElement();
        addDocumentTypeToDc(dcEl, type, dcDoc);
        dcElementList.add(dcEl);
      }

      addDCSecsK4(mets, cdm, cdmId, dcElementList);

//    addAmdSeck4(mets, null);
      File masterCopyTiff = new File(cdm.getWorkspaceDir(cdmId) + File.separator + "masterCopy_TIFF");
      String label = getDocumentLabel(modsList.get(0), cdmId);
      Collection<File> masterFiles = FileUtils.listFiles(masterCopyTiff, FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter());
      createMETSForImages(cdmId, label, masterCopyTiff, FileUtils.listFiles(masterCopyTiff, FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter()), masterFiles);

      addFileGroups(mets, null, cdm, cdmId, new DefaultDateCreatedStrategy());
    }
    else {
      LOG.warn("MODS file not included");
    }
    // Save
    mw.write(fos);
    IOUtils.closeQuietly(fos);

  }

  private Element createDCElement(String cdmId) {
    File inputFile = new CDM().getAlephFile(cdmId);
    Element el = null;
    try {
      Document dcDoc = CDMMarc2DC.transformAlephMarcToDC(inputFile);
      //Document dcDoc = CDMMarc2DC.transformAlephMarcToDC(inputFile);
      if (dcDoc != null) {
        el = dcDoc.getDocumentElement();
      }
    }
    catch (Exception e) { // FIXME tu sa strati vynimka!
      LOG.error("Error occured while parsing MARC212DC", e);
    }
    return el;
  }

  public Document createDCElementFromMods(Node modsNode, boolean isK4) {
    Document dcDoc = null;
    try {
      dcDoc = CDMMods2DC.transformMainModsToDC(modsNode, isK4);

      return dcDoc;
    }
    catch (Exception e) {
      LOG.error("Error occured while parsing MODS to dc", e);
      throw new SystemException("Error occured while parsing MODS to dc", e);
    }
  }

  private Element addDocumentTypeToDc(Element dc, String documentType, Document dcDoc) {

    Element e = dcDoc.createElement("dc:type");

    e.setTextContent("model:" + documentType);
    dc.appendChild(e);

    return dc;
  }

  private String getDocumentLabel(Document mods, String cdmId) throws CDMException, SAXException, IOException, ParserConfigurationException, METSException {
    checkNotNull(mods, "mods must not be null");
    return getDocumentLabel(mods.getDocumentElement(), cdmId);
  }

  public String getDocumentLabel(Element mods, String cdmId) throws CDMException, SAXException, IOException, ParserConfigurationException, METSException {
    checkNotNull(mods, "mods must not be null");
    //Element titleInfo = DOMUtil.getFirstChildElement(mods, "titleInfo");
    Element titleInfo = DOMUtil.getFirstChildElementNS(mods, "http://www.loc.gov/mods/v3", "titleInfo");
    //Element title = DOMUtil.getFirstChildElement(titleInfo, "title");
    Element title = DOMUtil.getFirstChildElementNS(titleInfo, "http://www.loc.gov/mods/v3", "title");
    //Element originInfo = DOMUtil.getFirstChildElement(mods, "originInfo");
    Element originInfo = DOMUtil.getFirstChildElementNS(mods, "http://www.loc.gov/mods/v3", "originInfo");
    //Element yearOfRelase = DOMUtil.getFirstChildElement(originInfo, "dateIssued");
    Element yearOfRelase = DOMUtil.getFirstChildElementNS(originInfo, "http://www.loc.gov/mods/v3", "dateIssued");
    if (yearOfRelase == null) {
      yearOfRelase = DOMUtil.getFirstChildElementNS(originInfo, "http://www.loc.gov/mods/v3", "dateCreated");
    }
    if (DOCUMENT_TYPE_MONOGRAPH.equals(getDocumentType(cdmId))) {
      checkNotNull(titleInfo, "titleInfo must not be null");
      checkNotNull(title, "title must not be null");
      checkNotNull(originInfo, "originInfo must not be null");
      checkNotNull(yearOfRelase, "yearOfRelase must not be null");
      return new String(title.getFirstChild().getTextContent() + ", " + yearOfRelase.getFirstChild().getTextContent());
    }
    else {
      // datum a cislo periodika sa prida v EM module (ATB-252)
      checkNotNull(titleInfo, "titleInfo must not be null");
      checkNotNull(title, "title must not be null");
      return new String(title.getFirstChild().getTextContent());
    }
  }

  private org.dom4j.Document getAlephDocument(InputStream input) throws DocumentException {

    SAXReader reader = new SAXReader();

    org.dom4j.Document marc21Doc = reader.read(input);//XMLHelper.parseXML(input);

    return marc21Doc;
  }

  public String getDocumentTypeFromAleph(InputStream input) throws IOException, TransformerException, SAXException, ParserConfigurationException, DocumentException {

    org.dom4j.Document alephDocument = getAlephDocument(input);

    //SAXReader reader = new SAXReader();

    //org.dom4j.Document marc21Doc = reader.read(input);//XMLHelper.parseXML(input);
    return getDocumentTypeFromAleph(alephDocument);

  }

  private String getSiglaFromAleph(String cdmId) throws FileNotFoundException, DocumentException {
    CDM cdm = new CDM();
    File alephFile = cdm.getAlephFile(cdmId);
    FileInputStream fileInputStream = null;
    try {
      fileInputStream = new FileInputStream(alephFile);
      org.dom4j.Document marc21Doc = getAlephDocument(fileInputStream);

      org.dom4j.Node node = marc21Doc.selectSingleNode("/oai_marc/varfield[@id='910']/subfield[@label='a']");

      if (node != null) {
        LOG.info("Sigla found in aleph document: " + node.getText());
        return node.getText();
      }
    }
    finally {
      IOUtils.closeQuietly(fileInputStream);
    }

    return null;
  }

  private String getDocumentTypeFromAleph(org.dom4j.Document marc21) {
    checkNotNull(marc21, "marc21 must not be null");

    org.dom4j.Node node;

    try {

      node = marc21.selectSingleNode("/oai_marc/fixfield[@id='LDR']");///selectSingleNode("/oai_marc/fixfield[@id='FMT']");
      if (node == null) {
        throw new RuntimeException("Cannot find node 'fixfield' with attribute 'id=fmt");
      }
    }
    catch (Exception e) {
      throw new SystemException("Document type resolving failed", ErrorCodes.GETTING_DOCUMENT_TYPE_ERROR);
    }

    String marc21Type = node.getText();
    String documentType;

//      Ad pouziti pole FMT
//      1. bylo by dobre se sjednotit na zkratkach
//      pro elektronicke zdroje ER nebo CF (jsem rovnez pro ER)
//      pro pokracujici zdroje SE nebo CR (tady bude zalezet na tom,
//      zda se bude pouzivat pouze pro serialy (pak by melo byt SE)
//      nebo pro serialy i integracni zdroje (pak by melo byt spise CR) 2. bylo by dobre se dohodnout na zpusobu pouziti
//      na MU odpovida pole FMT pouzite konfiguraci pro pole 008/
//      pozice 18-34 (viz MARC21. Bibliograficky format, Navesti-1,
//      druhy sloupecek u pozice 06)
//      tj. napr. hudebniny i zvukove zaznamy maji v poli FMT hodnotu MU
//      e-book ma v poli FMT hodnotu BK, e-serial hodnotu SE,
//      hodnotu ER pouzivame pouze pro pocitacovy software, ciselna
//      data, pocitacove orientovane multimedia, systemy on-line
//      nebo sitove sluzby.

    char type = marc21Type.charAt(7);
    if (MONOGRAPH_TYPE == type) {
      documentType = DOCUMENT_TYPE_MONOGRAPH;
    }
    else if (PERIODICAL_TYPE_1 == type || PERIODICAL_TYPE_2 == type) {
      documentType = DOCUMENT_TYPE_PERIODICAL;
    }
    else {
      throw new SystemException(format("Document type resolving failed for value '%s'.", marc21Type));
    }

    LOG.info("Document resolved as type '{}'", documentType);
    return documentType;
  }

  public String getDocumentType(String cdmId) throws CDMException, SAXException, IOException, ParserConfigurationException, METSException {
    CDM cdm = new CDM();
    String documentType = cdm.getCdmProperties(cdmId).getProperty("documentType");
    if (documentType != null) {
      return documentType;
    }
    Document metsDocument = XMLHelper.parseXML(cdm.getMetsFile(cdmId));
    METSWrapper mw = new METSWrapper(metsDocument);
    METS mets = mw.getMETSObject();

    return mets.getType();
  }

  public String getValueFormMets(String xPath, CDM cdm, String cdmId) {
    org.dom4j.Node node = getNodeFromMets(xPath, cdm, cdmId);
    if (node != null) {
      return node.getText();
    }
    return null;
  }

  public String getValueFormMets(String xPath, CDM cdm, File metsFile) {
    org.dom4j.Node node = getNodeFromMets(xPath, cdm, metsFile);
    if (node != null) {
      return node.getText();
    }
    return null;
  }

  public String getDocumentCreator(CDM cdm, String cdmId) {
    String xPath = "//mets:mets/mets:metsHdr/mets:agent[@ROLE='CREATOR']/mets:name/text()";
    org.dom4j.Node node = getNodeFromMets(xPath, cdm, cdmId);
    if (node != null) {
      return node.getText();
    }
    return null;
  }

  public String getDocumentAuthor(CDM cdm, String cdmId, String type) {
    String documentType;
    try {
      documentType = getDocumentType(cdmId);
    }
    catch (Exception e) {
      LOG.error("Error at getting document type, cdmId: " + cdmId + ", e: " + e.getMessage());
      throw new SystemException("Error at getting document type, cdmId: " + cdmId + ", e: " + e.getMessage(), ErrorCodes.GETTING_DOCUMENT_TYPE_ERROR);
    }
    String xPath;
    if ((type != null) && (type.length() > 0)) {
      xPath = "//mets:mets/mets:dmdSec[@ID=\"{type}\"]/mets:mdWrap/mets:xmlData/mods:mods/mods:name[1][@type='" + type + "']/mods:namePart";
    }
    else {
      xPath = "//mets:mets/mets:dmdSec[@ID=\"{type}\"]/mets:mdWrap/mets:xmlData/mods:mods/mods:name[1]/mods:namePart";
    }

    if (DOCUMENT_TYPE_MONOGRAPH.equals(documentType)) {
      xPath = xPath.replace("{type}", "MODSMD_VOLUME_0001");
    }
    else { //Issue or Suplement
      org.dom4j.Node issueNode = getNodeFromMets("//mods:mods[starts-with(@ID,'" + MODS_ID_ISSUE + "')]", cdm, cdmId);
      if (issueNode != null) {
        xPath = xPath.replace("{type}", "MODSMD_ISSUE_0001");
      }
      else
        xPath = xPath.replace("{type}", "MODS_SUPPLEMENT_0001");
    }

    String result = "";
    if ("K4".equals(cdm.getCdmProperties(cdmId).getProperty("importType"))) { // K4 all names
      List<org.dom4j.Node> nodes = getNodesFromMets(xPath + "[@type=\"family\"]/text()", cdm, cdmId);
      boolean firstGiven = true;
      if (nodes != null)
      {
        for (org.dom4j.Node node : nodes) {
          if (node != null) {
            result = result.isEmpty() ? node.getText() : result + " " + node.getText();
          }
        }
      }
      nodes = getNodesFromMets(xPath + "[@type=\"given\"]/text()", cdm, cdmId);
      if (nodes != null)
      {
        for (org.dom4j.Node node : nodes) {
          if (node != null) {
            if (!result.isEmpty()) {
              if (firstGiven) {
                result += ", ";
              }
              else {
                result += " ";
              }
            }
            result += node.getText();
            firstGiven = false;
          }
        }
      }

      if (result.isEmpty()) {
        org.dom4j.Node node = getNodeFromMets(xPath + "[not(@type=\"date\")]/text()", cdm, cdmId);
        if (node != null) {
          result = node.getText();
        }
      }
    }
    else//normal difitalization only first name
    {
      org.dom4j.Node node = getNodeFromMets(xPath + "[@type=\"family\"]/text()", cdm, cdmId);
      if (node != null) {
        result = node.getText();
      }
      node = getNodeFromMets(xPath + "[@type=\"given\"]/text()", cdm, cdmId);
      if (node != null) {
        if (!result.isEmpty()) {
          result += ", ";
        }
        result += node.getText();
      }

      if (result.isEmpty()) {
        node = getNodeFromMets(xPath + "[not(@type=\"date\")]/text()", cdm, cdmId);
        if (node != null) {
          result = node.getText();
        }
      }
    }

    if (result.isEmpty()) {
      LOG.debug("Method getDocument author retunr null. CdmId: " + cdmId);
    }
    return result;
  }

  public String getDateIssued(CDM cdm, String cdmId) {
    String documentType;
    try {
      documentType = getDocumentType(cdmId);
    }
    catch (Exception e) {
      LOG.error("Error at getting document type, cdmId: " + cdmId + ", e: " + e.getMessage());
      throw new SystemException("Error at getting document type, cdmId: " + cdmId + ", e: " + e.getMessage(), ErrorCodes.GETTING_DOCUMENT_TYPE_ERROR);
    }

    String xPath = "//mets:mets/mets:dmdSec[@ID=\"{type}\"]/mets:mdWrap/mets:xmlData/mods:mods/mods:originInfo/mods:dateIssued/text()";

    if (DOCUMENT_TYPE_MONOGRAPH.equals(documentType)) {
      xPath = xPath.replace("{type}", "MODSMD_VOLUME_0001");
    }
    else {
      xPath = xPath.replace("{type}", "MODSMD_TITLE_0001");
    }
    org.dom4j.Node node = getNodeFromMets(xPath, cdm, cdmId);
    if (node == null) {
      LOG.debug("Method getDateIssued return null. CdmId: " + cdmId);
      return "";
    }

    return node.getText();
  }

  public String getIssueNumber(CDM cdm, String cdmId) {
    String xPath = "//mets:mets/mets:dmdSec[@ID=\"MODSMD_ISSUE_0001\"]/mets:mdWrap/mets:xmlData/mods:mods/mods:titleInfo/mods:partNumber/text()";
    org.dom4j.Node node = getNodeFromMets(xPath, cdm, cdmId);
    if (node == null) {
      return "";
    }
    return node.getText();
  }

  public String getVolumeNumber(CDM cdm, String cdmId) {
    String xPath = "//mets:mets/mets:dmdSec[@ID=\"MODSMD_VOLUME_0001\"]/mets:mdWrap/mets:xmlData/mods:mods/mods:titleInfo/mods:partNumber/text()";
    org.dom4j.Node node = getNodeFromMets(xPath, cdm, cdmId);
    if (node == null) {
      return "";
    }
    return node.getText();
  }

  public String getTitle(CDM cdm, String cdmId, Document doc) throws CDMException, DocumentException {
    String sigla = null;
    sigla = getModsNodeValue("title", cdm, doc);
    if (sigla == null) {
      sigla = "";
    }
    return sigla;
  }

  public String getPartName(CDM cdm, String cdmId) {
    String xPath = "//mets:mets/mets:dmdSec[@ID=\"MODSMD_VOLUME_0001\"]/mets:mdWrap/mets:xmlData/mods:mods/mods:titleInfo/mods:partName/text()";
    org.dom4j.Node node = getNodeFromMets(xPath, cdm, cdmId);
    if (node == null) {
      return "";
    }
    return node.getText();
  }
  
  public String getTypeOfResource(CDM cdm, String cdmId) {
    String xPath = "//mets:mets/mets:dmdSec[@ID=\"MODSMD_VOLUME_0001\"]/mets:mdWrap/mets:xmlData/mods:mods/mods:typeOfResource/text()";
    org.dom4j.Node node = getNodeFromMets(xPath, cdm, cdmId);
    if (node == null) {
      return "";
    }
    return node.getText();
  }

  public String getVolumeDate(CDM cdm, String cdmId) {
    String xPath = "//mets:mets/mets:dmdSec[@ID=\"MODSMD_VOLUME_0001\"]/mets:mdWrap/mets:xmlData/mods:mods/mods:originInfo/mods:dateIssued/text()";
    org.dom4j.Node node = getNodeFromMets(xPath, cdm, cdmId);
    if (node == null) {
      return "";
    }
    return node.getText();
  }

  public void setDocumentType(String cdmId) throws CDMException, DocumentException, IOException, SAXException, ParserConfigurationException, METSException {
    CDM cdm = new CDM();
    //org.dom4j.Document documentAleph = DocumentHelper.parseText(FileUtils.readFileToString(cdm.getAlephFile(cdmId), "UTF-8"));
    org.dom4j.Document documentAleph = DocumentHelper.parseText(retriedReadFileToString(cdm.getAlephFile(cdmId)));
    File metsFile = cdm.getMetsFile(cdmId);
    Document metsDocument = XMLHelper.parseXML(metsFile);
    METSWrapper mw = new METSWrapper(metsDocument);
    METS mets = mw.getMETSObject();
    mets.setType(getDocumentTypeFromAleph(documentAleph));
    writeMetsWrapper(metsFile, mw);
    cdm.updateProperty(cdmId, "documentType", getDocumentTypeFromAleph(documentAleph));
  }

  /**
   * Creates METS form CDM direcotry content.
   * 
   * @param file
   *          Output is serialized there.
   * @param cdmId
   *          CDM ID
   * @throws METSException
   * @throws CDMException
   * @throws IOException
   * @throws ParserConfigurationException
   * @throws SAXException
   */

  public METSWrapper exportToMETS(File output, CDM cdm, String cdmId, Document mods) throws METSException, CDMException,
      IOException, SAXException, ParserConfigurationException {
    final FileOutputStream fos = new FileOutputStream(output);
    try {
      return exportToMETS(fos, cdm, cdmId, mods);
    }
    catch (Exception e) {
      LOG.warn("Export to MEST failed", e);
      throw new SystemException("Export to METS failed!", e);
    }
    finally {
      IOUtils.closeQuietly(fos);
    }
  }

  private void addStructMap(METS mets, CDM cdm, String cdmId) throws METSException {
    StructMap sm = mets.newStructMap();
    mets.addStructMap(sm);
    Div d = sm.newDiv();
    d.setType("investigation");
    d.setDmdID("J-1");
    sm.addDiv(d);
    Div d2 = d.newDiv();
    d2.setType("dataset");
    d2.setDmdID("J-2");
    d.addDiv(d2);
    Fptr fp = d2.newFptr();
    fp.setFileID("F-1");
    d2.addFptr(fp);
    Fptr fp2 = d2.newFptr();
    fp2.setFileID("F-2");
    d2.addFptr(fp2);
  }

  private void addPhysicalStructMap(METS mets, CDM cdm, String cdmId, Collection<EmCsvRecord> emRecords, boolean mainMets) throws METSException, DocumentException, CDMException, IOException, SAXException, ParserConfigurationException {
    if (emRecords == null || emRecords.size() < 1) {
      return;
    }
    // PHYSICAL
    StructMap sm = mets.newStructMap();
    if (mainMets) {
      sm.setLabel("Physical_Structure");
      sm.setType(CDMMetsHelper.STRUCT_MAP_TYPE_PHYSICAL);
      mets.addStructMap(sm);
      // 1 parent div
      Div pDiv = sm.newDiv();
      pDiv.setLabel(mets.getLabel());
      pDiv.setType(mets.getType());
      pDiv.setID("DIV_P_0000");
      pDiv.setDmdID(getSectionIdMods(cdmId));
      sm.addDiv(pDiv);
      // prepare map of file ids from existing mets
      Map<String, List<FileSecFile>> fileMap = getFileSecMap(mets);
      // div for every relation
      int i = 0;
      for (EmCsvRecord emCsvRecord : emRecords) {
        Div d = pDiv.newDiv();
        if (emCsvRecord.getPageType() != null) {
          d.setType(emCsvRecord.getPageType().toString());
        }
        d.setID(format(STRUCT_MAP_PHYSICAL_DIV_ID_FORMAT, i));
        if (emCsvRecord.getPageOrderLabel() != null) {
          d.setOrderLabel(emCsvRecord.getPageOrderLabel());
        }
        d.setOrder(new Integer(emCsvRecord.getPageOrder()).toString());
        pDiv.addDiv(d);
        FileSecFile fileSecFile;
        // 4 files

        // MC
        fileSecFile = getFile(fileMap.get(FILE_GRP_ID_MC), emCsvRecord.getPageId());
        if (fileSecFile != null) {
          addFileToMap(d, fileSecFile.getId());
        }

        // UC
        fileSecFile = getFile(fileMap.get(FILE_GRP_ID_UC), emCsvRecord.getPageId());
        if (fileSecFile != null) {
          addFileToMap(d, fileSecFile.getId());
        }

        // ALTO

        fileSecFile = getFile(fileMap.get(FILE_GRP_ID_ALTO), emCsvRecord.getPageId());
        if (fileSecFile != null) {
          addFileToMap(d, fileSecFile.getId());
        }

        // TXT
        fileSecFile = getFile(fileMap.get(FILE_GRP_ID_TXT), emCsvRecord.getPageId());
        if (fileSecFile != null) {
          addFileToMap(d, fileSecFile.getId());
        }

        // AMD
        fileSecFile = getFile(fileMap.get(FILE_GRP_ID_AMD), emCsvRecord.getPageId());
        if (fileSecFile != null) {
          addFileToMap(d, fileSecFile.getId());
        }
        i++;
      }

    }
    else { //physical map fpr amdSec has diffirent format
      sm.setType(CDMMetsHelper.STRUCT_MAP_TYPE_PHYSICAL);
      mets.addStructMap(sm);
      // 1 parent div
      Div pDiv = sm.newDiv();
      sm.addDiv(pDiv);
      // prepare map of file ids from existing mets
      Map<String, List<FileSecFile>> fileMap = getFileSecMap(mets);
      // div for every relation
      String pageType = mets.getType().equals(DOCUMENT_TYPE_PERIODICAL) ? PERIODICAL_PAGE : MONOGRAPH_PAGE;
      for (EmCsvRecord emCsvRecord : emRecords) {

        if (emCsvRecord.getPageType() != null) {
          pDiv.setType(pageType);
        }

        FileSecFile fileSecFile;
        // 4 files

        // MC
        fileSecFile = getFile(fileMap.get(FILE_GRP_ID_MC), emCsvRecord.getPageId());
        if (fileSecFile != null) {
          addFileToMap(pDiv, fileSecFile.getId());
        }

        // UC
        fileSecFile = getFile(fileMap.get(FILE_GRP_ID_UC), emCsvRecord.getPageId());
        if (fileSecFile != null) {
          addFileToMap(pDiv, fileSecFile.getId());
        }

        // ALTO

        fileSecFile = getFile(fileMap.get(FILE_GRP_ID_ALTO), emCsvRecord.getPageId());
        if (fileSecFile != null) {
          addFileToMap(pDiv, fileSecFile.getId());
        }

        // TXT
        fileSecFile = getFile(fileMap.get(FILE_GRP_ID_TXT), emCsvRecord.getPageId());
        if (fileSecFile != null) {
          addFileToMap(pDiv, fileSecFile.getId());
        }
      }
    }
  }

  private FileSecFile getFile(List<FileSecFile> fileList, String pageId) {
    FileSecFile metsFile = null;
    if (fileList != null && fileList.isEmpty() == false) {
      for (FileSecFile file : fileList) {
        // match: za pageId mozu nasledovat iba pripony (bodka + pismena)
        if (file.getUrl().matches(".*" + pageId + "(\\.[\\w]*)*")) {
          if (metsFile != null) {
            if (metsFile.getUrl().length() > file.getUrl().length()) { // Use the closest fileName to pageID
              metsFile = file;
            }
            //LOG.error("Can't select correct item for em record with pageId: " + pageId);
            //throw new SystemException("Can't select correct item for em record with pageId: " + pageId);
          }
          else {
            metsFile = file;
          }
        }
      }
      if (metsFile == null) {
        LOG.error("Can't select correct item for em record with pageId: " + pageId);
        throw new SystemException("Can't select correct item for em record with pageId: " + pageId, ErrorCodes.NO_ITEM_FOR_EM_RECORD);
      }
    }
    return metsFile;
  }

  private Fptr addFileToMap(Div d, String fileId) throws METSException {
    Fptr fp = null;
    if (fileId != null) {
      fp = d.newFptr();
      d.addFptr(fp);
      fp.setFileID(fileId);
//      Area area = fp.newArea();
//      area.setFileID(fileId);
//      fp.setArea(area);
    }
    return fp;
  }

  /**
   * Mapa obsahuje mapu pre fileGrp z METS-u. Klucom v mape je fileGrp ID (napr. ALTOGRP) a hodnota je list vsetkych
   * file v danej grupe. Vnutorny list obsahuje MetsFile objekty (pozri javaDoc v MetsFile). Vnutorny list je zoradeny
   * podla MetsFile objektov - pozri triedu MetsFile ako je implementovany compare.
   * 
   * @param mets
   * @return
   * @throws METSException
   */
  public Map<String, List<FileSecFile>> getFileSecMap(METS mets) throws METSException {
    Map<String, List<FileSecFile>> fileMap = new HashMap<String, List<FileSecFile>>();
    fileMap.put(FILE_GRP_ID_MC, new ArrayList<FileSecFile>());
    fileMap.put(FILE_GRP_ID_UC, new ArrayList<FileSecFile>());
    fileMap.put(FILE_GRP_ID_ALTO, new ArrayList<FileSecFile>());
    fileMap.put(FILE_GRP_ID_TXT, new ArrayList<FileSecFile>());
    fileMap.put(FILE_GRP_ID_AMD, new ArrayList<FileSecFile>());
    List<FileGrp> fileGrps = mets.getFileSec().getFileGrps();
    for (FileGrp fileGrp : fileGrps) {
      if (fileMap.containsKey(fileGrp.getID())) {
        for (au.edu.apsr.mtk.base.File file : fileGrp.getFiles()) {
          for (FLocat fLocat : file.getFLocats()) {
            List<FileSecFile> list = fileMap.get(fileGrp.getID());
            list.add(new FileSecFile(file.getID(), fLocat.getHref(), Integer.valueOf(file.getSeq())));
          }
        }
      }
    }
    for (List<FileSecFile> fileList : fileMap.values()) {
      Collections.sort(fileList);
    }
    return fileMap;
  }

  private void addLogicalStructMapMonograph(METS mets, CDM cdm, String cdmId) throws METSException, DocumentException, CDMException, IOException, SAXException, ParserConfigurationException {
    StructMap sm = mets.newStructMap();
    sm.setLabel("Logical_Structure");
    sm.setType(CDMMetsHelper.STRUCT_MAP_TYPE_LOGICAL);
    mets.addStructMap(sm);
    // 1 parent div (monograph)
    Div pDiv = sm.newDiv();
    pDiv.setLabel(mets.getLabel());
    pDiv.setType(mets.getType());
    pDiv.setID("MONOGRAPH_0001");

    sm.addDiv(pDiv);
    // 1 main div for VOLUME
    Div vDiv = pDiv.newDiv();
    vDiv.setLabel(mets.getLabel());
    vDiv.setType("VOLUME");
    vDiv.setID("VOLUME_0001");
    vDiv.setDmdID(getSectionIdMods(cdmId));
    pDiv.addDiv(vDiv);

//TODO vieme SUPPLEMENT pre monografiu?
//    // 1 main div for SUPPLEMENT
//    Div sDiv = pDiv.newDiv();
//    sDiv.setLabel(mets.getLabel());
//    sDiv.setType("SUPPLEMENT");
//    sDiv.setID("SUPPLEMENT_0001");
//    sDiv.setDmdID(DMDSEC_ID_PREFIX_MODSMD_SUPPL+"_0001");
//    sDiv.setDmdID(getSectionIdMods(cdmId));
//    pDiv.addDiv(sDiv);

    StructLink structLink = mets.newStructLink();
    SmLink smLink;

    List<StructMap> structMapList = mets.getStructMapByType(STRUCT_MAP_TYPE_PHYSICAL);
    if (structMapList == null || structMapList.size() != 1 || structMapList.get(0) == null || structMapList.get(0).getDivs() == null || structMapList.get(0).getDivs().size() != 1) {
      throw new SystemException("Incorrect format of METS file. There should be one structMap with type PHYSICAL. This structMap should contains one main div and several sub divs with fptr.", ErrorCodes.WRONG_METS_FORMAT);
    }
    Div mainDiv = structMapList.get(0).getDivs().get(0);
    for (Div div : mainDiv.getDivs()) {
      smLink = structLink.newSmLink();
      smLink.setFrom("VOLUME_0001");
      smLink.setTo(div.getID());
      structLink.getSmLinks().add(smLink);
      structLink.addSmLink(smLink);
    }
    mets.setStructLink(structLink);
  }

  private void addLogicalStructMapMultipartMonograph(METS mets, CDM cdm, String cdmId) throws METSException, DocumentException, CDMException, IOException, SAXException, ParserConfigurationException {
    StructMap sm = mets.newStructMap();
    sm.setLabel("Logical_Structure");
    sm.setType(CDMMetsHelper.STRUCT_MAP_TYPE_LOGICAL);
    mets.addStructMap(sm);
    // 1 parent div (monograph)
    Div pDiv = sm.newDiv();
    pDiv.setLabel(mets.getLabel());
    pDiv.setType(mets.getType());
    pDiv.setDmdID("MODSMD_TITLE_0001");
    pDiv.setID("MONOGRAPH_0001");

    sm.addDiv(pDiv);
    // 1 main div for VOLUME
    Div vDiv = pDiv.newDiv();
    vDiv.setLabel(mets.getLabel());
    vDiv.setType("VOLUME");
    vDiv.setID("VOLUME_0001");
    vDiv.setDmdID(getSectionIdMods(cdmId));
    pDiv.addDiv(vDiv);

    StructLink structLink = mets.newStructLink();
    SmLink smLink;

    List<StructMap> structMapList = mets.getStructMapByType(STRUCT_MAP_TYPE_PHYSICAL);
    if (structMapList == null || structMapList.size() != 1 || structMapList.get(0) == null || structMapList.get(0).getDivs() == null || structMapList.get(0).getDivs().size() != 1) {
      throw new SystemException("Incorrect format of METS file. There should be one structMap with type PHYSICAL. This structMap should contains one main div and several sub divs with fptr.", ErrorCodes.WRONG_METS_FORMAT);
    }
    Div mainDiv = structMapList.get(0).getDivs().get(0);
    for (Div div : mainDiv.getDivs()) {
      smLink = structLink.newSmLink();
      smLink.setFrom("VOLUME_0001");
      smLink.setTo(div.getID());
      structLink.getSmLinks().add(smLink);
      structLink.addSmLink(smLink);
    }
    mets.setStructLink(structLink);
  }

  private void addLogicalStructMapPeriodical(METS mets, CDM cdm, String cdmId) throws METSException, DocumentException, CDMException, IOException, SAXException, ParserConfigurationException {

    String type = null; //issue or supplement
    //org.dom4j.Document modsDoc = DocumentHelper.createDocument();
    SAXReader reader = new SAXReader();
    org.dom4j.Document metsDocument;
    try {
      metsDocument = reader.read(cdm.getMetsFile(cdmId));
      XPath xPath = DocumentHelper.createXPath("//*[starts-with(@ID,'MODS_ISSUE_')]");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
      org.dom4j.Node node = xPath.selectSingleNode(metsDocument);
      if (node != null) {
        type = "ISSUE";
      }
      else {
        metsDocument = reader.read(cdm.getMetsFile(cdmId));
        xPath = DocumentHelper.createXPath("//*[starts-with(@ID,'MODS_SUPPL')]");
        xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
        node = xPath.selectSingleNode(metsDocument);
        if (node != null) {
          type = "SUPPLEMENT";
        }
        else {
          throw new SystemException("MODS must contain ISSUE or SUPPLEMENT", ErrorCodes.WRONG_METS_FORMAT_PERIODICUM);
        }
      }
    }
    catch (Exception e) {
      throw new SystemException("Error while retrieving MODS from METS file.", ErrorCodes.MODS_FROM_METS_ERROR);
    }

    StructMap sm = mets.newStructMap();
    sm.setLabel("Logical_Structure");
    sm.setType(CDMMetsHelper.STRUCT_MAP_TYPE_LOGICAL);
    mets.addStructMap(sm);
    // 1 parent div (monograph)
    Div pDiv = sm.newDiv();
    pDiv.setLabel(mets.getLabel());
    pDiv.setType("PERIODICAL_TITLE");
    pDiv.setID("TITLE_0001");
    pDiv.setDmdID("MODSMD_TITLE_0001");

    sm.addDiv(pDiv);
    // 1 main div for VOLUME
    Div vDiv = pDiv.newDiv();
    vDiv.setLabel(mets.getLabel());
    vDiv.setType("PERIODICAL_VOLUME");
    vDiv.setID("VOLUME_0001");
    vDiv.setDmdID("MODSMD_VOLUME_0001");
    pDiv.addDiv(vDiv);

    Div typeDiv = vDiv.newDiv();
    typeDiv.setLabel(mets.getLabel());
    typeDiv.setType(type);
    typeDiv.setID(type + "_0001");
    typeDiv.setDmdID("MODSMD_" + type + "_0001");

    vDiv.addDiv(typeDiv);

    StructLink structLink = mets.newStructLink();
    SmLink smLink;

    List<StructMap> structMapList = mets.getStructMapByType(STRUCT_MAP_TYPE_PHYSICAL);
    if (structMapList == null || structMapList.size() != 1 || structMapList.get(0) == null || structMapList.get(0).getDivs() == null || structMapList.get(0).getDivs().size() != 1) {
      throw new SystemException("Incorrect format of METS file. There should be one structMap with type PHYSICAL. This structMap should contains one main div and several sub divs with fptr.");
    }
    //String dmdId = null;
    Div mainDiv = structMapList.get(0).getDivs().get(0);
    for (Div div : mainDiv.getDivs()) {

      smLink = structLink.newSmLink();
      smLink.setFrom(type + "_1");
      smLink.setTo(div.getID());
      structLink.getSmLinks().add(smLink);
      structLink.addSmLink(smLink);
    }
    mets.setStructLink(structLink);
  }

  public void addPhysicalStructMap(File metsFile, CDM cdm, String cdmId, Collection<EmCsvRecord> emRecords, boolean mainMets) throws SAXException, IOException, ParserConfigurationException, METSException, DocumentException {
    LOG.debug("Adding physical structure map from cdm " + cdmId + ", METS " + metsFile.getName());
    Document metsDocument = XMLHelper.parseXML(metsFile);
    METSWrapper mw = new METSWrapper(metsDocument);
    METS mets = mw.getMETSObject();
    addPhysicalStructMap(mets, cdm, cdmId, emRecords, mainMets);
    writeMetsWrapper(metsFile, mw);
    LOG.debug("Structure added");
  }

  public void addLogicalStructMap(File metsFile, CDM cdm, String cdmId) throws SAXException, IOException, ParserConfigurationException, METSException, DocumentException {
    LOG.debug("Adding logical structure map from cdm " + cdmId + ", METS " + metsFile.getName());
    Document metsDocument = XMLHelper.parseXML(metsFile);
    METSWrapper mw = new METSWrapper(metsDocument);
    METS mets = mw.getMETSObject();
    if (isMultiPartMonograph(cdmId)) {
      addLogicalStructMapMultipartMonograph(mets, cdm, cdmId);
    }
    else {
      if (getDocumentType(cdmId).equals(DOCUMENT_TYPE_MONOGRAPH)) {
        addLogicalStructMapMonograph(mets, cdm, cdmId);
      }
      else {
        addLogicalStructMapPeriodical(mets, cdm, cdmId);
      }
    }
    writeMetsWrapper(metsFile, mw);
    LOG.debug("Structure added");
  }

  @RetryOnFailure
  private org.dom4j.Document getMetsDocumentFromFile(File file) throws DocumentException {
    SAXReader reader = new SAXReader();

    return reader.read(file);
  }

  public void removeStructs(File metsFile, List<String> types) throws DocumentException, IOException {
    // Gods knows why there is not a removeStructMap method on METS class -> using Xpath

    Namespace nsMets = new Namespace("mets", "http://www.loc.gov/METS/");

    LOG.debug(metsFile.getName());
    LOG.debug("Removing structure map from METS " + metsFile.getName() + ", types: " + types);

    org.dom4j.Document metsDocument = getMetsDocumentFromFile(metsFile);

    LOG.debug(metsDocument.getXMLEncoding());
    if (types != null && types.size() > 0) {
      for (String type : types) {
        XPath xPath = metsDocument.createXPath("//mets:mets/mets:structMap[@TYPE='" + type + "']");
        xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mets", nsMets.getStringValue()));
        org.dom4j.Element e = (org.dom4j.Element) xPath.selectSingleNode(metsDocument);
        if (e != null) {
          LOG.debug(e.getName());
          e.detach();
        }
      }
    }
    else {
      // remove all
      XPath xPath = metsDocument.createXPath("//mets:mets/mets:structMap");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mets", nsMets.getStringValue()));
      @SuppressWarnings("rawtypes")
      List nodes = xPath.selectNodes(metsDocument);
      if (nodes != null) {
        for (Object object : nodes) {
          org.dom4j.Element e = (org.dom4j.Element) object;
          if (e != null) {
            LOG.debug(e.getName());
            e.detach();
          }
        }
      }

      xPath = metsDocument.createXPath("//mets:mets/mets:structLink");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mets", nsMets.getStringValue()));
      @SuppressWarnings("rawtypes")
      List nodesStructLink = xPath.selectNodes(metsDocument);
      if (nodesStructLink != null) {
        for (Object object : nodesStructLink) {
          org.dom4j.Element e = (org.dom4j.Element) object;
          if (e != null) {
            LOG.debug(e.getName());
            e.detach();
          }
        }
      }
    }

    writeToFile(metsDocument, metsFile);

    LOG.debug("Structure removed");
  }

  public void addDmdSecs(METSWrapper mw, Document mods, String dmdIdType) throws ParserConfigurationException, METSException, SAXException, IOException, CDMException, DocumentException {
    METS mets = mw.getMETSObject();
    DmdSec dmd = mets.newDmdSec();
    dmd.setID(dmdIdType);

    MdWrap mdw = dmd.newMdWrap();
    mdw.setMDType("MODS");
    mdw.setMIMEType("text/xml");
    mdw.setXmlData(mods.getDocumentElement());
    dmd.setMdWrap(mdw);
    mets.addDmdSec(dmd);
  }

  private void addDmdSecs(METSWrapper mw, CDM cdm, String cdmId, Document mods) throws ParserConfigurationException, METSException, SAXException, IOException, CDMException, DocumentException {

    if (DOCUMENT_TYPE_MONOGRAPH.equals(getDocumentType(cdmId))) {
      addDmdSecs(mw, mods, DMDSEC_ID_MODS_VOLUME);
    }
    else {
      addDmdSecs(mw, mods, DMDSEC_ID_MODS_TITLE);
    }

  }

  private void addDmdSecsK4(METS mets, CDM cdm, String cdmId, List<Document> modsList) throws ParserConfigurationException, METSException, SAXException, IOException, CDMException, DocumentException {
    if (DOCUMENT_TYPE_MONOGRAPH.equals(getDocumentType(cdmId))) {
      DmdSec dmd = mets.newDmdSec();
      dmd.setID(DMDSEC_ID_MODS_VOLUME);
      MdWrap mdw = dmd.newMdWrap();
      mdw.setMDType("MODS");
      mdw.setMIMEType("text/xml");
      mdw.setXmlData(modsList.get(0).getDocumentElement());
      dmd.setMdWrap(mdw);
      mets.addDmdSec(dmd);
    }
    else {
      DmdSec dmdTitle = mets.newDmdSec();
      dmdTitle.setID(DMDSEC_ID_MODS_TITLE);
      MdWrap mdwTitle = dmdTitle.newMdWrap();
      mdwTitle.setMDType("MODS");
      mdwTitle.setMIMEType("text/xml");
      mdwTitle.setXmlData(modsList.get(0).getDocumentElement());
      dmdTitle.setMdWrap(mdwTitle);
      mets.addDmdSec(dmdTitle);

      DmdSec dmdVolume = mets.newDmdSec();
      dmdVolume.setID(DMDSEC_ID_MODS_VOLUME);
      MdWrap mdwVOlume = dmdVolume.newMdWrap();
      mdwVOlume.setMDType("MODS");
      mdwVOlume.setMIMEType("text/xml");
      mdwVOlume.setXmlData(modsList.get(1).getDocumentElement());
      dmdVolume.setMdWrap(mdwVOlume);
      mets.addDmdSec(dmdVolume);

      DmdSec dmdIssue = mets.newDmdSec();
      dmdIssue.setID(DMDSEC_ID_MODS_ISSUE);
      MdWrap mdwIssue = dmdIssue.newMdWrap();
      mdwIssue.setMDType("MODS");
      mdwIssue.setMIMEType("text/xml");
      mdwIssue.setXmlData(modsList.get(2).getDocumentElement());
      dmdIssue.setMdWrap(mdwIssue);
      mets.addDmdSec(dmdIssue);
    }

  }

  public void removeDmdSec(File metsFile, String id) throws ParserConfigurationException, METSException, FileNotFoundException, SAXException, IOException {
    LOG.debug("Removing section " + id + " from METS file " + metsFile.getName());
    Document metsDocument = XMLHelper.parseXML(metsFile);
    METSWrapper mw = new METSWrapper(metsDocument);
    METS mets = mw.getMETSObject();
    mets.removeDmdSec(id);
    writeMetsWrapper(metsFile, mw);
    LOG.debug("Section removed");
  }

  public void removeDmdSec(File metsFile, List<String> ids) throws ParserConfigurationException, METSException, FileNotFoundException, SAXException, IOException {
    Document metsDocument = XMLHelper.parseXML(metsFile);
    METSWrapper mw = new METSWrapper(metsDocument);
    METS mets = mw.getMETSObject();
    for (String id : ids) {
      LOG.debug("Removing section " + id + " from METS file " + metsFile.getName());
      mets.removeDmdSec(id);
    }
    writeMetsWrapper(metsFile, mw);
    LOG.debug("Section removed");
  }

  public void writeMetsWrapper(File metsFile, METSWrapper mw) throws IOException {
    final FileOutputStream fos = new FileOutputStream(metsFile);
    try {
      mw.write(fos);
    }
    finally {
      IOUtils.closeQuietly(fos);
    }
  }

  public void prettyPrint(File metsFile) {
    XMLHelper.pretyPrint(metsFile, true);
  }

  @SuppressWarnings("unchecked")
  public List<String> getDmdSecsIds(File metsFile) throws DocumentException {

    Namespace nsMets = new Namespace("mets", "http://www.loc.gov/METS/");

    LOG.debug(metsFile.getName());
    LOG.debug("Geting dmd section IDs from METS " + metsFile.getName());

    SAXReader reader = new SAXReader();

    org.dom4j.Document metsDocument = reader.read(metsFile);
    LOG.debug(metsDocument.getXMLEncoding());
    XPath xPath = metsDocument.createXPath("//mets:mets/mets:dmdSec/@ID");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mets", nsMets.getStringValue()));
    List<Attribute> attributes = xPath.selectNodes(metsDocument);
    List<String> result = new ArrayList<String>();
    for (Attribute a : attributes) {
      result.add(a.getValue());
    }
    return result;
  }

  public Node getMainMODS(CDM cdm, String cdmId) throws METSException, CDMException, SAXException, IOException, ParserConfigurationException, DocumentException {
    return getDmdSec(cdm.getMetsFile(cdmId), getSectionIdMods(cdmId));
  }

  public Node getMainDC(CDM cdm, String cdmId) throws METSException, CDMException, SAXException, IOException, ParserConfigurationException, DocumentException {
    return getDmdSec(cdm.getMetsFile(cdmId), getSectionIdDC(cdmId));
  }

  public METS getMetsObject(CDM cdm, String cdmId) throws CDMException, SAXException, IOException, ParserConfigurationException, METSException {
    Document metsDocument = XMLHelper.parseXML(cdm.getMetsFile(cdmId));
    METSWrapper mw = new METSWrapper(metsDocument);
    return mw.getMETSObject();
  }

  public String getSectionIdDC(String cdmId) throws CDMException, DocumentException, IOException, SAXException, ParserConfigurationException, METSException
  {
    CDM cdm = new CDM();
    if ("K4".equals(cdm.getCdmProperties(cdmId).getProperty("importType"))) {
      return DOCUMENT_TYPE_MONOGRAPH.equals(getDocumentType(cdmId))
          ? DMDSEC_ID_DC_VOLUME
          : DMDSEC_ID_DC_ISSUE;
    }
    return DOCUMENT_TYPE_MONOGRAPH.equals(getDocumentType(cdmId))
        ? DMDSEC_ID_DC_VOLUME
        : DMDSEC_ID_DC_TITLE;
  }

  public String getSectionIdMods(String cdmId) throws CDMException, DocumentException, IOException, SAXException, ParserConfigurationException, METSException
  {
    CDM cdm = new CDM();
    if ("K4".equals(cdm.getCdmProperties(cdmId).getProperty("importType"))) {
      return DOCUMENT_TYPE_MONOGRAPH.equals(getDocumentType(cdmId))
          ? DMDSEC_ID_MODS_VOLUME
          : DMDSEC_ID_MODS_ISSUE;
    }
    return DOCUMENT_TYPE_MONOGRAPH.equals(getDocumentType(cdmId))
        ? DMDSEC_ID_MODS_VOLUME
        : DMDSEC_ID_MODS_TITLE;
  }

  public Node getDmdSec(File metsFile, String sectionId) throws METSException, SAXException, IOException, ParserConfigurationException {
    LOG.debug("Getting section " + sectionId + " from mets " + metsFile.getName());
    Document metsDocument = XMLHelper.parseXML(metsFile);
    METSWrapper mw = new METSWrapper(metsDocument);
    METS mets = mw.getMETSObject();

    Node sectionWrapper;
    DmdSec dmdSec = mets.getDmdSec(sectionId);
    if (dmdSec != null && dmdSec.getXmlData() != null)
    {
      sectionWrapper = dmdSec.getXmlData();
    }
    else
    {
      return null;
    }
    return DOMUtil.getFirstChildElement(sectionWrapper);
  }

  public Node getDmdSec(CDM cdm, String cdmId, String sectionId) throws METSException, SAXException, IOException, ParserConfigurationException {
    return getDmdSec(cdm.getMetsFile(cdmId), sectionId);
  }

  public boolean renameDmdSec(File metsFile, String oldSectionId, String newSectionId) throws METSException, SAXException, IOException, ParserConfigurationException {
    LOG.debug("Getting section " + oldSectionId + " from mets " + metsFile.getName());
    Document metsDocument = XMLHelper.parseXML(metsFile);
    METSWrapper mw = new METSWrapper(metsDocument);
    METS mets = mw.getMETSObject();

    DmdSec dmdSec = mets.getDmdSec(oldSectionId);
    if (dmdSec == null) {
      return false;
    }
    dmdSec.setID(newSectionId);

    //there is just one issue per intelectual entity, so rename it too  
    if (oldSectionId.startsWith(DMDSEC_ID_PREFIX_MODSMD_ISSUE)) {
      LOG.debug("Going to rebame MODS issue id");
      Node sectionWrapper = dmdSec.getXmlData();
      if (sectionWrapper != null) {
        Element mods = DOMUtil.getFirstChildElement(sectionWrapper);
        mods.setAttribute("ID", "MODS_ISSUE_0001");
      }
    }

    writeMetsWrapper(metsFile, mw);
    return true;
  }

  public static org.dom4j.Document parse(org.w3c.dom.Document doc) throws Exception {
    if (doc == null) {
      return (null);
    }
    org.dom4j.io.DOMReader xmlReader = new org.dom4j.io.DOMReader();
    return (xmlReader.read(doc));
  }

  public void removeFileSec(File metsFile) throws ParserConfigurationException, METSException, FileNotFoundException, SAXException, IOException {
    LOG.debug("Removing file section from METS file " + metsFile.getName());
    Document metsDocument = XMLHelper.parseXML(metsFile);
    METSWrapper mw = new METSWrapper(metsDocument);
    METS mets = mw.getMETSObject();
    mets.removeFileSec();
    writeMetsWrapper(metsFile, mw);
    LOG.debug("Section removed");
  }

  public FileSec addFileGroups(METS mets, FileSec fileSec, CDM cdm, String cdmId, DateCreatedStrategy dateCreatedStrategy) throws METSException, CDMException, IOException {
    final File rootDir = cdm.getCdmDataDir(cdmId);
    FileSec fs = null;
    if (fileSec != null) {
      fs = fileSec;
    }
    else {
      fs = mets.newFileSec();
    }
    addFiles(fs, mets, cdm.getMasterCopyDir(cdmId), null, rootDir, FILE_GRP_ID_MC, "Images", "MC_", cdmId, dateCreatedStrategy);
    addFiles(fs, mets, cdm.getUserCopyDir(cdmId), null, rootDir, FILE_GRP_ID_UC, "Images", "UC_", cdmId, dateCreatedStrategy);
    addFiles(fs, mets, cdm.getAltoDir(cdmId), null, rootDir, FILE_GRP_ID_ALTO, "Layout", "ALTO_", cdmId, dateCreatedStrategy);
    addFiles(fs, mets, cdm.getTxtDir(cdmId), null, rootDir, FILE_GRP_ID_TXT, "Text", "TXT_", cdmId, dateCreatedStrategy);
    addFiles(fs, mets, cdm.getAmdDir(cdmId), null, rootDir, FILE_GRP_ID_AMD, "Technical Metadata", FILE_ID_PREFIX_AMD, cdmId, dateCreatedStrategy);
    //if (cdm.getOriginalDataDir(cdmId).exists()) {
    //  addFiles(fs, mets, cdm.getOriginalDataDir(cdmId), null, rootDir, FILE_GRP_ID_ORIGINAL_DATA, "Original Data", "ORIGINAL_DATA_", cdmId, dateCreatedStrategy);
    //}
    mets.setFileSec(fs);
    return fs;
  }

  public void addAmdSecFileGroups(FileSec fileSec, METS mets, File metsFile, String cdmId, File dir, String groupId, String groupUse, String idPrefix, int counter, DateCreatedStrategy dateCreatedStrategy) throws METSException, IOException {
    CDM cdm = new CDM();
    File rootDir = cdm.getCdmDataDir(cdmId);
    FileSec fs = null;
    if (fileSec != null) {
      fs = fileSec;
    }
    else {
      fs = mets.newFileSec();
    }

    LOG.info("Adding files to file group in amdSec " + groupId);
    final FileGrp fg = fs.newFileGrp();
    fg.setUse(groupUse);
    fg.setID(groupId);
    // list files form dir
    IOFileFilter fileFilter = FileFilterUtils.trueFileFilter();

    final IOFileFilter dirFilter = FileFilterUtils.falseFileFilter();
    final List<File> files = new ArrayList<File>(FileUtils.listFiles(dir, fileFilter, dirFilter));
    // setridi soubory dle jmena; poradi souboru v METS vychazi z tohoto poradi
    Collections.sort(files, new Comparator<File>() {
      public int compare(File f1, final File f2) {
        return f1.getName().compareTo(f2.getName());
      }
    });
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    // add files to mets

    HashMap<String, au.edu.apsr.mtk.base.File> fileSecFilesmap = fileSecFilesMap(cdmId);
    for (File oneFile : files) {
      File parent = new File(oneFile.getParent());
      String oneFileHref = parent.getName() + "/" + oneFile.getName();
      StringBuilder sb = new StringBuilder(oneFile.getName());
      String nameBase;
      String prefix = sb.toString();
      LOG.info("amdSec prefix: " + prefix);
      if (sb.indexOf("_") != -1) {
        prefix = sb.substring(0, sb.indexOf("_"));
      }
      try { // najdem subor v amdSec ktory patri k danemu suboru
        Double.parseDouble(prefix);
        nameBase = sb.substring(0, sb.indexOf("."));
      }
      catch (Exception e) {
        nameBase = sb.substring(sb.indexOf("_") + 1, sb.indexOf("."));
      }
      LOG.info("amdSec nameBase: " + nameBase);
      if (!FilenameUtils.getBaseName(metsFile.getName()).endsWith(nameBase))
        continue;
      au.edu.apsr.mtk.base.File f = fg.newFile();

      au.edu.apsr.mtk.base.File fileSecFile = fileSecFilesmap.get(oneFileHref);
      if (fileSecFile != null) {
        f.setID(fileSecFile.getID());
      }
      else {
        //migrace formatu
        f.setID(oneFileHref);
      }

      f.setSize(FileUtils.sizeOf(oneFile));
      f.setMIMEType(mimeTypesMap.getContentType(oneFile));
      // f.setOwnerID("de.tar.bz.0");
      f.setChecksumType("MD5");
      f.setChecksum(getMD5Checksum(oneFile));
      f.setSeq(String.valueOf(counter - 1));
      f.setCreated(df.format(dateCreatedStrategy.getTimeCreated(cdmId, oneFile, dir.getName())));
      FLocat fl = f.newFLocat();
      fl.setHref(getRelativePath(oneFile, rootDir));
      fl.setLocType("URL");
      f.addFLocat(fl);
      fg.addFile(f);
    }
    fs.addFileGrp(fg);
    mets.setFileSec(fs);
  }

  private HashMap<String, au.edu.apsr.mtk.base.File> fileSecFilesMap(String cdmId) {
    CDM cdm = new CDM();
    Document metsDocument;
    METSWrapper mw = null;
    HashMap<String, au.edu.apsr.mtk.base.File> files = null;
    try {
      metsDocument = XMLHelper.parseXML(cdm.getMetsFile(cdmId));
      mw = new METSWrapper(metsDocument);
      METS mets = mw.getMETSObject();
      List<FileGrp> fileGrps = mets.getFileSec().getFileGrps();
      files = new HashMap<String, au.edu.apsr.mtk.base.File>();
      for (FileGrp fileGrp : fileGrps) {
        for (au.edu.apsr.mtk.base.File file : fileGrp.getFiles()) {
          files.put(file.getFLocats().get(0).getHref(), file);
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return files;
  }

  public boolean addFileGroups(File metsFile, CDM cdm, String cdmId, int counter) throws SAXException, IOException, ParserConfigurationException, METSException {
    return addFileGroups(metsFile, cdm, cdmId, counter, new DefaultDateCreatedStrategy());
  }

  public boolean addFileGroups(File metsFile, CDM cdm, String cdmId, int counter, DateCreatedStrategy dateCreatedStrategy) throws SAXException, IOException, ParserConfigurationException, METSException {
    LOG.debug("Adding file groups to METS file " + metsFile.getName());
    Document metsDocument = XMLHelper.parseXML(metsFile);
    METSWrapper mw = new METSWrapper(metsDocument);
    METS mets = mw.getMETSObject();
    boolean amdMets = true;
    if (metsFile.getName().contains(AMD_METS_FILE_PREFIX)) {

      FileSec fileSec = mets.newFileSec();
      addAmdSecFileGroups(fileSec, mets, metsFile, cdmId, cdm.getMasterCopyDir(cdmId), FILE_GRP_ID_MC, "Images", "MC_", counter, dateCreatedStrategy);
      addAmdSecFileGroups(fileSec, mets, metsFile, cdmId, cdm.getUserCopyDir(cdmId), FILE_GRP_ID_UC, "Images", "UC_", counter, dateCreatedStrategy);
      addAmdSecFileGroups(fileSec, mets, metsFile, cdmId, cdm.getAltoDir(cdmId), FILE_GRP_ID_ALTO, "Layout", "ALTO_", counter, dateCreatedStrategy);
      addAmdSecFileGroups(fileSec, mets, metsFile, cdmId, cdm.getTxtDir(cdmId), FILE_GRP_ID_TXT, "Text", "TXT_", counter, dateCreatedStrategy);
    }
    else {
      addFileGroups(mets, null, cdm, cdmId, dateCreatedStrategy);
      amdMets = false;
    }
    writeMetsWrapper(metsFile, mw);
    LOG.debug("Section added");
    return amdMets;
  }

  public void addFiles(FileSec fs, METS mets, File dir, IOFileFilter fileFilter, File rootDir, String groupId, String groupUse, String idPrefix, String cdmId) throws METSException, CDMException, IOException {
    addFiles(fs, mets, dir, fileFilter, rootDir, groupId, groupUse, idPrefix, cdmId, new DefaultDateCreatedStrategy());
  }

  public void addFiles(FileSec fs, METS mets, File dir, IOFileFilter fileFilter, File rootDir, String groupId, String groupUse, String idPrefix, String cdmId, DateCreatedStrategy dateCreatedStrategy) throws METSException, CDMException, IOException {
    if (dir == null || !dir.exists() || dir.listFiles().length == 0) {
      LOG.info("Directory " + dir + " does not exist, skipping...");
      return;
    }
    LOG.info("Adding files " + dir + " to file group " + groupId);
    final FileGrp fg = fs.newFileGrp();
    fg.setUse(groupUse);
    fg.setID(groupId);
    // list files form dir
    if (fileFilter == null) {
      if (groupId.equals("ARCGRP")) {
        fileFilter = FileFilterUtils.suffixFileFilter(".arc.gz", IOCase.INSENSITIVE);
      }
      else if (groupId.equals("WARCGRP")) {
        fileFilter = FileFilterUtils.suffixFileFilter(".warc.gz", IOCase.INSENSITIVE);
      }
      else {
        fileFilter = FileFilterUtils.trueFileFilter();
      }
    }
    final IOFileFilter dirFilter = FileFilterUtils.trueFileFilter();
    final List<File> files = new ArrayList<File>(FileUtils.listFiles(dir, fileFilter, dirFilter));
    // setridi soubory dle jmena; poradi souboru v METS vychazi z tohoto poradi
    Collections.sort(files, new Comparator<File>() {
      public int compare(File f1, final File f2) {
        return f1.getName().compareTo(f2.getName());
      }
    });
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    // add files to mets
    int counter = 1;
    for (File oneFile : files) {
      au.edu.apsr.mtk.base.File f = fg.newFile();
      f.setID(idPrefix + String.format("%04d", counter));
      f.setSize(FileUtils.sizeOf(oneFile));
      f.setMIMEType(mimeTypesMap.getContentType(oneFile));
      // f.setOwnerID("de.tar.bz.0");
      f.setChecksumType("MD5");
      f.setChecksum(getMD5Checksum(oneFile));
      f.setSeq(String.valueOf(counter - 1));
      f.setCreated(df.format(dateCreatedStrategy.getTimeCreated(cdmId, oneFile, dir.getName())));
      FLocat fl = f.newFLocat();
      fl.setHref(getRelativePath(oneFile, rootDir));
      fl.setLocType("URL");
      f.addFLocat(fl);
      fg.addFile(f);
      ++counter;
    }
    fs.addFileGrp(fg);
  }

  private void addDCSecs(METS mets, CDM cdm, String cdmId, Element dcElement) throws METSException, CDMException, DocumentException, IOException, SAXException, ParserConfigurationException {
    String dcSecId = DOCUMENT_TYPE_MONOGRAPH.equals(getDocumentType(cdmId))
        ? DMDSEC_ID_DC_VOLUME
        : DMDSEC_ID_DC_TITLE;
    addDCSecs(mets, cdm, cdmId, dcElement, dcSecId);
  }

  public void addDCSecs(METS mets, CDM cdm, String cdmId, Element dcElement, String dcSecId) throws METSException, CDMException, DocumentException, IOException, SAXException, ParserConfigurationException {
    checkNotNull(cdm, "cdm must not be null");
    checkNotNull(cdmId, "cdmId must not be null");
    checkNotNull(dcSecId, "dcSecId must not be null");

    DmdSec dmd = mets.newDmdSec();
    dmd.setID(dcSecId);
    MdWrap mdw = dmd.newMdWrap();
    mdw.setMDType("DC");
    mdw.setMIMEType("text/xml");
    mdw.setXmlData(dcElement);
    dmd.setMdWrap(mdw);
    mets.addDmdSec(dmd);
  }

  public void addDCSecsK4(METS mets, CDM cdm, String cdmId, List<Element> dcElementList) throws METSException, CDMException, DocumentException, IOException, SAXException, ParserConfigurationException {
    checkNotNull(cdm, "cdm must not be null");
    checkNotNull(cdmId, "cdmId must not be null");

    if (dcElementList.size() == 1) { //monografia
      DmdSec volumeDmd = mets.newDmdSec();
      volumeDmd.setID(DMDSEC_ID_DC_VOLUME);
      MdWrap volumeMdw = volumeDmd.newMdWrap();
      volumeMdw.setMDType("DC");
      volumeMdw.setMIMEType("text/xml");
      volumeMdw.setXmlData(dcElementList.get(0));
      volumeDmd.setMdWrap(volumeMdw);
      mets.addDmdSec(volumeDmd);
    }
    else {
      if (dcElementList.size() == 3) { //periodikum
        DmdSec issueDmd = mets.newDmdSec();
        issueDmd.setID(DMDSEC_ID_DC_ISSUE);
        MdWrap issueMdw = issueDmd.newMdWrap();
        issueMdw.setMDType("DC");
        issueMdw.setMIMEType("text/xml");
        issueMdw.setXmlData(dcElementList.get(2));//2  1old
        issueDmd.setMdWrap(issueMdw);
        mets.addDmdSec(issueDmd);

        DmdSec titleDmd = mets.newDmdSec();
        titleDmd.setID(DMDSEC_ID_DC_TITLE);
        MdWrap titleMdw = titleDmd.newMdWrap();
        titleMdw.setMDType("DC");
        titleMdw.setMIMEType("text/xml");
        titleMdw.setXmlData(dcElementList.get(0));//0  2old
        titleDmd.setMdWrap(titleMdw);
        mets.addDmdSec(titleDmd);

        DmdSec volumeDmd = mets.newDmdSec();
        volumeDmd.setID(DMDSEC_ID_DC_VOLUME);
        MdWrap volumeMdw = volumeDmd.newMdWrap();
        volumeMdw.setMDType("DC");
        volumeMdw.setMIMEType("text/xml");
        volumeMdw.setXmlData(dcElementList.get(1));//1  0old
        volumeDmd.setMdWrap(volumeMdw);
        mets.addDmdSec(volumeDmd);
      }
      else {
        throw new SystemException("Wrong number of dc element. There should be 3 (periodical) or 1 (monography).", ErrorCodes.XML_CREATION_FAILED);
      }
    }
  }

  /**
   * Prida identifier do MODS a DC hlavnych casti.
   * 
   * @param cdmId
   * @param type
   * @param value
   */
  public void addIdentifier(String cdmId, String type, String value) throws CDMException, DocumentException, XPathExpressionException, ParserConfigurationException, SAXException, IOException, METSException
  {
    addIdentifier(cdmId, getSectionIdMods(cdmId), getSectionIdDC(cdmId), type, value);
  }

  /**
   * Prida identifier do MODS casti s ID sectionIdMods a do DC casti s ID sectionIdDC.
   * 
   * @param cdmId
   * @param type
   * @param value
   */

  public void addIdentifier(String cdmId, String sectionIdMods, String sectionIdDC, String type, String value) throws CDMException, DocumentException, XPathExpressionException, ParserConfigurationException, SAXException, IOException, METSException
  {
    addIdentifierToMods(cdmId, sectionIdMods, type, value);
    addIdentifierToDC(cdmId, sectionIdDC, type, value, true);
  }

  @RetryOnFailure(attempts = 2)
  public void addValidUrnnbnToMods(String cdmId, String sectionIdMods, String value) throws CDMException, DocumentException, XPathExpressionException, ParserConfigurationException, SAXException, IOException, METSException {
    CDM cdm = new CDM();
    SAXReader saxReader = new SAXReader();
    File metsFile = cdm.getMetsFile(cdmId);
    if (!metsFile.exists()) {
      throw new SystemException("Mets file " + metsFile.getPath() + " does note exist", ErrorCodes.NO_METS_FILE);
    }
    org.dom4j.Document metsDocument = saxReader.read(metsFile);
    String addressElementMetsForMods = "//mets:dmdSec[@ID=\"" + sectionIdMods + "\"]";
    LOG.debug("Path to elements at mods: " + addressElementMetsForMods);
    org.dom4j.Element elementMetsForMods = (org.dom4j.Element) getNodeDom4jMets(addressElementMetsForMods, cdm, cdmId, metsDocument);
    if (elementMetsForMods != null) {
      LOG.debug("Going to create new identifier in mods. Identifier type: urnnbn value: " + value);
      org.dom4j.Element elementMods = (org.dom4j.Element) (elementMetsForMods).selectSingleNode("//mets:dmdSec[@ID=\"" + sectionIdMods + "\"]/mets:mdWrap/mets:xmlData/mods:mods");
      DefaultElement newElement = new DefaultElement("mods:identifier");
      newElement.addAttribute("type", "urnnbn");
      newElement.setText(value);
      addElement(elementMods, newElement);
    }
    writeToFile(metsDocument, metsFile);
  }

  /**
   * Pride identifier do MODS casti s ID sectionIdMods.
   * 
   * @param cdmId
   * @param sectionIdMods
   * @param type
   * @param value
   */
  @RetryOnFailure(attempts = 2)
  public void addIdentifierToMods(String cdmId, String sectionIdMods, String type, String value) throws CDMException, DocumentException, XPathExpressionException, ParserConfigurationException, SAXException, IOException, METSException {
    LOG.info("addIdentifier: type: " + type + " value: " + value + " to sectionIdMods: " + sectionIdMods + " for cdm:" + cdmId);
    if (value == null) {
      LOG.info("Value is null or empty, skipping adding to MODS.");
      return;
    }
    CDM cdm = new CDM();
    SAXReader saxReader = new SAXReader();
    File metsFile = cdm.getMetsFile(cdmId);
    if (!metsFile.exists()) {
      throw new SystemException("Mets file " + metsFile.getPath() + " does note exist", ErrorCodes.NO_METS_FILE);
    }
    org.dom4j.Document metsDocument = saxReader.read(metsFile);
    String addressElementMetsForMods = "//mets:dmdSec[@ID=\"" + sectionIdMods + "\"]";
    LOG.debug("Path to elements at mods: " + addressElementMetsForMods);
    org.dom4j.Element elementMetsForMods = (org.dom4j.Element) getNodeDom4jMets(addressElementMetsForMods, cdm, cdmId, metsDocument);
    if (elementMetsForMods != null) {
      List<org.dom4j.Element> elementIdentifierMods = (List<org.dom4j.Element>) ((org.dom4j.Node) elementMetsForMods).selectNodes("//mets:dmdSec[@ID=\"" + sectionIdMods + "\"]/mets:mdWrap/mets:xmlData/mods:mods/mods:identifier[@type=\"" + type + "\"]");
      if (elementIdentifierMods != null && !elementIdentifierMods.isEmpty() && modsElementAlreadyInculded(elementIdentifierMods, value)) {//same element already in MODS
        LOG.debug("Node already in MODS. Identifier type: " + type + " value: " + value);
        return;
      }

      if (elementIdentifierMods == null || elementIdentifierMods.isEmpty())
      {
        LOG.debug("Node wasnt found - going to create new identifier in mods. Identifier type: " + type + " value: " + value);
        org.dom4j.Element elementMods = (org.dom4j.Element) (elementMetsForMods).selectSingleNode("//mets:dmdSec[@ID=\"" + sectionIdMods + "\"]/mets:mdWrap/mets:xmlData/mods:mods");
        DefaultElement newElement = new DefaultElement("mods:identifier");
        newElement.addAttribute("type", type);
        newElement.setText(value);
        addElement(elementMods, newElement);
      }
      else if (elementIdentifierMods.size() == 1) {
        LOG.debug("Node was found - going to update identifier value in MODS. Identifier type: " + type + " value: " + value);
        elementIdentifierMods.get(0).setText(value);
      }
      else {
        throw new SystemException("More than 1 DC element of type: " + type + " found. Can not update element.", ErrorCodes.WRONG_URNNBN_COUNT); //Poz. Update hodnoty se vyuziva jen pri URNNBN
      }
    }
    writeToFile(metsDocument, metsFile);
  }

  /**
   * Prida identifier do DC casti s ID sectionIdDC.
   * 
   * @param cdmId
   * @param sectionIdDC
   * @param type
   * @param value
   */
  @RetryOnFailure(attempts = 2)
  public void addIdentifierToDC(String cdmId, String sectionIdDC, String type, String value, boolean updateIfExists) throws CDMException, DocumentException, XPathExpressionException, ParserConfigurationException, SAXException, IOException, METSException {
    LOG.info("addIdentifier: type: " + type + " value: " + value + " to sectionIdDC: " + sectionIdDC + " for cdm:" + cdmId);
    if (value == null || value.isEmpty()) {
      LOG.info("Value is null or empty, skipping adding to DC.");
      return;
    }
    CDM cdm = new CDM();
    SAXReader saxReader = new SAXReader();
    File metsFile = cdm.getMetsFile(cdmId);
    if (!metsFile.exists()) {
      throw new SystemException("Mets file " + metsFile.getPath() + " does note exist", ErrorCodes.NO_METS_FILE);
    }
    org.dom4j.Document metsDocument = saxReader.read(metsFile);
    String addressElementMetsForDC = "//mets:dmdSec[@ID=\"" + sectionIdDC + "\"]";
    LOG.debug("Path to elements at DC: " + addressElementMetsForDC);
    org.dom4j.Element elementMetsForDC = (org.dom4j.Element) getNodeDom4jMets(addressElementMetsForDC, cdm, cdmId, metsDocument);
    if (elementMetsForDC != null) {
      List<org.dom4j.Element> elementDCIdentifiers = ((org.dom4j.Node) elementMetsForDC).selectNodes("//mets:dmdSec[@ID=\"" + sectionIdDC + "\"]/mets:mdWrap/mets:xmlData/oai_dc:dc/dc:identifier[starts-with(text(),'" + type + ":')]");

      //smazani vicenasobnych ISBN z DC - zustane pouze prvni
      if (elementDCIdentifiers.size() > 1 && type.equals("URN:ISBN") && !sectionIdDC.startsWith(DMDSEC_ID_PREFIX_DCMD_TITLE)) {
        for (int i = elementDCIdentifiers.size() - 1; i > 0; i--) {
          LOG.debug("Detaching node: " + type + " value: " + elementDCIdentifiers.get(i).getText());
          elementDCIdentifiers.get(i).detach();
        }
        writeToFile(metsDocument, metsFile);
        elementDCIdentifiers = ((org.dom4j.Node) elementMetsForDC).selectNodes("//mets:dmdSec[@ID=\"" + sectionIdDC + "\"]/mets:mdWrap/mets:xmlData/oai_dc:dc/dc:identifier[starts-with(text(),'" + type + ":')]");
      }

      if (dcElementAlreadyInculded(elementDCIdentifiers, type + ":" + value)) {//same element with same value already in DC
        LOG.debug("Node already in DC. Identifier type: " + type + " value: " + value);
        if (type.equals("URN:ISBN")) {
          writeToFile(metsDocument, metsFile);
        }
        return;
      }
      if (elementDCIdentifiers == null || elementDCIdentifiers.isEmpty())
      {
        LOG.debug("Node wasnt found - going to create new identifier in DC. Identifier type: " + type + " value: " + value);
        org.dom4j.Element elementDC = (org.dom4j.Element) (elementMetsForDC).selectSingleNode("//mets:dmdSec[@ID=\"" + sectionIdDC + "\"]/mets:mdWrap/mets:xmlData/oai_dc:dc");
        DefaultElement newElement = new DefaultElement("dc:identifier");
        newElement.setText(type + ":" + value);
        addElement(elementDC, newElement);
      }
      else {
        if (!updateIfExists && !type.equals("uuid") && !type.equals("URN:ISBN")) {
          LOG.debug("Node was found - going to add same type identifier in DC. Identifier type: " + type + " value: " + value);
          org.dom4j.Element elementDC = (org.dom4j.Element) (elementMetsForDC).selectSingleNode("//mets:dmdSec[@ID=\"" + sectionIdDC + "\"]/mets:mdWrap/mets:xmlData/oai_dc:dc");
          DefaultElement newElement = new DefaultElement("dc:identifier");
          newElement.setText(type + ":" + value);
          addElement(elementDC, newElement);
        }
        else {
          if (elementDCIdentifiers.size() == 1) {
            LOG.debug("Node was found - going to update identifier value in DC. Identifier type: " + type + " value: " + value);
            elementDCIdentifiers.get(0).setText(type + ":" + value);
          }
          else {
            if (sectionIdDC.startsWith(DMDSEC_ID_PREFIX_MODSMD_TITLE)) {
              return;
            }
            throw new SystemException("More than 1 DC element of type: " + type + " found. Can not update element.", ErrorCodes.WRONG_URNNBN_COUNT); //Poz. Update hodnoty se vyuziva jen pri URNNBN
          }
        }

      }
    }
    writeToFile(metsDocument, metsFile);
  }

  public void removeDcElement(String cdmId, String type, String sectionIdDC) throws DocumentException, CDMException, XPathExpressionException, ParserConfigurationException, SAXException, IOException {
    CDM cdm = new CDM();
    SAXReader saxReader = new SAXReader();
    File metsFile = cdm.getMetsFile(cdmId);
    if (!metsFile.exists()) {
      throw new SystemException("Mets file " + metsFile.getPath() + " does note exist", ErrorCodes.NO_METS_FILE);
    }
    org.dom4j.Document metsDocument = saxReader.read(metsFile);
    String addressElementMetsForDC = "//mets:dmdSec[@ID=\"" + sectionIdDC + "\"]";
    LOG.debug("Path to elements at DC: " + addressElementMetsForDC);
    org.dom4j.Element elementMetsForDC = (org.dom4j.Element) getNodeDom4jMets(addressElementMetsForDC, cdm, cdmId, metsDocument);
    List<org.dom4j.Element> elementDCIdentifiers = null;
    if (elementMetsForDC != null) {
      elementDCIdentifiers = ((org.dom4j.Node) elementMetsForDC).selectNodes("//mets:dmdSec[@ID=\"" + sectionIdDC + "\"]/mets:mdWrap/mets:xmlData/oai_dc:dc/dc:identifier[starts-with(text(),'" + type + ":')]");
    }
    if (elementDCIdentifiers != null && !elementDCIdentifiers.isEmpty()) {
      elementDCIdentifiers.get(0).detach();
    }
    writeToFile(metsDocument, metsFile);
  }

  private boolean dcElementAlreadyInculded(List<org.dom4j.Element> elementDCIdentifiers, String value) {
    for (org.dom4j.Element element : elementDCIdentifiers) {
      if (element.getStringValue().equals(value)) {
        return true;
      }
    }
    return false;
  }

  private boolean modsElementAlreadyInculded(List<org.dom4j.Element> elementModsIdentifiers, String value) {
    for (org.dom4j.Element element : elementModsIdentifiers) {
      if (element.getStringValue().equals(value.trim().replaceAll("[\\u00A0]", ""))) {
        return true;
      }
    }
    return false;
  }

  /**
   * Pridaj element v strome pod parent element. Ak uz existuje element s takym menom potom ho pridaj pred tento
   * element.
   * 
   * @param node
   * @param elementToAdd
   */
  @SuppressWarnings("unchecked")
  private void addElement(org.dom4j.Element parent, org.dom4j.Element elementToAdd) {
    if (parent == null || elementToAdd == null || elementToAdd.getName() == null) {
      return;
    }
    @SuppressWarnings("rawtypes")
    List content = parent.content();
    int index = 0;
    for (; index < content.size(); index++) {
      Object object = content.get(index);
      if (object instanceof org.dom4j.Element) {
        org.dom4j.Element anElement = (org.dom4j.Element) object;
        String s1 = elementToAdd.getName();
        String s2 = null;
        int p = elementToAdd.getName().indexOf(":");
        if (p != -1) {
          s2 = elementToAdd.getName().substring(p + 1);
        }
        else {
          s2 = elementToAdd.getName();
        }
        // hladame ci uz existuje element s takym menom pricom kontrolujeme meno s namespace prefixom aj bez neho
        if (s1.equals(anElement.getName()) || s2.equals(anElement.getName())) {
          break;
        }
      }
    }
    if (index != 0) {
      content.add(index, elementToAdd);
    }
    else {
      content.add(elementToAdd);
    }
  }

  /**
   * Zabezpeci aby vsetky indetifiers ktore su v MODS castiach boli aj v DC castiach a to iste aj naopak.
   * 
   * @param cdmId
   */
  public void consolidateIdentifiers(String cdmId) throws DocumentException, IOException, CDMException, XPathExpressionException, METSException, ParserConfigurationException, SAXException {
    LOG.info("consolidateIdentifiers stat cdmId: " + cdmId);
    CDM cdm = new CDM();
    File metsFile = cdm.getMetsFile(cdmId);
    // ziskaj identifiers v MODS casti VOLUME
    LOG.info("consolidateIdentifiers from MODS to DC");
    List<String> dmdSecsIds = getDmdSecsIds(metsFile);
    for (String dmdSecId : dmdSecsIds) {
      if (dmdSecId.startsWith(DMDSEC_ID_PREFIX_MODSMD_TITLE) || dmdSecId.startsWith(DMDSEC_ID_PREFIX_MODSMD_VOLUME) || dmdSecId.startsWith(DMDSEC_ID_PREFIX_MODSMD_ISSUE) || dmdSecId.startsWith(DMDSEC_ID_PREFIX_MODSMD_SUPPL)) {
        String sectionIdDc = dmdSecId.replace("MODSMD_", "DCMD_");
        Map<String, List<String>> ids = getAllIdentifiersFromMods(cdm, cdmId, dmdSecId);
        // pridaj vsetky id z MODS casti do DC casti (ak uz existuju tak sa updatuju)
        for (String type : ids.keySet()) {
          for (String value : ids.get(type)) {
            if (type.equals("URN:ISBN")) {
              addIdentifierToDC(cdmId, sectionIdDc, type, value, false);
              break;
            }
            else {
              addIdentifierToDC(cdmId, sectionIdDc, type, value, false);
            }
          }

        }
      }
    }
    // to iste teraz naopak
    LOG.info("consolidateIdentifiers from DC to MODS");
    dmdSecsIds = getDmdSecsIds(metsFile);
    for (String dmdSecId : dmdSecsIds) {
      if (dmdSecId.startsWith(DMDSEC_ID_PREFIX_DCMD_TITLE) || dmdSecId.startsWith(DMDSEC_ID_PREFIX_DCMD_VOLUME) || dmdSecId.startsWith(DMDSEC_ID_PREFIX_DCMD_ISSUE) || dmdSecId.startsWith(DMDSEC_ID_PREFIX_MODSMD_SUPPL)) {
        String sectionIdDc = dmdSecId.replace("DCMD_", "MODSMD_");
        Map<String, String> ids = getIdentifiersFromDC(cdm, cdmId, dmdSecId);
        // pridaj vsetky id z DC casti do MODS casti (ak uz existuju tak sa updatuju)
        for (String type : ids.keySet()) {
          addIdentifierToMods(cdmId, sectionIdDc, type, ids.get(type));
        }
      }
    }
    LOG.info("consolidateIdentifiers end");
  }

  public static org.dom4j.Node getNodeDom4jMets(String xPathExpression, CDM cdm, String cdmId, org.dom4j.Document document) throws CDMException, DocumentException, ParserConfigurationException, SAXException, IOException, XPathExpressionException {

    Namespace nsMods = new Namespace("mets", "http://www.loc.gov/METS/");
    Namespace nsPremis = new Namespace("premis", "info:lc/xmlns/premis-v2");
    Namespace nsOaiDc = new Namespace("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");

    org.dom4j.Document metsDocument = document;
    LOG.debug(metsDocument.getXMLEncoding());
    XPath xPath = metsDocument.createXPath(xPathExpression);
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mets", nsMods.getStringValue(), "premis", nsPremis.getStringValue(), "oai_dc", nsOaiDc.getStringValue()));
    org.dom4j.Node node = xPath.selectSingleNode(metsDocument);
    if (node == null) {
      return null;
    }
    return node;
  }

  public void addDCSecs(File metsFile, CDM cdm, String cdmId) throws METSException, SAXException, IOException, ParserConfigurationException, CDMException, DocumentException {
    Element dcElement = createDCElement(cdmId);
    LOG.debug("Adding dc section to METS file " + metsFile.getName());
    Document metsDocument = XMLHelper.parseXML(metsFile);
    METSWrapper mw = new METSWrapper(metsDocument);
    METS mets = mw.getMETSObject();
    addDCSecs(mets, cdm, cdmId, dcElement);
    writeMetsWrapper(metsFile, mw);
    LOG.debug("Section removed");
  }

  public String getRelativePath(File f, File cdmDir) {
    return cdmDir.toURI().relativize(f.toURI()).getPath();
  }

  @RetryOnFailure(attempts = 3)
  public String getMD5Checksum(File f) throws IOException {
    final FileInputStream fis = new FileInputStream(f);
    try {
      return DigestUtils.md5Hex(fis);
    }
    finally {
      IOUtils.closeQuietly(fis);
    }
  }

  public int createMETSForImage(File file, int pageCounter, String cdmId, String label, File inDir, Collection<File> flatFiles) {
    CDM cdm = new CDM();
    final File outDir = cdm.getAmdDir(cdmId);

    LOG.debug("Going to create amdSec for file: " + file.getPath());
    String pageName = file.getName().substring(0, file.getName().indexOf("."));

    LOG.debug("pageName:" + pageName);
    try {

      // <mets:mets xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:premis="info:lc/xmlns/premis-v2" xmlns:mix="http://www.loc.gov/mix/v20" xsi:schemaLocation="http://www.w3.org/2001/XMLSchema-instance http://www.w3.org/2001/XMLSchema.xsd http://www.loc.gov/METS/ http://www.loc.gov/standards/mets/mets.xsd http://www.loc.gov/mix/v20 http://www.loc.gov/standards/mix/mix20/mix20.xsd info:lc/xmlns/premis-v2 http://www.loc.gov/standards/premis/premis.xsd" TYPE="Periodical" xmlns:mets="http://www.loc.gov/METS/">

      int mixId = 1;

      org.dom4j.Document document = DocumentHelper.createDocument();
      org.dom4j.Element metsElement = document.addElement(new QName("mets", nsMets));
      metsElement.add(nsPremis);
      metsElement.add(nsMix);
      metsElement.add(nsXsi);

      metsElement
          .addAttribute(
              "xsi:schemaLocation",
              "http://www.w3.org/2001/XMLSchema-instance http://www.w3.org/2001/XMLSchema.xsd http://www.loc.gov/METS/ http://www.loc.gov/standards/mets/mets.xsd http://www.loc.gov/mix/v20 http://www.loc.gov/standards/mix/mix20/mix20.xsd info:lc/xmlns/premis-v2 http://www.loc.gov/standards/premis/premis.xsd");

      metsElement.addAttribute("TYPE", getDocumentType(cdmId));
      Element mainMods = null;

      metsElement.addAttribute("LABEL", label);

      org.dom4j.Element metsHdrElement = metsElement.addElement(new QName("metsHdr", nsMets));
      XMLGregorianCalendar currentDate = DateUtils.toXmlDateTime(new Date(file.lastModified()));
      metsHdrElement.addAttribute("CREATEDATE", currentDate.toXMLFormat());
      metsHdrElement.addAttribute("LASTMODDATE", currentDate.toXMLFormat());

      org.dom4j.Element agentElement = metsHdrElement.addElement(new QName("agent", nsMets));
      agentElement.addAttribute("ROLE", "CREATOR");
      agentElement.addAttribute("TYPE", "ORGANIZATION");

      agentElement.addElement(new QName("name", nsMets))
          .setText("NDK_TM");

      org.dom4j.Element amdSecElement = metsElement.addElement(new QName("amdSec", nsMets));
      amdSecElement.addAttribute("ID", format(AMD_SEC_ID_FORMAT, pageCounter++));

      File premisDir;

      premisDir = cdm.getPremisDir(cdmId);

      if (premisDir.listFiles().length > 0) {

        if ("K4".equals(cdm.getCdmProperties(cdmId).getProperty("importType"))) {
          addPremisObjSection(cdmId, amdSecElement, pageName, CDMSchemaDir.ORIGINAL_DATA.getDirName(), 1);
          addPremisObjSection(cdmId, amdSecElement, getNewName(pageName, cdmId), inDir.getName(), 2);
          addPremisObjSection(cdmId, amdSecElement, getNewName(pageName, cdmId), CDMSchemaDir.MC_DIR.getDirName(), 3);
        }
        else {
          //TODO change values for OBJ_001
          addPremisObjSection(cdmId, amdSecElement, pageName, inDir.getName(), 1);

          // MasterCopy
          //TODO change values for OBJ_002
          addPremisObjSection(cdmId, amdSecElement, getNewName(pageName, cdmId), CDMSchemaDir.MC_DIR.getDirName(), 2);

          if (cdm.getAltoDir(cdmId).listFiles().length > 0) {
            // ALTO
            //TODO change values for OBJ_003
            addPremisObjSection(cdmId, amdSecElement, getNewName(pageName, cdmId), CDMSchemaDir.ALTO_DIR.getDirName(), 3);
          }
        }
      }
      else {
        //TODO je to spravne? Minimalne validace
        LOG.debug("Empty PREMIS Dir...");
      }

      if ("K4".equals(cdm.getCdmProperties(cdmId).getProperty("importType"))) {
        //******MIX_001 - ORIGINAL_DATA*********
        org.dom4j.Element mixTechMDElement = amdSecElement.addElement(new QName("techMD", nsMets));
        mixTechMDElement.addAttribute("ID", format("MIX_%03d", mixId++));

        org.dom4j.Element mixMdWrapElement = mixTechMDElement.addElement(new QName("mdWrap", nsMets));
        mixMdWrapElement.addAttribute("MDTYPE", "NISOIMG");
        mixMdWrapElement.addAttribute("MIMETYPE", "text/xml");

        File mixFile = null;
        mixFile = new File(cdm.getWorkspaceDir(cdmId), "mix/" + cdm.getOriginalDataDir(cdmId).getName() + "/" + FilenameUtils.removeExtension(file.getName()) + ".xml.mix");

        org.dom4j.Document mixDocument = DocumentHelper.parseText(retriedReadFileToString(mixFile));
        XMLHelper.qualify(mixDocument, nsMix);

        Namespace mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("");
        mixDocument.getRootElement().remove(mixNamespace);
        mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("mix");
        mixDocument.getRootElement().remove(mixNamespace);

        org.dom4j.Element mixXmlDataElement = mixMdWrapElement.addElement(new QName("xmlData", nsMets));
        mixXmlDataElement.add(mixDocument.getRootElement());

        //******MIX_002 MASTER_COPY_TIFF*********
        mixTechMDElement = amdSecElement.addElement(new QName("techMD", nsMets));
        mixTechMDElement.addAttribute("ID", format("MIX_%03d", mixId++));

        mixMdWrapElement = mixTechMDElement.addElement(new QName("mdWrap", nsMets));
        mixMdWrapElement.addAttribute("MDTYPE", "NISOIMG");
        mixMdWrapElement.addAttribute("MIMETYPE", "text/xml");

        mixFile = new File(cdm.getWorkspaceDir(cdmId), "mix/" + CDMSchemaDir.MASTER_COPY_TIFF_DIR.getDirName() + "/" + file.getName() + ".xml.mix");

        mixDocument = DocumentHelper.parseText(retriedReadFileToString(mixFile));
        XMLHelper.qualify(mixDocument, nsMix);

        mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("");
        mixDocument.getRootElement().remove(mixNamespace);
        mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("mix");
        mixDocument.getRootElement().remove(mixNamespace);

        mixXmlDataElement = mixMdWrapElement.addElement(new QName("xmlData", nsMets));
        mixXmlDataElement.add(mixDocument.getRootElement());

        //******MIX_003 MASTER_COPY*********
        mixTechMDElement = amdSecElement.addElement(new QName("techMD", nsMets));
        mixTechMDElement.addAttribute("ID", format("MIX_%03d", mixId++));

        mixMdWrapElement = mixTechMDElement.addElement(new QName("mdWrap", nsMets));
        mixMdWrapElement.addAttribute("MDTYPE", "NISOIMG");
        mixMdWrapElement.addAttribute("MIMETYPE", "text/xml");

        mixFile = new File(cdm.getWorkspaceDir(cdmId), "mix/" + cdm.getMasterCopyDir(cdmId).getName() + "/" + FilenameUtils.removeExtension(file.getName()) + ".tif.jp2.xml.mix");

        mixDocument = DocumentHelper.parseText(retriedReadFileToString(mixFile));
        XMLHelper.qualify(mixDocument, nsMix);

        mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("");
        mixDocument.getRootElement().remove(mixNamespace);
        mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("mix");
        mixDocument.getRootElement().remove(mixNamespace);

        mixXmlDataElement = mixMdWrapElement.addElement(new QName("xmlData", nsMets));
        mixXmlDataElement.add(mixDocument.getRootElement());
      }

      else {

        //******MIX_001*********
        org.dom4j.Element mixTechMDElement = amdSecElement.addElement(new QName("techMD", nsMets));
        mixTechMDElement.addAttribute("ID", format("MIX_%03d", mixId++));

        org.dom4j.Element mixMdWrapElement = mixTechMDElement.addElement(new QName("mdWrap", nsMets));
        mixMdWrapElement.addAttribute("MDTYPE", "NISOIMG");
        mixMdWrapElement.addAttribute("MIMETYPE", "text/xml");

        File mixFile = null;

        mixFile = new File(cdm.getWorkspaceDir(cdmId), "mix/" + cdm.getFlatDataDir(cdmId).getName() + "/" + getFlatFileForPPFile(cdmId, FilenameUtils.getBaseName(file.getName())) + "." + FilenameUtils.getExtension(file.getName()) + ".xml.mix");

        if (!mixFile.exists()) {
          if (cdm.getPostprocessingDataDir(cdmId).listFiles().length > 0) {
            mixFile = new File(cdm.getWorkspaceDir(cdmId), "mix/" + cdm.getPostprocessingDataDir(cdmId).getName() + "/" + file.getName() + ".xml.mix");
          }
          else {
            File mcTiffDir = new File(cdm.getWorkspaceDir(cdmId) + File.separator + CDMSchemaDir.MASTER_COPY_TIFF_DIR.getDirName());
            if (mcTiffDir.exists()) { //Import from Kramerius
              mixFile = new File(cdm.getWorkspaceDir(cdmId), "mix/" + mcTiffDir.getName() + "/" + file.getName() + ".xml.mix");
            }
            else {
              mixFile = new File(cdm.getWorkspaceDir(cdmId), "mix/" + cdm.getMasterCopyDir(cdmId).getName() + "/" + file.getName() + ".jp2.xml.mix");
            }
          }
        }
        org.dom4j.Document mixDocument = DocumentHelper.parseText(retriedReadFileToString(mixFile));
        XMLHelper.qualify(mixDocument, nsMix);

        Namespace mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("");
        mixDocument.getRootElement().remove(mixNamespace);
        mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("mix");
        mixDocument.getRootElement().remove(mixNamespace);

        org.dom4j.Element mixXmlDataElement = mixMdWrapElement.addElement(new QName("xmlData", nsMets));
        mixXmlDataElement.add(mixDocument.getRootElement());

        //******MIX_002*********
        mixTechMDElement = amdSecElement.addElement(new QName("techMD", nsMets));
        mixTechMDElement.addAttribute("ID", format("MIX_%03d", mixId++));

        mixMdWrapElement = mixTechMDElement.addElement(new QName("mdWrap", nsMets));
        mixMdWrapElement.addAttribute("MDTYPE", "NISOIMG");
        mixMdWrapElement.addAttribute("MIMETYPE", "text/xml");

        mixFile = new File(cdm.getWorkspaceDir(cdmId), "mix/" + cdm.getMasterCopyDir(cdmId).getName() + "/" + file.getName() + ".jp2.xml.mix");

        mixDocument = DocumentHelper.parseText(retriedReadFileToString(mixFile));
        XMLHelper.qualify(mixDocument, nsMix);

        mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("");
        mixDocument.getRootElement().remove(mixNamespace);
        mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("mix");
        mixDocument.getRootElement().remove(mixNamespace);

        mixXmlDataElement = mixMdWrapElement.addElement(new QName("xmlData", nsMets));
        mixXmlDataElement.add(mixDocument.getRootElement());
      }

      String importType = getImportType(cdmId);
      //*****************Events****************************
      int evtId = 1;
      File premisFlatFile = new File(cdm.getPremisDir(cdmId), PREMIS_PREFIX + CDMSchemaDir.FLAT_DATA_DIR.getDirName() + "_" + getFlatFileForPPFile(cdmId, pageName) + ".xml");
      if (premisFlatFile.exists()) {
        evtId = addPremisEvtSection(cdmId, amdSecElement, getFlatFileForPPFile(cdmId, pageName), CDMSchemaDir.FLAT_DATA_DIR.getDirName(), evtId);
      }
      if (importType != null && importType.equals(DOCUMENT_TYPE_K4)) {
        evtId = addPremisEvtSection(cdmId, amdSecElement, pageName, CDMSchemaDir.ORIGINAL_DATA.getDirName(), evtId);
      }
      evtId = addPremisEvtSection(cdmId, amdSecElement, pageName, inDir.getName(), evtId);
      evtId = addPremisEvtSection(cdmId, amdSecElement, getNewName(pageName, cdmId), CDMSchemaDir.MC_DIR.getDirName(), evtId);
      //addPremisEvtSection(cdmId, amdSecElement, pageName, CDMSchemaDir.UC_DIR.getDirName(), evtId++);
      if (cdm.getAltoDir(cdmId).listFiles().length > 0) { // if not import
        evtId = addPremisEvtSection(cdmId, amdSecElement, getNewName(pageName, cdmId), CDMSchemaDir.ALTO_DIR.getDirName(), evtId);
      }
//    addPremisEvtSection(cdmId, amdSecElement, CDMSchemaDir.ORIGINAL_DATA.getDirName(), CDMSchemaDir.ORIGINAL_DATA.getDirName(), evtId++);

      //*****************Agents****************************
      int agentId = 1;
    //  boolean isPackageType = (cdm.getCdmProperties(cdmId).getProperty("importType") != null && cdm.getCdmProperties(cdmId).getProperty("importType").equals("PACKAGE")) ? true : false;
      if (flatFiles.size() > 0) {
        //Check if flag update from ltp exist
        if (!(ImportFromLTPHelper.isFromLtpImport(file, cdmId))) {
          //String importType = getImportType(cdmId);
          if (importType != null && importType.equals(DOCUMENT_TYPE_K4)) {
            agentId = addPremisAgentSection(cdmId, amdSecElement, pageName, CDMSchemaDir.ORIGINAL_DATA.getDirName(), agentId);
          }
          else {
            agentId = addPremisAgentSection(cdmId, amdSecElement, getFlatFileForPPFile(cdmId, pageName), CDMSchemaDir.FLAT_DATA_DIR.getDirName(), agentId);
          }
        }
      }
      else {
        throw new SystemException("No images in flatData. cdmId: " + cdmId + "or in parent of this entity..", ErrorCodes.FILE_NOT_FOUND);
      }
      agentId = addPremisAgentSection(cdmId, amdSecElement, pageName, inDir.getName(), agentId++);
      agentId = addPremisAgentSection(cdmId, amdSecElement, getNewName(pageName, cdmId), CDMSchemaDir.MC_DIR.getDirName(), agentId++);
      //addPremisAgentSection(cdmId, amdSecElement, pageName, CDMSchemaDir.UC_DIR.getDirName(), agentId++);
      if (cdm.getAltoDir(cdmId).listFiles().length > 0) { // if not import
        agentId = addPremisAgentSection(cdmId, amdSecElement, getNewName(pageName, cdmId), CDMSchemaDir.ALTO_DIR.getDirName(), agentId++);
      }

      File outputFile = new File(outDir, AMD_METS_FILE_PREFIX + getNewName(pageName, cdmId) + ".xml");

      writeToFile(document, outputFile);

      LOG.info("Write METS file for page {} into file {}", pageName, outputFile);

    }
    catch (Exception e) {
      LOG.error(format("Write METS file for %s failed. ", file) + e, e);
      throw new SystemException(format("Write METS file for %s failed.", file), e);
    }

    return pageCounter;

  }

  public void createMETSForImages(final String cdmId, final String label, final File inDir, Collection<File> inFiles, Collection<File> flatFiles) {
    checkNotNull(cdmId, "cdmId must not be null");
    LOG.debug("createMETSForImages started for: " + cdmId);

    FileFilter filter = new WildcardFileFilter(new String[] { "*.tif", "*.jp2", "*.tiff" }, IOCase.INSENSITIVE); // TODO ondrusekl (30.4.2012): Bude toho vice?
    int pageCounter = 0;
    for (File file : inFiles) {
      if (!filter.accept(file)) {
        continue;
      }
      pageCounter = createMETSForImage(file, pageCounter, cdmId, label, inDir, flatFiles);
    }
  }

  public void createMETSForImagesAfterConvertFromLTP(final String cdmId) {
    checkNotNull(cdmId, "cdmId must not be null");

    CDM cdm = new CDM();

    final File outDir = cdm.getAmdDir(cdmId);
    File emCsv = cdm.getEmConfigFile(cdmId);
    final List<EmCsvRecord> records = getListFromEmCsv(cdmId);
    int pageCounter = 0;

    for (EmCsvRecord record : records) {
      String pageName = record.getPageId();

      try {
        //Create amdSection
        org.dom4j.Document document = DocumentHelper.createDocument();
        org.dom4j.Element metsElement = document.addElement(new QName("mets", nsMets));
        metsElement.add(nsPremis);
        metsElement.add(nsMix);
        metsElement.add(nsXsi);

        metsElement
            .addAttribute(
                "xsi:schemaLocation",
                "http://www.w3.org/2001/XMLSchema-instance http://www.w3.org/2001/XMLSchema.xsd http://www.loc.gov/METS/ http://www.loc.gov/standards/mets/mets.xsd http://www.loc.gov/mix/v20 http://www.loc.gov/standards/mix/mix20/mix20.xsd info:lc/xmlns/premis-v2 http://www.loc.gov/standards/premis/premis.xsd");

        metsElement.addAttribute("TYPE", getDocumentType(cdmId));

        Document metsDocument = XMLHelper.parseXML(cdm.getMetsFile(cdmId));
        METSWrapper mw = new METSWrapper(metsDocument);
        METS mets = mw.getMETSObject();
        metsElement.addAttribute("LABEL", mets.getLabel());

        org.dom4j.Element metsHdrElement = metsElement.addElement(new QName("metsHdr", nsMets));
        XMLGregorianCalendar currentDate = DateUtils.toXmlDateTime(new Date(emCsv.lastModified()));
        metsHdrElement.addAttribute("CREATEDATE", currentDate.toXMLFormat());
        metsHdrElement.addAttribute("LASTMODDATE", currentDate.toXMLFormat());

        org.dom4j.Element agentElement = metsHdrElement.addElement(new QName("agent", nsMets));
        agentElement.addAttribute("ROLE", "CREATOR");
        agentElement.addAttribute("TYPE", "ORGANIZATION");

        agentElement.addElement(new QName("name", nsMets))
            .setText("NDK_TM");

        org.dom4j.Element amdSecElement = metsElement.addElement(new QName("amdSec", nsMets));
        amdSecElement.addAttribute("ID", format(AMD_SEC_ID_FORMAT, pageCounter++));

        //add OBJ sections
        addLTPMigrationPremisObjSections(cdmId, amdSecElement, pageName, 1);

        //add MIX sections
        //MIX_001
        int mixId = 1;
        File mixFile = null;
        mixFile = new File(cdm.getWorkspaceDir(cdmId), "mix/" + cdm.getPostprocessingDataDir(cdmId).getName() + "/" + "1_" + pageName + ".tif.xml.mix");
        addMixSection(cdmId, amdSecElement, pageName, mixFile, mixId++);

        //MIX_002
        mixFile = new File(cdm.getWorkspaceDir(cdmId), "mix/" + cdm.getMasterCopyDir(cdmId).getName() + "/" + "1_" + pageName + ".tif.jp2.xml.mix");
        addMixSection(cdmId, amdSecElement, pageName, mixFile, mixId++);

        //MIX_003
        IOFileFilter filter = new WildcardFileFilter("*MC_" + pageName + ".tif.xml.mix");
        List<File> mixFileAsList = (List<File>) FileUtils.listFiles(new File(cdm.getMixDir(cdmId).getAbsolutePath() + File.separator + cdm.getPostprocessingDataDir(cdmId).getName()), filter, FileFilterUtils.trueFileFilter());
        if (mixFileAsList.size() != 1) {
          throw new SystemException("There should be exactly one mix file for *" + pageName + ".jpg.xml.mix", ErrorCodes.WRONG_FILES_COUNT);
        }
        addMixSection(cdmId, amdSecElement, pageName, mixFileAsList.get(0), mixId++);

        //MIX_004
        LTPFormatMigrationProfileHelper migrationProfileHelper = new LTPFormatMigrationProfileHelper();
        filter = new WildcardFileFilter(format("*%s.%s.xml.mix", pageName, migrationProfileHelper.getTargetExtension(cdm.getCdmProperties(cdmId).getProperty("processType"))));
        mixFileAsList = (List<File>) FileUtils.listFiles(new File(cdm.getMixDir(cdmId).getAbsolutePath() + File.separator + cdm.getMasterCopyDir(cdmId).getName()), filter, FileFilterUtils.trueFileFilter());
        if (mixFileAsList.size() != 1) {
          throw new SystemException("There should be exactly one mix file for *" + pageName + ".jpg.xml.mix", ErrorCodes.WRONG_FILES_COUNT);
        }
        addMixSection(cdmId, amdSecElement, pageName, mixFileAsList.get(0), mixId++);

        addLTPMigrationPremisEvtSections(cdmId, amdSecElement, pageName, 1);
        addLTPMigrationPremisAgentSection(cdmId, amdSecElement, pageName, 1);

        File outputFile = new File(outDir, AMD_METS_FILE_PREFIX + getNewName(pageName, cdmId) + ".xml");

        writeToFile(document, outputFile);

        LOG.info("Write METS file for page {} into file {}", pageName, outputFile);

      }
      catch (Exception e) {
        LOG.error(format("Write METS file for %s failed.", pageName), e);
        throw new SystemException(format("Write METS file for %s failed.", emCsv), e);
      }
    }
  }

  @RetryOnFailure(attempts = 1)
  public void addMixSection(String cdmId, org.dom4j.Element amdSecElement, String pageName, File mixFile, int mixId) throws DocumentException, IOException {
    org.dom4j.Element mixTechMDElement = amdSecElement.addElement(new QName("techMD", nsMets));
    mixTechMDElement.addAttribute("ID", format("MIX_%03d", mixId++));

    org.dom4j.Element mixMdWrapElement = mixTechMDElement.addElement(new QName("mdWrap", nsMets));
    mixMdWrapElement.addAttribute("MDTYPE", "NISOIMG");
    mixMdWrapElement.addAttribute("MIMETYPE", "text/xml");

    //org.dom4j.Document mixDocument = DocumentHelper.parseText(FileUtils.readFileToString(mixFile));
    org.dom4j.Document mixDocument = DocumentHelper.parseText(retriedReadFileToString(mixFile));
    XMLHelper.qualify(mixDocument, nsMix);

    Namespace mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("");
    mixDocument.getRootElement().remove(mixNamespace);
    mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("mix");
    mixDocument.getRootElement().remove(mixNamespace);

    org.dom4j.Element mixXmlDataElement = mixMdWrapElement.addElement(new QName("xmlData", nsMets));
    mixXmlDataElement.add(mixDocument.getRootElement());
  }

  @RetryOnFailure(attempts = 1)
  public void addPremisObjSection(String cdmId, org.dom4j.Element amdSecElement, String pageName, String dirName, int objId) throws DocumentException, IOException {
    LOG.debug("Adding " + dirName + " premis for pageName: " + pageName);
    CDM cdm = new CDM();
    org.dom4j.Element techMDElement = amdSecElement.addElement(new QName("techMD", nsMets));
    techMDElement.addAttribute("ID", format(OBJ_ID_FORMAT, objId));

    org.dom4j.Element mdWrapElement = techMDElement.addElement(new QName("mdWrap", nsMets));
    mdWrapElement.addAttribute("MDTYPE", "PREMIS");
    mdWrapElement.addAttribute("MIMETYPE", "text/xml");

    File premisFile = new File(cdm.getPremisDir(cdmId), PREMIS_PREFIX + dirName + "_" + pageName + ".xml");
    if (!premisFile.exists()) {
      premisFile = new File(cdm.getPremisDir(cdmId), PREMIS_PREFIX + dirName + "_" + StringUtils.substringAfter(pageName, "_") + ".xml");
    }
    //org.dom4j.Document premisDocument = DocumentHelper.parseText(FileUtils.readFileToString(premisFile));
    org.dom4j.Document premisDocument = DocumentHelper.parseText(retriedReadFileToString(premisFile));
    XMLHelper.qualify(premisDocument, nsPremis);
    Namespace premisNamespace = premisDocument.getRootElement().getNamespaceForPrefix("");
    premisDocument.getRootElement().remove(premisNamespace);
    //  premisDocument.getRootElement().addNamespace("premis", "info:lc/xmlns/premis-v2");

    org.dom4j.Element xmlDataElement = mdWrapElement.addElement(new QName("xmlData", nsMets));
    XPath xPath = premisDocument.createXPath("//premis:object");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("premis", nsPremis.getStringValue()));
    org.dom4j.Element premisObject = (org.dom4j.Element) xPath.selectSingleNode(premisDocument);
    premisObject.addAttribute(QName.get("type", nsXsi), "premis:file");
    xmlDataElement.add(premisObject.createCopy());

  }

  @RetryOnFailure(attempts = 3)
  public void testFailure() {
    LOG.info("Test rerun anotation");
    throw new SystemException();
  }

  @RetryOnFailure(attempts = 1)
  public void addLTPMigrationPremisObjSections(String cdmId, org.dom4j.Element amdSecElement, String pageName, int objId) throws DocumentException, IOException {
    CDM cdm = new CDM();

    IOFileFilter filter = new WildcardFileFilter("*" + pageName + ".xml");
    List<File> premisFiles = (List<File>) FileUtils.listFiles(cdm.getPremisDir(cdmId), filter, FileFilterUtils.trueFileFilter());
    //XML does not have MIX section, will be last

    for (int i = premisFiles.size() - 1; i >= 0; i--) {

      org.dom4j.Element techMDElement = amdSecElement.addElement(new QName("techMD", nsMets));
      techMDElement.addAttribute("ID", format(OBJ_ID_FORMAT, objId++));

      org.dom4j.Element mdWrapElement = techMDElement.addElement(new QName("mdWrap", nsMets));
      mdWrapElement.addAttribute("MDTYPE", "PREMIS");
      mdWrapElement.addAttribute("MIMETYPE", "text/xml");

      //org.dom4j.Document premisDocument = DocumentHelper.parseText(FileUtils.readFileToString(premisFiles.get(i)));
      org.dom4j.Document premisDocument = DocumentHelper.parseText(retriedReadFileToString(premisFiles.get(i)));
      XMLHelper.qualify(premisDocument, nsPremis);
      Namespace premisNamespace = premisDocument.getRootElement().getNamespaceForPrefix("");
      premisDocument.getRootElement().remove(premisNamespace);
      //  premisDocument.getRootElement().addNamespace("premis", "info:lc/xmlns/premis-v2");

      org.dom4j.Element xmlDataElement = mdWrapElement.addElement(new QName("xmlData", nsMets));
      XPath xPath = premisDocument.createXPath("//premis:object");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("premis", nsPremis.getStringValue()));
      org.dom4j.Element premisObject = (org.dom4j.Element) xPath.selectSingleNode(premisDocument);
      premisObject.addAttribute(QName.get("type", nsXsi), "premis:file");
      xmlDataElement.add(premisObject.createCopy());
    }

  }

  public int addLTPMigrationPremisEvtSections(String cdmId, org.dom4j.Element amdSecElement, String pageName, int objId) throws DocumentException, IOException {
    CDM cdm = new CDM();
    IOFileFilter filter = new WildcardFileFilter("*" + pageName + ".xml");
    List<File> premisFiles = (List<File>) FileUtils.listFiles(cdm.getPremisDir(cdmId), filter, FileFilterUtils.trueFileFilter());
    for (File premisFile : premisFiles) {
      //org.dom4j.Document premisDocument = DocumentHelper.parseText(FileUtils.readFileToString(premisFile));
      org.dom4j.Document premisDocument = DocumentHelper.parseText(retriedReadFileToString(premisFile));
      XMLHelper.qualify(premisDocument, nsPremis);
      Namespace premisNamespace = premisDocument.getRootElement().getNamespaceForPrefix("");
      premisDocument.getRootElement().remove(premisNamespace);

      XPath xPath = premisDocument.createXPath("//premis:event");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("premis", nsPremis.getStringValue()));
      List<org.dom4j.Node> eventNodeList = xPath.selectNodes(premisDocument);
      for (org.dom4j.Node node : eventNodeList) {
        org.dom4j.Element digiprovMDElement = amdSecElement.addElement(new QName("digiprovMD", nsMets));
        digiprovMDElement.addAttribute("ID", format(EVT_ID_FORMAT, objId));

        org.dom4j.Element mdWrapElement = digiprovMDElement.addElement(new QName("mdWrap", nsMets));
        mdWrapElement.addAttribute("MDTYPE", "PREMIS");
        mdWrapElement.addAttribute("MIMETYPE", "text/xml");

        org.dom4j.Element xmlDataElement = mdWrapElement.addElement(new QName("xmlData", nsMets));

        org.dom4j.Element premisEvent = (org.dom4j.Element) node;

        xmlDataElement.add(premisEvent.createCopy());
        objId++;
      }
    }
    return objId;
  }

  public int addPremisEvtSection(String cdmId, org.dom4j.Element amdSecElement, String pageName, String dirName, int objId) throws DocumentException, IOException {
    CDM cdm = new CDM();
    File premisFile = new File(cdm.getPremisDir(cdmId), PREMIS_PREFIX + dirName + "_" + pageName + ".xml");
    //org.dom4j.Document premisDocument = DocumentHelper.parseText(FileUtils.readFileToString(premisFile));
    org.dom4j.Document premisDocument = DocumentHelper.parseText(retriedReadFileToString(premisFile));
    XMLHelper.qualify(premisDocument, nsPremis);
    Namespace premisNamespace = premisDocument.getRootElement().getNamespaceForPrefix("");
    premisDocument.getRootElement().remove(premisNamespace);

    XPath xPath = premisDocument.createXPath("//premis:event");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("premis", nsPremis.getStringValue()));
    List<org.dom4j.Node> eventNodeList = xPath.selectNodes(premisDocument);
    for (org.dom4j.Node node : eventNodeList) {
      org.dom4j.Element digiprovMDElement = amdSecElement.addElement(new QName("digiprovMD", nsMets));
      digiprovMDElement.addAttribute("ID", format(EVT_ID_FORMAT, objId));

      org.dom4j.Element mdWrapElement = digiprovMDElement.addElement(new QName("mdWrap", nsMets));
      mdWrapElement.addAttribute("MDTYPE", "PREMIS");
      mdWrapElement.addAttribute("MIMETYPE", "text/xml");

      org.dom4j.Element xmlDataElement = mdWrapElement.addElement(new QName("xmlData", nsMets));

      org.dom4j.Element premisEvent = (org.dom4j.Element) node;
      xmlDataElement.add(premisEvent.createCopy());
      objId++;
    }
    return objId;
  }

  public int addLTPMigrationPremisAgentSection(String cdmId, org.dom4j.Element amdSecElement, String pageName, int agentId) throws DocumentException, IOException {
    CDM cdm = new CDM();
    IOFileFilter filter = new WildcardFileFilter("*" + pageName + ".xml");
    List<File> premisFiles = (List<File>) FileUtils.listFiles(cdm.getPremisDir(cdmId), filter, FileFilterUtils.trueFileFilter());
    for (File premisFile : premisFiles) {
      //org.dom4j.Document premisDocument = DocumentHelper.parseText(FileUtils.readFileToString(premisFile));
      org.dom4j.Document premisDocument = DocumentHelper.parseText(retriedReadFileToString(premisFile));
      XMLHelper.qualify(premisDocument, nsPremis);
      Namespace premisNamespace = premisDocument.getRootElement().getNamespaceForPrefix("");
      premisDocument.getRootElement().remove(premisNamespace);

      XPath xPath = premisDocument.createXPath("//premis:agent");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("premis", nsPremis.getStringValue()));
      List<org.dom4j.Node> agentNodeList = xPath.selectNodes(premisDocument);
      for (org.dom4j.Node node : agentNodeList) {
        org.dom4j.Element digiprovMDElement = amdSecElement.addElement(new QName("digiprovMD", nsMets));
        digiprovMDElement.addAttribute("ID", format(AGENT_ID_FORMAT, agentId));

        org.dom4j.Element mdWrapElement = digiprovMDElement.addElement(new QName("mdWrap", nsMets));
        mdWrapElement.addAttribute("MDTYPE", "PREMIS");
        mdWrapElement.addAttribute("MIMETYPE", "text/xml");

        org.dom4j.Element xmlDataElement = mdWrapElement.addElement(new QName("xmlData", nsMets));

        org.dom4j.Element premisEvent = (org.dom4j.Element) node;
        xmlDataElement.add(premisEvent.createCopy());
        agentId++;
      }
    }
    return agentId;
  }

  public int addPremisAgentSection(String cdmId, org.dom4j.Element amdSecElement, String pageName, String dirName, int agentId) throws DocumentException, IOException {
    CDM cdm = new CDM();
    File premisFile = new File(cdm.getPremisDir(cdmId), PREMIS_PREFIX + dirName + "_" + pageName + ".xml");
    //org.dom4j.Document premisDocument = DocumentHelper.parseText(FileUtils.readFileToString(premisFile));
    org.dom4j.Document premisDocument = DocumentHelper.parseText(retriedReadFileToString(premisFile));
    XMLHelper.qualify(premisDocument, nsPremis);
    Namespace premisNamespace = premisDocument.getRootElement().getNamespaceForPrefix("");
    premisDocument.getRootElement().remove(premisNamespace);

    XPath xPath = premisDocument.createXPath("//premis:agent");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("premis", nsPremis.getStringValue()));
    List<org.dom4j.Node> agentNodeList = xPath.selectNodes(premisDocument);
    for (org.dom4j.Node node : agentNodeList) {
      org.dom4j.Element digiprovMDElement = amdSecElement.addElement(new QName("digiprovMD", nsMets));
      digiprovMDElement.addAttribute("ID", format(AGENT_ID_FORMAT, agentId));

      org.dom4j.Element mdWrapElement = digiprovMDElement.addElement(new QName("mdWrap", nsMets));
      mdWrapElement.addAttribute("MDTYPE", "PREMIS");
      mdWrapElement.addAttribute("MIMETYPE", "text/xml");

      org.dom4j.Element xmlDataElement = mdWrapElement.addElement(new QName("xmlData", nsMets));

      org.dom4j.Element premisEvent = (org.dom4j.Element) node;
      xmlDataElement.add(premisEvent.createCopy());
      agentId++;
    }
    return agentId;
  }

  public void addAgent(File metsFile, String cdmId, String role, String type, String name) {
    LOG.info("Adding agent to METS header. Role: " + role + " Type:" + type);
    METSWrapper mw;

    try {
      Document metsDocument = XMLHelper.parseXML(metsFile);
      mw = new METSWrapper(metsDocument);
      METS mets = mw.getMETSObject();
      MetsHdr mh = mets.getMetsHdr();

      Agent agent = mh.newAgent();
      //Agent agent = mh.getAgents().get(0);      
      agent.setRole(role);
      agent.setType(type);
      agent.setName(name);
      mh.addAgent(agent);
      mets.setMetsHdr(mh);
      writeMetsWrapper(metsFile, mw);
      XMLHelper.pretyPrint(metsFile, true);

      LOG.info("Agent added. Role: " + role + " Type:" + type);
    }
    catch (Exception e) {
      throw new SystemException("Error while adding agent to METS", ErrorCodes.ADDING_AGENT_FAILED);
    }

  }

  public String getDocumentTitle(CDM cdm, String cdmId) throws CDMException, DocumentException {
    return getModsNodeValue("//mods:title", cdm, cdmId);
  }

  /**
   * Najde identifier podla typu z MODS casti METS.
   * 
   * @param cdm
   * @param cdmId
   * @param type
   * @return
   * @throws CDMException
   * @throws DocumentException
   */
  public String getIdentifierFromMods(CDM cdm, String cdmId, String type) throws CDMException, DocumentException {
    return getModsNodeValue("//mods:identifier[@type=\"" + type + "\"]", cdm, cdmId);
  }

  /**
   * Find issn identifier which have parent element mods:mods.
   * This method was added, because method getIdentifierFromMods always found first element of type issn in mets file.
   * It is incorrect behaviour, because in some cases first issn element belonged to related items.
   * 
   * @param cdm
   * @param cdmId
   * @return issn value or null
   * @throws CDMException
   * @throws DocumentException
   */
  public String getIssnIdentifierFromMods(CDM cdm, String cdmId) throws CDMException, DocumentException {
    return getModsNodeValue("//mods:mods/mods:identifier[@type=\"issn\"]", cdm, cdmId);
  }

  /**
   * Najde identifier podla typu z MODS casti MODS_TITLE v METS.
   * 
   * @param cdm
   * @param cdmId
   * @param type
   * @return
   * @throws CDMException
   * @throws DocumentException
   */
  public String getIdentifierFromModsTitle(CDM cdm, String cdmId, String type) throws CDMException, DocumentException {
    return getModsNodeValue("//mods:mods[contains(@ID,'MODS_TITLE')]//mods:identifier[@type=\"" + type + "\"]", cdm, cdmId);
  }

  /**
   * Najde identifier podla type z MODS casti s ID sectionId.
   * 
   * @param cdm
   * @param cdmId
   * @param sectionId
   * @param type
   * @return
   */
  public String getIdentifierFromMods(CDM cdm, String cdmId, String sectionId, String type) throws CDMException, DocumentException, METSException, XPathExpressionException, ParserConfigurationException, SAXException, IOException {
    return getIdentifiersFromMods(cdm, cdmId, sectionId).get(type);
  }

  public String getIssuance(CDM cdm, String cdmId) throws CDMException, DocumentException {
    return getModsNodeValue("//mods:issuance", cdm, cdmId);
  }

  /**
   * Vrati zoznam vsetkych identifier elementov ako mapu kde kluc je typ identifier-a a hodnota je hodnota identifier-a.
   * Najde ich v MODS casti s ID sectionId.
   * 
   * @param cdm
   * @param cdmId
   * @param sectionId
   * @return
   */
  private Map<String, String> getIdentifiersFromMods(CDM cdm, String cdmId, String sectionId) throws CDMException, DocumentException, METSException, XPathExpressionException, ParserConfigurationException, SAXException, IOException {
    Map<String, String> result = new HashMap<String, String>();
    SAXReader saxReader = new SAXReader();
    File metsFile = cdm.getMetsFile(cdmId);
    if (!metsFile.exists()) {
      throw new SystemException("Mets file " + metsFile.getPath() + " does note exist", ErrorCodes.NO_METS_FILE);
    }
    org.dom4j.Document doc = saxReader.read(cdm.getMetsFile(cdmId));
    String address = "//mets:dmdSec[@ID=\"" + sectionId + "\"]";
    org.dom4j.Element elementMetsForMods = (org.dom4j.Element) getNodeDom4jMets(address, cdm, cdmId, doc);
    if (elementMetsForMods != null) {
      @SuppressWarnings("rawtypes")
      List nodes = ((org.dom4j.Node) elementMetsForMods).selectNodes("//mets:dmdSec[@ID=\"" + sectionId + "\"]/mets:mdWrap/mets:xmlData/mods:mods/mods:identifier");
      for (Object object : nodes) {
        if (object instanceof org.dom4j.Element) {
          org.dom4j.Element element = (org.dom4j.Element) object;
          String invalidValue = element.attributeValue("invalid");
          if (invalidValue != null && invalidValue.equalsIgnoreCase("yes")) {
            LOG.info("Invalid identifier: " + element.getText() + " skipping.");
            continue;
          }
          if (element.getText() != null && !element.getText().isEmpty()) {
            String indetifierValue = element.getText();
            String type = element.attributeValue("type");

            if (type.equalsIgnoreCase("uri")) {
              type = "http";
              indetifierValue = indetifierValue.substring("http".length() + 1);
            }
            else if (type.equalsIgnoreCase("isbn")) {
              type = "URN:ISBN";
            }
            result.put(type, indetifierValue);

          }
          else {
            LOG.debug("skippning element :" + element.getName());
          }
        }
      }
    }
    return result;
  }

  /**
   * Vrati zoznam vsetkych identifier elementov ako mapu kde kluc je typ identifier-a a hodnota je seznam hodnot
   * stejneho typu identifikatora. Nevraci nevalidne identifikatory
   * Najde ich v MODS casti s ID sectionId.
   * 
   * @param cdm
   * @param cdmId
   * @param sectionId
   * @return
   */
  private Map<String, List<String>> getAllIdentifiersFromMods(CDM cdm, String cdmId, String sectionId) throws CDMException, DocumentException, METSException, XPathExpressionException, ParserConfigurationException, SAXException, IOException {
    Map<String, List<String>> result = new HashMap<String, List<String>>();
    SAXReader saxReader = new SAXReader();
    File metsFile = cdm.getMetsFile(cdmId);
    if (!metsFile.exists()) {
      throw new SystemException("Mets file " + metsFile.getPath() + " does note exist", ErrorCodes.NO_METS_FILE);
    }
    org.dom4j.Document doc = saxReader.read(cdm.getMetsFile(cdmId));
    String address = "//mets:dmdSec[@ID=\"" + sectionId + "\"]";
    org.dom4j.Element elementMetsForMods = (org.dom4j.Element) getNodeDom4jMets(address, cdm, cdmId, doc);
    if (elementMetsForMods != null) {
      @SuppressWarnings("rawtypes")
      List nodes = ((org.dom4j.Node) elementMetsForMods).selectNodes("//mets:dmdSec[@ID=\"" + sectionId + "\"]/mets:mdWrap/mets:xmlData/mods:mods/mods:identifier");
      for (Object object : nodes) {
        if (object instanceof org.dom4j.Element) {
          org.dom4j.Element element = (org.dom4j.Element) object;
          String invalidValue = element.attributeValue("invalid");
          if (invalidValue != null && invalidValue.equalsIgnoreCase("yes")) {
            LOG.info("Invalid identifier: " + element.getText() + " skipping.");
            continue;
          }
          if (element.getText() != null && !element.getText().isEmpty()) {
            String indetifierValue = element.getText();
            String type = element.attributeValue("type");

            if (type.equalsIgnoreCase("uri")) {
              type = "http";
              indetifierValue = indetifierValue.substring("http".length() + 1);
            }
            else if (type.equalsIgnoreCase("isbn")) {
              type = "URN:ISBN";
            }
            List<String> valuesList = result.get(type);
            if (valuesList == null) {
              valuesList = new ArrayList<String>();
              result.put(type, valuesList);
            }
            result.get(type).add(indetifierValue);

          }
          else {
            LOG.debug("skippning element :" + element.getName());
          }
        }
      }
    }
    return result;
  }

  /**
   * Vrati zoznam vsetkych identifier elementov ako mapu kde kluc je typ identifier-a a hodnota je hodnota identifier-a.
   * Najde ich v DC casti s ID sectionId.
   * 
   * @param cdm
   * @param cdmId
   * @param sectionId
   * @return
   */
  private Map<String, String> getIdentifiersFromDC(CDM cdm, String cdmId, String sectionId) throws CDMException, DocumentException, METSException, XPathExpressionException, ParserConfigurationException, SAXException, IOException {
    List<Object> identifiersTypesObjList = TmConfig.instance().getList("cdm.cmdMetsHelper.identifierTypes");
    List<String> identifiersTypes = new ArrayList<String>();
    for (Object object : identifiersTypesObjList) {
      identifiersTypes.add((String) object);
    }

    Map<String, String> result = new HashMap<String, String>();
    SAXReader saxReader = new SAXReader();
    File metsFile = cdm.getMetsFile(cdmId);
    if (!metsFile.exists()) {
      throw new SystemException("Mets file " + metsFile.getPath() + " does note exist", ErrorCodes.NO_METS_FILE);
    }
    org.dom4j.Document doc = saxReader.read(cdm.getMetsFile(cdmId));
    String address = "//mets:dmdSec[@ID=\"" + sectionId + "\"]";
    org.dom4j.Element elementMetsForDC = (org.dom4j.Element) getNodeDom4jMets(address, cdm, cdmId, doc);
    if (elementMetsForDC != null) {
      @SuppressWarnings("rawtypes")
      List nodes = ((org.dom4j.Node) elementMetsForDC).selectNodes("//mets:dmdSec[@ID=\"" + sectionId + "\"]/mets:mdWrap/mets:xmlData/oai_dc:dc/dc:identifier");
      for (Object object : nodes) {
        if (object instanceof org.dom4j.Element) {
          org.dom4j.Element element = (org.dom4j.Element) object;
          String text = element.getText();
          boolean found = false;
          for (String string : identifiersTypes) {
            if (text.startsWith(string)) {
              String type;
              String indetifierValue = null;
              if (string.equalsIgnoreCase("http")) {
                type = "uri";
                indetifierValue = text;
              }
              else if (string.equalsIgnoreCase("urn:isbn")) {
                type = "isbn";
                if (string.length() + 1 < text.length()) {
                  indetifierValue = text.substring(string.length() + 1, text.length());
                }
              }
              else {
                type = text.substring(0, string.length());
                indetifierValue = text.substring(string.length() + 1, text.length());
              }
              if (indetifierValue != null) {
                result.put(type, indetifierValue);
              }
              found = true;
              break;
            }
          }

          if (!found) {
            LOG.info("Unknown identifier in dc: " + text);
          }
          found = false;
          /*if (!text.contains(":")) {
            throw new SystemException("Missing ':' in identifier: " + text);
          }
          String[] split = text.split(":");
          result.put(split[0], split[1]);*/
        }
      }
    }
    return result;
  }

  public Map<String, List<String>> getIdentifiersFromDC(File xml, String sectionId) throws CDMException, DocumentException, METSException, XPathExpressionException, ParserConfigurationException, SAXException, IOException {
    Map<String, List<String>> result = new HashMap<String, List<String>>();
    SAXReader saxReader = new SAXReader();
    File metsFile = xml;
    if (!metsFile.exists()) {
      throw new SystemException("Mets file " + metsFile.getPath() + " does note exist", ErrorCodes.NO_METS_FILE);
    }
    org.dom4j.Document doc = saxReader.read(metsFile);
    String address = "//mets:dmdSec[@ID=\"" + sectionId + "\"]";
    org.dom4j.Element elementMetsForDC = (org.dom4j.Element) getNodeDom4jMets(address, null, null, doc);
    if (elementMetsForDC != null) {
      @SuppressWarnings("rawtypes")
      List nodes = ((org.dom4j.Node) elementMetsForDC).selectNodes("//mets:dmdSec[@ID=\"" + sectionId + "\"]/mets:mdWrap/mets:xmlData/oai_dc:dc/dc:identifier");
      nodes.addAll(((org.dom4j.Node) elementMetsForDC).selectNodes("//mets:dmdSec[@ID=\"" + sectionId + "\"]/mets:mdWrap/mets:xmlData/oai_dc:dc/dc:title"));
      for (Object object : nodes) {
        if (object instanceof org.dom4j.Element) {
          org.dom4j.Element element = (org.dom4j.Element) object;
          String name = element.getName();
          String value = element.getText();
          if (!result.containsKey(name)) {
            result.put(name, new ArrayList<String>());
          }
          result.get(name).add(value);
        }
      }
    }
    return result;
  }

  public String getDocumentSigla(CDM cdm, String cdmId) throws CDMException, DocumentException {
    String sigla = null;
    sigla = getModsNodeValue("//mods:physicalLocation[@authority=\"siglaADR\"]", cdm, cdmId);
    if (sigla == null) {
      sigla = getModsNodeValue("//mods:recordContentSource", cdm, cdmId);
    }
    return sigla;
  }

  public String getDocumentSiglaForKrameriusImport(CDM cdm, String cdmId) throws CDMException, DocumentException {
    String sigla = null;
    sigla = getModsNodeValue("//mods:physicalLocation", cdm, cdmId);
    if (sigla == null) {
      sigla = getModsNodeValue("//mods:recordContentSource", cdm, cdmId);
    }
    return sigla;
  }

  public String getDocumentSiglaForKrameriusImport(CDM cdm, String cdmId, Document doc) throws CDMException, DocumentException {
    String sigla = null;
    sigla = getModsNodeValue("physicalLocation", cdm, doc);
    if (sigla == null) {
      sigla = getModsNodeValue("recordContentSource", cdm, doc);
    }
    return sigla;
  }

  public int getImageCount(CDM cdm, String cdmId) {
    try {
      return getFileSecMap(getMetsObject(cdm, cdmId)).get(FILE_GRP_ID_MC).size();
    }
    catch (Exception e) {
      LOG.error("Error while getting image count, exceptin message: " + e.getMessage());
      LOG.error("Stack trace: " + e.getStackTrace());
      throw new SystemException("Error while getting image count", ErrorCodes.IMAGE_COUNT_FAILED);
    }
  }

  public String getDocumentLanguage(CDM cdm, String cdmId) throws CDMException, DocumentException {
    return getModsNodeValue("//mods:languageTerm[@type=\"code\"]", cdm, cdmId);
  }

  public static String getModsNodeValue(String xPathExpression, CDM cdm, String cdmId) throws CDMException, DocumentException {
    Namespace nsMods = new Namespace("mods", "http://www.loc.gov/mods/v3");
    SAXReader reader = new SAXReader();
    org.dom4j.Document metsDocument = reader.read(cdm.getMetsFile(cdmId));
    LOG.debug(metsDocument.getXMLEncoding());
    XPath xPath = metsDocument.createXPath(xPathExpression);
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", nsMods.getStringValue()));
    org.dom4j.Node node = xPath.selectSingleNode(metsDocument);
    if (node == null) {
      return null;
    }
    return node.getText();
  }

  public static String getModsNodeValue(String nodeName, CDM cdm, Document metsDocument) throws CDMException, DocumentException {
    NodeList nodes = metsDocument.getElementsByTagNameNS("http://www.loc.gov/mods/v3", nodeName);
    for (int i = 0; i < nodes.getLength(); i++) {
      if (nodes.item(i).getTextContent() != null)
        return nodes.item(i).getTextContent();
    }
    return null;
  }

  public String getCcnb(String cdmId) {
    CDM cdm = new CDM();
    org.dom4j.Document doc = DocumentHelper.createDocument();

    SAXReader reader = new SAXReader();
    org.dom4j.Document metsDocument;
    try {
      metsDocument = reader.read(cdm.getMetsFile(cdmId));
      XPath xPath = DocumentHelper.createXPath("//mods:mods");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
      org.dom4j.Node node = xPath.selectSingleNode(metsDocument);
      doc.add((org.dom4j.Node) node.clone());

    }
    catch (Exception e) {
      e.printStackTrace();
    }
    XPath xPath = DocumentHelper.createXPath("//mods:identifier[@type='ccnb']");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mods", "http://www.loc.gov/mods/v3"));
    org.dom4j.Node node = xPath.selectSingleNode(doc);
    String ccnb = null;
    if (node != null) {
      ccnb = node.getText();
    }
    return ccnb;
  }

  public static void writeToFile(org.dom4j.Document doc, File file) throws IOException {
    final Writer fwe = new FileWriterWithEncoding(file, "UTF-8");
    final OutputFormat format = OutputFormat.createPrettyPrint();
    format.setEncoding("UTF-8");
    final XMLWriter xmlWriter = new XMLWriter(fwe, format);
    try {
      xmlWriter.write("\ufeff");
      xmlWriter.write(doc);
      xmlWriter.flush();
    }
    finally {
      xmlWriter.close();
    }
  }

  public org.dom4j.Node getNodeFromMets(String stringXPath, CDM cdm, String cdmId) {
    return getNodeFromMets(stringXPath, cdm, cdm.getMetsFile(cdmId));
  }

  public org.dom4j.Node getNodeFromMets(String stringXPath, CDM cdm, File metsFile) {

    try {
      Map<String, String> namespaces = new HashMap<String, String>();
      namespaces.put("mods", "http://www.loc.gov/mods/v3");
      namespaces.put("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
      namespaces.put("mets", "http://www.loc.gov/METS/");
      namespaces.put("xlink", "http://www.w3.org/1999/xlink");
      namespaces.put("dc", "http://purl.org/dc/elements/1.1/");

      SAXReader reader = new SAXReader();

      org.dom4j.Document metsDocument;

      metsDocument = reader.read(metsFile);
      //log.debug(metsDocument.getXMLEncoding());
      XPath xPath;
      org.dom4j.Node node;

      xPath = metsDocument.createXPath(stringXPath);
      xPath.setNamespaceURIs(namespaces);

      node = xPath.selectSingleNode(metsDocument);

      return node;
    }
    catch (Exception e) {
      LOG.error("Error (Exception: \"" + e.getClass() + "\" while getting value from mets file:" + e.getMessage());
      LOG.error("Stack trace: " + e.getStackTrace().toString());
      return null;
    }

  }

  public org.dom4j.Node getNodeFromMets(String stringXPath, CDM cdm, org.dom4j.Document metsDocument) {

    try {
      Map<String, String> namespaces = new HashMap<String, String>();
      namespaces.put("mods", "http://www.loc.gov/mods/v3");
      namespaces.put("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
      namespaces.put("mets", "http://www.loc.gov/METS/");
      namespaces.put("xlink", "http://www.w3.org/1999/xlink");
      namespaces.put("dc", "http://purl.org/dc/elements/1.1/");
      XPath xPath;
      org.dom4j.Node node;

      xPath = metsDocument.createXPath(stringXPath);
      xPath.setNamespaceURIs(namespaces);

      node = xPath.selectSingleNode(metsDocument);

      return node;
    }
    catch (Exception e) {
      LOG.error("Error (Exception: \"" + e.getClass() + "\" while getting value from mets file:" + e.getMessage());
      LOG.error("Stack trace: " + e.getStackTrace().toString());
      return null;
    }

  }

  public List<org.dom4j.Node> getNodesFromMets(String stringXPath, CDM cdm, String cdmId) {
    return getNodesFromMets(stringXPath, cdm, cdm.getMetsFile(cdmId));
  }

  public List<org.dom4j.Node> getNodesFromMets(String stringXPath, CDM cdm, File metsFile) {
    try {
      Map<String, String> namespaces = new HashMap<String, String>();
      namespaces.put("mods", "http://www.loc.gov/mods/v3");
      namespaces.put("mets", "http://www.loc.gov/METS/");
      namespaces.put("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
      namespaces.put("dc", "http://purl.org/dc/elements/1.1/");

      SAXReader reader = new SAXReader();

      org.dom4j.Document metsDocument;

      metsDocument = reader.read(metsFile);
      //log.debug(metsDocument.getXMLEncoding());
      XPath xPath;
      List<org.dom4j.Node> nodes;

      xPath = metsDocument.createXPath(stringXPath);
      xPath.setNamespaceURIs(namespaces);

      nodes = xPath.selectNodes(metsDocument);

      return nodes;
    }
    catch (Exception e) {
      LOG.error("Error (Exception: \"" + e.getClass() + "\" while getting value from mets file:" + e.getMessage());
      LOG.error("Stack trace: " + e.getStackTrace().toString());
      return null;
    }
  }

  public List<org.dom4j.Node> getNodesFromMets(String stringXPath, CDM cdm, org.dom4j.Document metsDocument) {
    try {
      Map<String, String> namespaces = new HashMap<String, String>();
      namespaces.put("mods", "http://www.loc.gov/mods/v3");
      namespaces.put("mets", "http://www.loc.gov/METS/");
      namespaces.put("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
      namespaces.put("dc", "http://purl.org/dc/elements/1.1/");
      //log.debug(metsDocument.getXMLEncoding());
      XPath xPath;
      List<org.dom4j.Node> nodes;

      xPath = metsDocument.createXPath(stringXPath);
      xPath.setNamespaceURIs(namespaces);
      nodes = xPath.selectNodes(metsDocument);
      return nodes;
    }
    catch (Exception e) {
      LOG.error("Error (Exception: \"" + e.getClass() + "\" while getting value from mets file:" + e.getMessage());
      LOG.error("Stack trace: " + e.getStackTrace().toString());
      return null;
    }
  }

  /**
   * Vrati identifikator suboru po premenovani
   * 
   * @author kovalm
   */
  public String getNewName(String oldName, String cdmId) {
    CDM cdm = new CDM();
    CsvReader csvRecords = null;
    try {
      File mappingFile = new File(cdm.getWorkspaceDir(cdmId) + File.separator + "renameMapping.csv");
      if (!mappingFile.exists())
        return oldName;
      csvRecords = new CsvReader(cdm.getWorkspaceDir(cdmId) + File.separator + "renameMapping.csv");
      csvRecords.setDelimiter(EmConstants.CSV_COLUMN_DELIMITER);
      csvRecords.setTrimWhitespace(true);
      csvRecords.setTextQualifier(EmConstants.CSV_TEXT_QUALIFIER);
      csvRecords.readHeaders();

      while (csvRecords.readRecord()) {
        if (csvRecords.get("old").equals(oldName)) {
          return csvRecords.get("new");
        }
      }
    }
    catch (IOException e) {
      LOG.error("Error while reading csv file.", e);
      throw new SystemException("Error while reading csv file.", ErrorCodes.CSV_READING);
    }
    finally {
      if (csvRecords != null) {
        csvRecords.close();
      }
    }

    return null;
  }

  /**
   * Vrati identifikator suboru pred premenovanim
   * 
   * @author kovalm
   */
  public String getOldName(String newName, String cdmId) {
    CDM cdm = new CDM();
    CsvReader csvRecords = null;
    try {
      File mappingFile = new File(cdm.getWorkspaceDir(cdmId) + File.separator + "renameMapping.csv");
      if (!mappingFile.exists())
        return null;
      csvRecords = new CsvReader(cdm.getWorkspaceDir(cdmId) + File.separator + "renameMapping.csv");
      csvRecords.setDelimiter(EmConstants.CSV_COLUMN_DELIMITER);
      csvRecords.setTrimWhitespace(true);
      csvRecords.setTextQualifier(EmConstants.CSV_TEXT_QUALIFIER);
      csvRecords.readHeaders();

      while (csvRecords.readRecord()) {
        if (csvRecords.get("new").equals(newName)) {
          return csvRecords.get("old");
        }
      }
    }
    catch (IOException e) {
      throw new SystemException("Error while reading csv file.", ErrorCodes.CSV_READING);
    }
    finally {
      if (csvRecords != null) {
        csvRecords.close();
      }
    }

    return null;
  }

  /**
   * Vrati pre subor z postprocessingData subor z flatData
   * 
   * @author kovalm
   */
  public String getFlatFileForPPFile(String cdmId, String filePP) {
    CDM cdm = new CDM();
    CsvReader csvRecords = null;
    try {
      File mappingFile = new File(cdm.getWorkspaceDir(cdmId) + File.separator + "mapping.csv");
      if (!mappingFile.exists())
        return null;
      csvRecords = new CsvReader(cdm.getWorkspaceDir(cdmId) + File.separator + "mapping.csv");
      csvRecords.setDelimiter(EmConstants.CSV_COLUMN_DELIMITER);
      csvRecords.setTrimWhitespace(true);
      csvRecords.setTextQualifier(EmConstants.CSV_TEXT_QUALIFIER);
      csvRecords.readHeaders();

      while (csvRecords.readRecord()) {
        if (csvRecords.get("postprocessingData").equals(filePP)) {
          String flatFileName = csvRecords.get("flatData");
          csvRecords.close();
          return flatFileName;
        }
      }
    }
    catch (IOException e) {
      throw new SystemException("Error while reading csv file.", ErrorCodes.CSV_READING);
    }
    finally {
      if (csvRecords != null) {
        csvRecords.close();
      }
    }
    return null;
  }

  //method for getting list od records from csv file
  public List<EmCsvRecord> getListFromEmCsv(String cdmId) {
    CDM cdm = new CDM();
    final List<EmCsvRecord> records = new ArrayList<EmCsvRecord>();
    File csvFile = cdm.getEmConfigFile(cdmId);
    try {
      final CsvReader reader = new CsvReader(new FileReader(csvFile));
      reader.setDelimiter(EmConstants.CSV_COLUMN_DELIMITER);
      reader.setTrimWhitespace(true);
      reader.setTextQualifier(EmConstants.CSV_TEXT_QUALIFIER);
      reader.setUseComments(true);
      reader.readHeaders();
      while (reader.readRecord()) {
        final EmCsvRecord record = new EmCsvRecord(reader.get("pageId"), reader.get("pageLabel"), EmPageType.valueOf(reader.get("pageType")), 1, reader.get("pageOrderLabel"), reader.get("dmdId"), reader.get("scanId"), reader.get("scanType"), reader.get("scanNote"), reader.get("admid"),
            reader.get("scanMode"), reader.get("profilOCR"), reader.get("OCRResult"));
        records.add(record);
      }

    }
    catch (final Exception e) {
      throw new SystemException(format("Reading CSV file %s failed", csvFile.getAbsolutePath()), ErrorCodes.CSV_READING);
    }
    return records;
  }

  public boolean isMultiPartMonograph(String cdmId) {
    CDM cdm = new CDM();
    return !Strings.isNullOrEmpty(cdm.getCdmProperties(cdmId).getProperty("Monograph type"));
  }

  public void addDummyStructMaps(File metsFile, CDM cdm, String cdmId) throws SAXException, IOException, ParserConfigurationException, METSException, CDMException, DocumentException {
    LOG.debug("Adding empty physical structure map from cdm " + cdmId + ", METS " + metsFile.getName());
    Document metsDocument = XMLHelper.parseXML(metsFile);
    METSWrapper mw = new METSWrapper(metsDocument);
    METS mets = mw.getMETSObject();
    // PHYSICAL
    StructMap sm = mets.newStructMap();
    sm.setLabel("Physical_Structure");
    sm.setType(CDMMetsHelper.STRUCT_MAP_TYPE_PHYSICAL);
    mets.addStructMap(sm);
    // 1 parent div
    Div pDiv = sm.newDiv();
    pDiv.setLabel(mets.getLabel());
    pDiv.setType(("Monogrpah".equals(mets.getType())) ? "MONOGRAPH_PAGE" : "PERIODICAL_PAGE");
    pDiv.setID("DIV_P_0000");
    pDiv.setDmdID(getSectionIdMods(cdmId));
    sm.addDiv(pDiv);
    Div d = pDiv.newDiv();
    d.setID(format(STRUCT_MAP_PHYSICAL_DIV_ID_FORMAT, 0));

    writeMetsWrapper(metsFile, mw);
    LOG.debug("Structure added");
  }

  public String getImportType(String cdmId) {
    if (isImportType(cdmId)) {
      CDM cdm = new CDM();
      return cdm.getCdmProperties(cdmId).getProperty("importType");
    }
    else {
      //It is not import type
      return null;
    }
  }

  public boolean isImportType(String cdmId) {
    CDM cdm = new CDM();
    return !Strings.isNullOrEmpty(cdm.getCdmProperties(cdmId).getProperty("importType"));
  }

  /**
   * Reprezentuje jeden file z fileSec v METS.
   * 
   * @author Rudolf Daco
   */
  public class FileSecFile implements Comparable<FileSecFile> {
    /**
     * Id elementu file.
     */
    private String id;
    /**
     * Url z elementu FLocat elementu file (FLocat moze byt viac v nednom file podla xsd, ale my uvezujeme vzdy iba o
     * jednom)
     */
    private String url;
    /**
     * Seq z elementu file.
     */
    private Integer seq;

    public FileSecFile(String id, String url, Integer seq) {
      super();
      this.id = id;
      this.url = url;
      this.seq = seq;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public Integer getSeq() {
      return seq;
    }

    public void setSeq(Integer seq) {
      this.seq = seq;
    }

    @Override
    public int compareTo(FileSecFile f) {
      return this.seq.compareTo(f.getSeq());
    }

    public String toString() {
      return id + " : " + url + " : " + seq;
    }
  }

  @RetryOnFailure(attempts = 3)
  private String retriedReadFileToString(File file) throws IOException {
    return FileUtils.readFileToString(file, "UTF-8");
  }

  public static void main(String[] args) {
    try {          
    CDM cdm=new CDM();
    CDMMetsHelper helper=new CDMMetsHelper();
    String cdmId="dd97d2b0-51b2-11e3-ae59-005056827e52";
   
    String label = "More v plamenech, 1974";
    CsvReader csvRecords = null;
    csvRecords = new CsvReader(cdm.getWorkspaceDir(cdmId) + File.separator + "renameMapping.csv");
    csvRecords.setDelimiter(EmConstants.CSV_COLUMN_DELIMITER);
    csvRecords.setTrimWhitespace(true);
    csvRecords.setTextQualifier(EmConstants.CSV_TEXT_QUALIFIER);
    csvRecords.readHeaders();

    List<String> ppFiles=new ArrayList<String>();
    List<String> flatFiles=new ArrayList<String>();
    while (csvRecords.readRecord()) {
      ppFiles.add(csvRecords.get("old"));
      flatFiles.add(csvRecords.get("new"));      
    }
   
    
    helper.createMETSForImages(cdmId, label, cdm.getPostprocessingDataDir(cdmId), ppFiles, flatFiles);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  public void createMETSForImages(final String cdmId, final String label, final File inDir, List<String> inFiles, List<String> flatFiles) {
    checkNotNull(cdmId, "cdmId must not be null");
    LOG.debug("createMETSForImages started for: " + cdmId);

   // FileFilter filter = new WildcardFileFilter(new String[] { "*.tif", "*.jp2", "*.tiff" }, IOCase.INSENSITIVE); // TODO ondrusekl (30.4.2012): Bude toho vice?
    int pageCounter = 0;
    for (String file : inFiles) {
//      if (!filter.accept(file)) {
//        continue;
//      }
      pageCounter = createMETSForImage(file, pageCounter, cdmId, label, inDir, flatFiles);
    }
    
  }
  public int createMETSForImage(String file, int pageCounter, String cdmId, String label, File inDir, List<String> flatFiles) {
    CDM cdm = new CDM();
    final File outDir = cdm.getAmdDir(cdmId);

    LOG.debug("Going to create amdSec for file: " + file);
    System.out.println(file);
    String pageName = file;

    LOG.debug("pageName:" + pageName);
    try {

      // <mets:mets xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:premis="info:lc/xmlns/premis-v2" xmlns:mix="http://www.loc.gov/mix/v20" xsi:schemaLocation="http://www.w3.org/2001/XMLSchema-instance http://www.w3.org/2001/XMLSchema.xsd http://www.loc.gov/METS/ http://www.loc.gov/standards/mets/mets.xsd http://www.loc.gov/mix/v20 http://www.loc.gov/standards/mix/mix20/mix20.xsd info:lc/xmlns/premis-v2 http://www.loc.gov/standards/premis/premis.xsd" TYPE="Periodical" xmlns:mets="http://www.loc.gov/METS/">

      int mixId = 1;

      org.dom4j.Document document = DocumentHelper.createDocument();
      org.dom4j.Element metsElement = document.addElement(new QName("mets", nsMets));
      metsElement.add(nsPremis);
      metsElement.add(nsMix);
      metsElement.add(nsXsi);

      metsElement
          .addAttribute(
              "xsi:schemaLocation",
              "http://www.w3.org/2001/XMLSchema-instance http://www.w3.org/2001/XMLSchema.xsd http://www.loc.gov/METS/ http://www.loc.gov/standards/mets/mets.xsd http://www.loc.gov/mix/v20 http://www.loc.gov/standards/mix/mix20/mix20.xsd info:lc/xmlns/premis-v2 http://www.loc.gov/standards/premis/premis.xsd");

      metsElement.addAttribute("TYPE", getDocumentType(cdmId));
      Element mainMods = null;

      metsElement.addAttribute("LABEL", label);

      org.dom4j.Element metsHdrElement = metsElement.addElement(new QName("metsHdr", nsMets));
      XMLGregorianCalendar currentDate = DateUtils.toXmlDateTime(new Date());//******
      metsHdrElement.addAttribute("CREATEDATE", currentDate.toXMLFormat());
      metsHdrElement.addAttribute("LASTMODDATE", currentDate.toXMLFormat());

      org.dom4j.Element agentElement = metsHdrElement.addElement(new QName("agent", nsMets));
      agentElement.addAttribute("ROLE", "CREATOR");
      agentElement.addAttribute("TYPE", "ORGANIZATION");

      agentElement.addElement(new QName("name", nsMets))
          .setText("NDK_TM");

      org.dom4j.Element amdSecElement = metsElement.addElement(new QName("amdSec", nsMets));
      amdSecElement.addAttribute("ID", format(AMD_SEC_ID_FORMAT, pageCounter++));

      File premisDir;

      premisDir = cdm.getPremisDir(cdmId);

      if (premisDir.listFiles().length > 0) {

        if ("K4".equals(cdm.getCdmProperties(cdmId).getProperty("importType"))) {
          addPremisObjSection(cdmId, amdSecElement, pageName, CDMSchemaDir.ORIGINAL_DATA.getDirName(), 1);
          addPremisObjSection(cdmId, amdSecElement, getNewName(pageName, cdmId), inDir.getName(), 2);
          addPremisObjSection(cdmId, amdSecElement, getNewName(pageName, cdmId), CDMSchemaDir.MC_DIR.getDirName(), 3);
        }
        else {
          //TODO change values for OBJ_001
          addPremisObjSection(cdmId, amdSecElement, pageName, inDir.getName(), 1);

          // MasterCopy
          //TODO change values for OBJ_002
          addPremisObjSection(cdmId, amdSecElement, getNewName(pageName, cdmId), CDMSchemaDir.MC_DIR.getDirName(), 2);

          if (cdm.getAltoDir(cdmId).listFiles().length > 0) {
            // ALTO
            //TODO change values for OBJ_003
            addPremisObjSection(cdmId, amdSecElement, getNewName(pageName, cdmId), CDMSchemaDir.ALTO_DIR.getDirName(), 3);
          }
        }
      }
      else {
        //TODO je to spravne? Minimalne validace
        LOG.debug("Empty PREMIS Dir...");
      }

      if ("K4".equals(cdm.getCdmProperties(cdmId).getProperty("importType"))) {
        //******MIX_001 - ORIGINAL_DATA*********
        org.dom4j.Element mixTechMDElement = amdSecElement.addElement(new QName("techMD", nsMets));
        mixTechMDElement.addAttribute("ID", format("MIX_%03d", mixId++));

        org.dom4j.Element mixMdWrapElement = mixTechMDElement.addElement(new QName("mdWrap", nsMets));
        mixMdWrapElement.addAttribute("MDTYPE", "NISOIMG");
        mixMdWrapElement.addAttribute("MIMETYPE", "text/xml");

        File mixFile = null;
        mixFile = new File(cdm.getWorkspaceDir(cdmId), "mix/" + cdm.getOriginalDataDir(cdmId).getName() + "/" + FilenameUtils.removeExtension(file) + ".xml.mix");

        org.dom4j.Document mixDocument = DocumentHelper.parseText(retriedReadFileToString(mixFile));
        XMLHelper.qualify(mixDocument, nsMix);

        Namespace mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("");
        mixDocument.getRootElement().remove(mixNamespace);
        mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("mix");
        mixDocument.getRootElement().remove(mixNamespace);

        org.dom4j.Element mixXmlDataElement = mixMdWrapElement.addElement(new QName("xmlData", nsMets));
        mixXmlDataElement.add(mixDocument.getRootElement());

        //******MIX_002 MASTER_COPY_TIFF*********
        mixTechMDElement = amdSecElement.addElement(new QName("techMD", nsMets));
        mixTechMDElement.addAttribute("ID", format("MIX_%03d", mixId++));

        mixMdWrapElement = mixTechMDElement.addElement(new QName("mdWrap", nsMets));
        mixMdWrapElement.addAttribute("MDTYPE", "NISOIMG");
        mixMdWrapElement.addAttribute("MIMETYPE", "text/xml");

        mixFile = new File(cdm.getWorkspaceDir(cdmId), "mix/" + CDMSchemaDir.MASTER_COPY_TIFF_DIR.getDirName() + "/" + file + ".xml.mix");

        mixDocument = DocumentHelper.parseText(retriedReadFileToString(mixFile));
        XMLHelper.qualify(mixDocument, nsMix);

        mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("");
        mixDocument.getRootElement().remove(mixNamespace);
        mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("mix");
        mixDocument.getRootElement().remove(mixNamespace);

        mixXmlDataElement = mixMdWrapElement.addElement(new QName("xmlData", nsMets));
        mixXmlDataElement.add(mixDocument.getRootElement());

        //******MIX_003 MASTER_COPY*********
        mixTechMDElement = amdSecElement.addElement(new QName("techMD", nsMets));
        mixTechMDElement.addAttribute("ID", format("MIX_%03d", mixId++));

        mixMdWrapElement = mixTechMDElement.addElement(new QName("mdWrap", nsMets));
        mixMdWrapElement.addAttribute("MDTYPE", "NISOIMG");
        mixMdWrapElement.addAttribute("MIMETYPE", "text/xml");

        mixFile = new File(cdm.getWorkspaceDir(cdmId), "mix/" + cdm.getMasterCopyDir(cdmId).getName() + "/" + FilenameUtils.removeExtension(file) + ".tif.jp2.xml.mix");

        mixDocument = DocumentHelper.parseText(retriedReadFileToString(mixFile));
        XMLHelper.qualify(mixDocument, nsMix);

        mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("");
        mixDocument.getRootElement().remove(mixNamespace);
        mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("mix");
        mixDocument.getRootElement().remove(mixNamespace);

        mixXmlDataElement = mixMdWrapElement.addElement(new QName("xmlData", nsMets));
        mixXmlDataElement.add(mixDocument.getRootElement());
      }

      else {

        //******MIX_001*********
        org.dom4j.Element mixTechMDElement = amdSecElement.addElement(new QName("techMD", nsMets));
        mixTechMDElement.addAttribute("ID", format("MIX_%03d", mixId++));

        org.dom4j.Element mixMdWrapElement = mixTechMDElement.addElement(new QName("mdWrap", nsMets));
        mixMdWrapElement.addAttribute("MDTYPE", "NISOIMG");
        mixMdWrapElement.addAttribute("MIMETYPE", "text/xml");

        File mixFile = null;

        mixFile = new File(cdm.getWorkspaceDir(cdmId), "mix/" + cdm.getFlatDataDir(cdmId).getName() + "/" + getFlatFileForPPFile(cdmId, FilenameUtils.getBaseName(file)) + "." + "tif" + ".xml.mix");//****tif

        if (!mixFile.exists()) {
          if (cdm.getPostprocessingDataDir(cdmId).listFiles().length > 0) {
            mixFile = new File(cdm.getWorkspaceDir(cdmId), "mix/" + cdm.getPostprocessingDataDir(cdmId).getName() + "/" + file + ".xml.mix");
          }
          else {
            File mcTiffDir = new File(cdm.getWorkspaceDir(cdmId) + File.separator + CDMSchemaDir.MASTER_COPY_TIFF_DIR.getDirName());
            if (mcTiffDir.exists()) { //Import from Kramerius
              mixFile = new File(cdm.getWorkspaceDir(cdmId), "mix/" + mcTiffDir.getName() + "/" + file + ".xml.mix");
            }
            else {
              mixFile = new File(cdm.getWorkspaceDir(cdmId), "mix/" + cdm.getMasterCopyDir(cdmId).getName() + "/" + file + ".jp2.xml.mix");
            }
          }
        }
        org.dom4j.Document mixDocument = DocumentHelper.parseText(retriedReadFileToString(mixFile));
        XMLHelper.qualify(mixDocument, nsMix);

        Namespace mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("");
        mixDocument.getRootElement().remove(mixNamespace);
        mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("mix");
        mixDocument.getRootElement().remove(mixNamespace);

        org.dom4j.Element mixXmlDataElement = mixMdWrapElement.addElement(new QName("xmlData", nsMets));
        mixXmlDataElement.add(mixDocument.getRootElement());

        //******MIX_002*********
        mixTechMDElement = amdSecElement.addElement(new QName("techMD", nsMets));
        mixTechMDElement.addAttribute("ID", format("MIX_%03d", mixId++));

        mixMdWrapElement = mixTechMDElement.addElement(new QName("mdWrap", nsMets));
        mixMdWrapElement.addAttribute("MDTYPE", "NISOIMG");
        mixMdWrapElement.addAttribute("MIMETYPE", "text/xml");

        mixFile = new File(cdm.getWorkspaceDir(cdmId), "mix/" + cdm.getMasterCopyDir(cdmId).getName() + "/" + file + ".tif.jp2.xml.mix");

        mixDocument = DocumentHelper.parseText(retriedReadFileToString(mixFile));
        XMLHelper.qualify(mixDocument, nsMix);

        mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("");
        mixDocument.getRootElement().remove(mixNamespace);
        mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("mix");
        mixDocument.getRootElement().remove(mixNamespace);

        mixXmlDataElement = mixMdWrapElement.addElement(new QName("xmlData", nsMets));
        mixXmlDataElement.add(mixDocument.getRootElement());
      }

      String importType = getImportType(cdmId);
      //*****************Events****************************
      int evtId = 1;
      File premisFlatFile = new File(cdm.getPremisDir(cdmId), PREMIS_PREFIX + CDMSchemaDir.FLAT_DATA_DIR.getDirName() + "_" + getFlatFileForPPFile(cdmId, pageName) + ".xml");
      if (premisFlatFile.exists()) {
        evtId = addPremisEvtSection(cdmId, amdSecElement, getFlatFileForPPFile(cdmId, pageName), CDMSchemaDir.FLAT_DATA_DIR.getDirName(), evtId);
      }
      if (importType != null && importType.equals(DOCUMENT_TYPE_K4)) {
        evtId = addPremisEvtSection(cdmId, amdSecElement, pageName, CDMSchemaDir.ORIGINAL_DATA.getDirName(), evtId);
      }
      evtId = addPremisEvtSection(cdmId, amdSecElement, pageName, inDir.getName(), evtId);
      evtId = addPremisEvtSection(cdmId, amdSecElement, getNewName(pageName, cdmId), CDMSchemaDir.MC_DIR.getDirName(), evtId);
      //addPremisEvtSection(cdmId, amdSecElement, pageName, CDMSchemaDir.UC_DIR.getDirName(), evtId++);
      if (cdm.getAltoDir(cdmId).listFiles().length > 0) { // if not import
        evtId = addPremisEvtSection(cdmId, amdSecElement, getNewName(pageName, cdmId), CDMSchemaDir.ALTO_DIR.getDirName(), evtId);
      }
//    addPremisEvtSection(cdmId, amdSecElement, CDMSchemaDir.ORIGINAL_DATA.getDirName(), CDMSchemaDir.ORIGINAL_DATA.getDirName(), evtId++);

      //*****************Agents****************************
      int agentId = 1;
    //  boolean isPackageType = (cdm.getCdmProperties(cdmId).getProperty("importType") != null && cdm.getCdmProperties(cdmId).getProperty("importType").equals("PACKAGE")) ? true : false;
      if (flatFiles.size() > 0) {
        //Check if flag update from ltp exist
        if(true){//****************
       // if (!(ImportFromLTPHelper.isFromLtpImport(file, cdmId))) {
          //String importType = getImportType(cdmId);
          if (importType != null && importType.equals(DOCUMENT_TYPE_K4)) {
            agentId = addPremisAgentSection(cdmId, amdSecElement, pageName, CDMSchemaDir.ORIGINAL_DATA.getDirName(), agentId);
          }
          else {
            agentId = addPremisAgentSection(cdmId, amdSecElement, getFlatFileForPPFile(cdmId, pageName), CDMSchemaDir.FLAT_DATA_DIR.getDirName(), agentId);
          }
        }
      }
      else {
        throw new SystemException("No images in flatData. cdmId: " + cdmId + "or in parent of this entity..", ErrorCodes.FILE_NOT_FOUND);
      }
      agentId = addPremisAgentSection(cdmId, amdSecElement, pageName, inDir.getName(), agentId++);
      agentId = addPremisAgentSection(cdmId, amdSecElement, getNewName(pageName, cdmId), CDMSchemaDir.MC_DIR.getDirName(), agentId++);
      //addPremisAgentSection(cdmId, amdSecElement, pageName, CDMSchemaDir.UC_DIR.getDirName(), agentId++);
      if (cdm.getAltoDir(cdmId).listFiles().length > 0) { // if not import
        agentId = addPremisAgentSection(cdmId, amdSecElement, getNewName(pageName, cdmId), CDMSchemaDir.ALTO_DIR.getDirName(), agentId++);
      }

      File outputFile = new File(outDir, AMD_METS_FILE_PREFIX + getNewName(pageName, cdmId) + ".xml");

      writeToFile(document, outputFile);

      LOG.info("Write METS file for page {} into file {}", pageName, outputFile);

    }
    catch (Exception e) {
      LOG.error(format("Write METS file for %s failed. ", file) + e, e);
      throw new SystemException(format("Write METS file for %s failed.", file), e);
    }

    return pageCounter;

  }
  
}
