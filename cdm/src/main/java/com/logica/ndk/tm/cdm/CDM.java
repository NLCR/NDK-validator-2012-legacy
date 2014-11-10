package com.logica.ndk.tm.cdm;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.METSWrapper;

import com.csvreader.CsvWriter;
import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.commons.utils.DateUtils;
import com.logica.ndk.commons.utils.cli.SysCommandExecutor;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.premis.PremisConstants;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord;

/**
 * Helper for CDM accessing and manipulation. Hides real CDM location, and CDM
 * is identified by ID (cdmId). Mapping to physical path is defined by base
 * directory. Property baseDir is either explicitly set in constructor or defined by system property
 * <p>
 * Configurable properties:<br>
 * cdm.baseDirHashTable - base dir for CDM packages (if not set explicitly); defaults to java temp dir<br>
 * cdm.recycleSubDir - recycle sub dir; defaults to java temp dir<br>
 * 
 * @author rse
 */
public class CDM {

  private static final Logger LOG = LoggerFactory.getLogger(CDM.class);

  private static final String BASE_DIR_HASH_TABLE_PROPERTY = "cdm.baseDirHashTable";
  private static final String RECYCLE_SUBDIR = TmConfig.instance().getString("cdm.recycleSubDir");
  /**
   * CDM base dir hash table. Sluzi na ukladanie CDM-iek do viac lokacii. Base dir sa ziskava pre dane cdm podla tejto
   * tabulky. Klucom je posledny znak MD5_hex(cdmId). Hodnota je lokacia pre dane cdmId.
   */
  private static final Properties BASE_DIR_HASH_TABLE = TmConfig.instance().getProperties(BASE_DIR_HASH_TABLE_PROPERTY);
  private static final String[] IGNORED_DIRS = {
      CDMConstants.WORKSPACE_DIR_NAME, CDMConstants.TMP_DIR_NAME,
      CDMConstants.BACKUP_DIR_NAME };
  private static final String CDM_PROPERTIES_NAME = "cdmProperties.xml";
  private static final String CDM_VERSION = "1";
  // CDM property names
  private static final String PROPERTY_CDM_REFS = "referencedCdms";
  private static final String PROPERTY_UUID = "uuid";
  private static final String PROPERTY_BARCODE = "barCode";
  public static final String ID_TYPE_TITLE = "MODS_TITLE_0001";
  public static final String ID_TYPE_VOLUME = "MODS_VOLUME_0001";

  public static final String ALEPH_HDR_XPATH = "//oai_marc/fixfield[@id='LDR']";
  public static final int MULTIPART_MONOGRAPH_CHAR_HEADER_INDEX = 19;
  public static final char MULTIPART_MONOGRAPH_CHAR_HEADER_VALUE = 'a';

  public static final String CYGWIN_BIN = TmConfig.instance().getString("cygwinBin", null);
  public static final boolean RESOLVE_LINKS = TmConfig.instance().getBoolean("cdm.symlinks.resolve");

  // CDM schema for this instance of CDM
  private final CDMSchema cdmSchema;

  // CDM base dir; should be definned by constructor or is null; must exist
  private final File baseDir;

  // METS helper
//  private final CDMMetsHelper metsHelper = new CDMMetsHelper();

  // METS helper
  private final CDMBagItHelper bagitHelper = new CDMBagItHelper();

  private static final int priorityCount;
  private static final HashMap<Integer, List<File>> storages = new HashMap<Integer, List<File>>();
  private static final long diskSpaceTreshold;
  private static final String linkedCdmDirName;
  private static final String symlinkDesc;
  private static final String symlinkCmd;
  private static final boolean useLinks;
  private static final Random dice = new Random(System.currentTimeMillis());

  private static Map<String, File> realCDMDirs = new ConcurrentHashMap<String, File>();

  static {
    useLinks = TmConfig.instance().getBoolean("cdm.symlinks.enabled");
    priorityCount = TmConfig.instance().getInt("cdm.storages.priorityCount");
    for (int i = 0; i < priorityCount; i++) {
      ArrayList<File> list = new ArrayList<File>();
      for (Object storageName : TmConfig.instance().getList("cdm.storages.priority" + String.valueOf(i))) {
        list.add(new File(storageName.toString()));
      }
      storages.put(Integer.valueOf(i), list);
    }
    diskSpaceTreshold = TmConfig.instance().getLong("cdm.storages.diskSpaceTreshold");
    linkedCdmDirName = TmConfig.instance().getString("cdm.symlinks.linkedCdmDir");
    symlinkDesc = TmConfig.instance().getString("cdm.symlinks.description");
    symlinkCmd = TmConfig.instance().getString("cdm.symlinks.command");
  }

  /**
   * Constructor.
   * 
   * @parm cdmSchema Explicity CDM schema
   */
  public CDM(final File baseDir, final CDMSchema cdmSchema) {
    this.baseDir = baseDir;
    this.cdmSchema = cdmSchema;
  }

  /**
   * Constructor. BaseDir is to null, baseDir will be used from configuration based on cdmId.
   * 
   * @parm cdmSchema Explicity CDM schema
   */
  public CDM(final CDMSchema cdmSchema) {
    this(null, cdmSchema);
  }

  /**
   * Default constructor. Uses default CDMSchema.
   */
  public CDM() {
    this(new CDMSchema());
  }

  // =========================================================================
  // CDM life cycle
  // =========================================================================

  /**
   * Creates "emtpy" CDM structure, which is minimal and valid.
   * 
   * @param cdmId
   *          CDM Identifier
   * @param overwrite
   *          Overwrite if exists (use with care!)
   */
  public void createEmptyCdm(final String cdmId, final boolean overwrite)
      throws CDMException {
    final File cdmDir = getCdmLinkDir(cdmId);
    if (cdmDir.exists()) {
      LOG.debug("cdmDir exists: " + cdmDir.getAbsolutePath());
      if (overwrite) {
        try {
          LOG.debug("Going to delete: " + cdmDir.getAbsolutePath());
          retriedDeleteDirectory(cdmDir);
        }
        catch (final IOException ex) {
          throw new CDMException("Can't delete CDM directory: "
              + cdmDir);
        }
      }
      else {
        throw new CDMException("CDM directory already exists: "
            + cdmDir);
      }
    }

    if (useLinks) {
      createLink(getBaseDirForLink(cdmId), cdmDir, overwrite);
    }
    else {
      if (!cdmDir.mkdirs()) {
        throw new CDMException("Can't create CDM directory: " + cdmDir);
      }
    }

    for (final File mandatoryDir : getMandatoryDirs(cdmId)) {
      if (!mandatoryDir.mkdirs()) {
        throw new CDMException("Can't create directory: "
            + mandatoryDir);
      }
    }
    //createMetsFromContent(cdmId, false);
    createInitialProperties(cdmDir);
    bagitHelper.createBagInPlace(cdmDir, Arrays.asList(IGNORED_DIRS));
    /*
    if (!validateCdm(cdmId)) {
      throw new CDMException("Created CDM is not valid: " + cdmId);
    }
    */
  }

  /**
   * Updates CDM structure.
   * 
   * @param cdmId
   *          CDM Identifier
   * @param overwrite
   *          Overwrite if exists (use with care!)
   */
  public void updateCdm(final String cdmId)
      throws CDMException {
    final File cdmDir = getCdmDir(cdmId);
    //TODO is this correct?
    //JUST update, do not validate - validations are called separately
//    if (!validateCdm(cdmId, false)) {
//      throw new CDMException("Current CDM is not valid: " + cdmId);
//    }
    bagitHelper.updateBagInPlace(cdmDir, Arrays.asList(IGNORED_DIRS));
    //TODO is this correct?
//    if (!validateCdm(cdmId, true)) {
//      throw new CDMException("Update CDM is not valid: " + cdmId);
//    }
  }

  /**
   * Deletes CDM. In fact, it moves whole CDM structure to some sort of
   * "Recycle Bin".
   * 
   * @param cdmId
   *          CDM Identifier
   * @returns Directory CDM was moved to (as a recycleBin subdirectory)
   */
  public File deleteCdm(final String cdmId) throws CDMException {
    final File cdmDir = getCdmLinkDir(cdmId);
    File subdir = null;
    if (cdmDir.exists()) {
      File recycleBinDir = getRecycleBinDir(cdmId);
      if (!recycleBinDir.exists() && !recycleBinDir.mkdirs()) {
        throw new CDMException("Can't create recycle bin directory: "
            + recycleBinDir);
      }
      subdir = new File(recycleBinDir, cdmDir.getName() + "-"
          + System.currentTimeMillis() + RandomUtils.nextInt());
      try {
        // as of Commons File Utils 2.0+, this is safe even for different file systems
        String linkTargetPath = getRealPath(cdmDir.getAbsolutePath());
        File linkedCdmsDir = new File(linkTargetPath);
        LOG.debug("Going to move cdm from _LINKED_CDM to recycle-bin: " + linkTargetPath);
        //FileUtils.moveDirectory(linkedCdmsDir, subdir);
        retriedMoveDirectory(linkedCdmsDir, subdir);
        //FileUtils.deleteQuietly(cdmDir);
        retriedDeleteQuietly(cdmDir);
      }
      catch (final IOException ex) {
//        throw new CDMException("Can't move directory " + cdmDir
//            + " to recycle bin " + subdir, ex);
        LOG.error("Can't move directory " + cdmDir + " to recycle bin " + subdir, ex);
      }
    }
    return subdir;
  }

  /**
   * Deletes CDM directly, permanently. Now way back, sorry. Use deleteCdm to move it to recycleBin.
   * 
   * @param cdmId
   *          CDM Identifier
   */
  public void zapCdm(final String cdmId) {
    //FileUtils.deleteQuietly(getCdmDir(cdmId));
    retriedDeleteQuietly(getCdmDir(cdmId));
  }

  public void deleteJpgTiffImages(final String cdmId) {
    File jpgTiffImagePath = getJpgTiffImagePath(cdmId);
    if (jpgTiffImagePath != null) {
      File jpgTiffCdmDir = jpgTiffImagePath.getParentFile();
      try {
        if (jpgTiffCdmDir.exists())
          //FileUtils.deleteDirectory(jpgTiffCdmDir);
          retriedDeleteDirectory(jpgTiffCdmDir);
      }
      catch (IOException e) {
        throw new CDMException("Can't delete JPG-TIFF directory " + jpgTiffCdmDir);
      }
    }
  }

  // =========================================================================
  // CDM resource management
  // =========================================================================

  /**
   * Creates new METS file from existing content.
   * 
   * @param cdmId
   *          CDM Identifier
   */
  public File createMetsFromContent(final String cdmId, final boolean overwrite) throws CDMException {
    final File result = getMetsFile(cdmId);

    if (result.exists() && !overwrite) {
      throw new CDMException("METS file already exists: " + result);
    }

    FileInputStream fileInputStream = null;
    try {
      CDM cdm = new CDM();
      fileInputStream = new FileInputStream(cdm.getAlephFile(cdmId));
      CDMMetsHelper metsHelper = new CDMMetsHelper();

      String genre, idType;

      if (metsHelper.getDocumentTypeFromAleph(fileInputStream).equalsIgnoreCase(CDMMetsHelper.DOCUMENT_TYPE_PERIODICAL)) {
        genre = "title";
        idType = ID_TYPE_TITLE;
      }
      else {
        genre = "volume";
        idType = ID_TYPE_VOLUME;
      }

      Document mods = getMods(cdmId, CDMMarc2Mods.XSL_MARC21_TO_MODS, genre, idType);
      //writeMods(mods, cdmId);

      METSWrapper mw = metsHelper.exportToMETS(result, this, cdmId, mods);

      FileInputStream alephFileInputStream = null;
      try {
        alephFileInputStream = new FileInputStream(cdm.getAlephFile(cdmId));

        if (isMultiPartMonograph(cdm.getAlephFile(cdmId))) {
          updateProperty(cdmId, "Monograph type", "multipart monograph");
          Document multiPartMonograph = getMods(cdmId, CDMMarc2Mods.XSL_MARC21_TO_MULTI_PART_MONOGRAPH_TITLE_MODS, "title", ID_TYPE_TITLE);
          //Document multiPartMonographDc = metsHelper.createDCElementFromMods(multiPartMonograph);
          metsHelper.addDmdSecs(mw, multiPartMonograph, CDMMetsHelper.DMDSEC_ID_MODS_TITLE);
          //metsHelper.addDmdSecs(mw, multiPartMonographDc, CDMMetsHelper.DMDSEC_ID_DC_TITLE);
          FileOutputStream fileOutputStream = null;
          try {
            fileOutputStream = new FileOutputStream(result);
            mw.write(fileOutputStream);
            modifyResultInMultiPartMonograthAndSave(result);
          }
          finally {
            IOUtils.closeQuietly(fileOutputStream);
          }
        }
        else if (metsHelper.getDocumentTypeFromAleph(alephFileInputStream).equalsIgnoreCase(CDMMetsHelper.DOCUMENT_TYPE_PERIODICAL)) {
          Document multiPartMonograph = getMods(cdmId, CDMMarc2Mods.XSL_MARC21_TO_PERIODICAL_TITLE_MODS, "volume", ID_TYPE_VOLUME);
          //Document multiPartMonographDc = metsHelper.createDCElementFromMods(multiPartMonograph);
          metsHelper.addDmdSecs(mw, multiPartMonograph, CDMMetsHelper.DMDSEC_ID_MODS_VOLUME);
          //metsHelper.addDmdSecs(mw, multiPartMonographDc, CDMMetsHelper.DMDSEC_ID_DC_VOLUME);
          FileOutputStream fileOutputStream = null;
          try {
            fileOutputStream = new FileOutputStream(result);
            mw.write(fileOutputStream);
          }
          finally {
            IOUtils.closeQuietly(fileOutputStream);
          }
        }
      }
      finally {
        IOUtils.closeQuietly(alephFileInputStream);
      }

      XMLHelper.pretyPrint(result);
      final Properties p = getCdmProperties(cdmId);
      final String uuid = p.getProperty(PROPERTY_UUID, null);
      if (uuid != null) {
        LOG.info("Inserting uuid " + uuid + " to MODS");
        metsHelper.addIdentifier(cdmId, "uuid", uuid);
      }
      final String barCode = p.getProperty(PROPERTY_BARCODE, null);
      if (barCode != null) {
        LOG.info("Inserting barCode " + barCode + " to MODS");
        metsHelper.addIdentifier(cdmId, "barCode", barCode);
      }
      metsHelper.consolidateIdentifiers(cdmId);
    }
    catch (final Exception ex) {
      LOG.error("Can't create METS", ex);
      throw new CDMException("Can't create METS", ex);
    }
    finally {
      IOUtils.closeQuietly(fileInputStream);
    }
    return result;
  }

  private void modifyResultInMultiPartMonograthAndSave(File result)
  {
    try {
      LOG.info("Removing values(partNumber, partName, dateIssued, extend, isbn) from multipart monograph mets");
      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
          .newInstance();
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      Document document = docBuilder.parse(result);
      removeValue("mods:dateIssued", false, document);
      removeValue("mods:identifier", true, document);
      removeValue("mods:partNumber", false, document);
      removeValue("mods:partName", false, document);
      removeValue("mods:extent", false, document);
      // save document
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource source = new DOMSource(document);
      StreamResult streamResult = new StreamResult(result);
      transformer.transform(source, streamResult);
      LOG.info("Removing values from multipart monograph complete, mets saved");
    }
    catch (ParserConfigurationException pce) {
      pce.printStackTrace();
    }
    catch (TransformerException tfe) {
      tfe.printStackTrace();
    }
    catch (IOException ioe) {
      ioe.printStackTrace();
    }
    catch (SAXException sae) {
      sae.printStackTrace();
    }

  }

  private void removeValue(String tag, boolean isIsbn, Document document)
  {
    NodeList nodeList = document.getElementsByTagName(tag);
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        if (!isIsbn) {
          LOG.info("Values from " + node.getNodeName() + " removed");
          node.getParentNode().removeChild(node);
          removeValue(tag, isIsbn, document);
          return;
        }
        else {
          for (int j = 0; j < node.getAttributes().getLength(); j++) {
            if (node.getAttributes().item(j).getNodeValue().equals("isbn") && node.getParentNode().getAttributes().getNamedItem("ID").toString().contains("MODS_VOLUME")) {
              LOG.info("Values from " + node.getNodeName() + " removed");
              node.getParentNode().removeChild(node);
              removeValue(tag, isIsbn, document);
              return;
            }
          }
        }
      }
    }
  }

  public static void main(String[] args) {
    CDM c = new CDM();
    String cdmId = "450c5de0-f874-11e3-b963-00505682629d";
    c.createMetsFromContent(cdmId, true);
  }

  public boolean isMultiPartMonograph(File alephFile) {
    Document alephDocument = null;
    try {
      alephDocument = XMLHelper.parseXML(alephFile);
    }
    catch (Exception e) {
      LOG.error("Exception while parsing aleph file", e);
      throw new SystemException("Exception while parsing aleph file", e);
    }

    XPath xPath = XPathFactory.newInstance().newXPath();
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
        if (arg0.equals("mods")) {
          return "http://www.loc.gov/mods/v3";
        }
        return null;
      }
    });
    try {
      String hdr = xPath.compile(ALEPH_HDR_XPATH).evaluate(alephDocument.getDocumentElement());
      char charAt = hdr.charAt(MULTIPART_MONOGRAPH_CHAR_HEADER_INDEX);
      return charAt == MULTIPART_MONOGRAPH_CHAR_HEADER_VALUE;
    }
    catch (XPathExpressionException e) {
      LOG.error("Error at expath expresiion", e);
      throw new SystemException("Error at expath expresiion", e);
    }
  }

  /**
   * Creates new METS file from existing WA CDM content.
   * 
   * @param cdmId
   *          CDM Identifier
   */
  public File createMetsWAFromContent(final String cdmId, final boolean overwrite, Document mods) throws CDMException {
    final File result = getMetsFile(cdmId);
    if (result.exists() && !overwrite) {
      throw new CDMException("METS file already exists: " + result);
    }
    try {
      CDMMetsWAHelper metsWAHelper = new CDMMetsWAHelper();
      metsWAHelper.createWAMets(result, this, cdmId, mods);
      XMLHelper.pretyPrint(result);
      CDM cdm = new CDM();

      File metsFile = cdm.getMetsFile(cdmId);
      if (!metsFile.exists()) {
        throw new SystemException("Mets file " + metsFile.getPath() + " does not exist", ErrorCodes.NO_METS_FILE);
      }
    }
    catch (final Exception ex) {
      throw new CDMException("Can't create METS", ex);
    }
    return result;
  }

  // =========================================================================
  // CDM events
  // =========================================================================

  public void addValidationEvent(final String cdmId, final PremisCsvRecord record) {
    checkNotNull(cdmId, "cdmId must not be null");

    final File dir = new File(getWorkspaceDir(cdmId),
        PremisConstants.VALIDATIONS_DIR);
    checkDir(dir);
    addEvent(new File(dir, record.getEventDir() + ".csv"), record);
  }

  public void addTransformationEvent(final String cdmId, final PremisCsvRecord record, String transformationIdentifier) {
    checkNotNull(cdmId, "cdmId must not be null");
    LOG.debug("Method addTransformationEvent started.");
    final File dir = new File(getWorkspaceDir(cdmId),
        PremisConstants.TRANSFORMATIONS_DIR);
    checkDir(dir);

    if (transformationIdentifier != null && !transformationIdentifier.isEmpty()) {
      addEvent(new File(dir, transformationIdentifier + ".csv"), record);
    }
    else {
      addEvent(new File(dir, record.getEventDir() + ".csv"), record);
    }
  }

  @RetryOnFailure(attempts = 2)
  private void addEvent(final File file, final PremisCsvRecord record) {
    checkNotNull(file, "file must not be null");
    checkNotNull(record, "record must not be null");

    LOG.debug("Adding event to: " + file);

    synchronized (file) {
      CsvWriter csvWriter = null;
      try {
        boolean fileExists = false;
        if (file.exists()) {
          fileExists = true;
        }
        csvWriter = new CsvWriter(new FileWriter(file, true), PremisConstants.CSV_COLUMN_DELIMITER);
        csvWriter.setTextQualifier(PremisConstants.CSV_TEXT_QUALIFIER);
        csvWriter.setForceQualifier(true);

        if (!fileExists) {
          csvWriter.writeComment(format(" Premis events log file. Created %s", DateUtils.toXmlDateTime(new Date()).toXMLFormat()));
          csvWriter.writeRecord(PremisCsvRecord.HEADER);
        }
        csvWriter.writeRecord(record.asCsvRecord());

      }
      catch (final Exception e) {
        LOG.error("Write into CSV file " + file + "\nEx:", e);
        throw new SystemException(format("Write into CSV file %s failed.", file), ErrorCodes.CSV_WRITING);
      }
      finally {
        if (csvWriter != null) {
          csvWriter.close();
        }
      }
    }

  }

  private void checkDir(final File dir) {
    checkNotNull(dir, "dir must not be null");

    if (!dir.exists()) {
      dir.mkdirs();
    }
  }

  // =========================================================================
  // CDM validation
  // =========================================================================

  /**
   * Validates CDM structure
   * 
   * @param cdmId
   *          CDM Identifier
   */
  // TODO: validation result should include list of error-ids and their
  // respective reasons
  public boolean validateCdm(final String cdmId, final boolean verifyBagit) throws CDMException {
    final File cdmDir = getCdmDir(cdmId);
    if (!cdmDir.exists() || !cdmDir.isDirectory()) {
      LOG.warn("CDM directory does not exist: " + cdmDir);
      return false;
    }
    for (final File mandatoryDir : getMandatoryDirs(cdmId)) {
      if (!mandatoryDir.exists()) {
        LOG.warn("Mandatory directory does not exist: " + mandatoryDir);
        return false;
      }
    }
    if (!getMetsFile(cdmId).exists()) {
      LOG.warn("METS filedoes not exist: " + getMetsFile(cdmId));
      return false;
    }
    if (verifyBagit) {
      final List<String> errors = bagitHelper.verifyBag(cdmDir);
      if (errors != null && !errors.isEmpty()) {
        LOG.warn("CDM bagit not valid: " + errors);
        return false;
      }
    }
    return true;
  }

  /**
   * Returns CDM properties. Properties are read form the disk.
   * 
   * @param cdmId
   *          CDMIdentifier
   */
  public Properties getCdmProperties(final String cdmId) {
    return loadCdmProperties(getCdmDir(cdmId));
  }

  /**
   * Updates property by supplied value. New key/value will be added, or
   * existing will be updated. Note that all existing properties will be read
   * from the file, processed and written back to the file.
   * 
   * @param cdmId
   *          CDMIdentifier
   * @param key
   *          Property key
   * @param value
   *          Property value
   */
  public void updateProperty(final String cdmId, final String key,
      final String value) {
    final File dir = getCdmDir(cdmId);
    final Properties p = loadCdmProperties(dir);
    p.setProperty(key, value);
    saveCdmProperties(dir, p);
  }

  /**
   * Updates properties by supplied values. New key/values will be added,
   * existing will be updated, the rest is unchanged. Note that all existing
   * properties will be read from the file, processed and written back to the
   * file.
   * 
   * @param cdmId
   *          CDMIdentifier
   * @param props
   *          Update values
   */
  public void updateProperties(final String cdmId, final Properties props) {
    final File dir = getCdmDir(cdmId);
    final Properties p = loadCdmProperties(dir);
    p.putAll(props);
    saveCdmProperties(dir, p);
  }

  // =========================================================================
  // CDM Resolver - returns physical location of specified resource(s)
  // =========================================================================

  /**
   * CDM package has no content itself, it is just a bag of references to
   * other CMDs.
   * 
   * @param cdmId
   * @return
   * @throws CDMException
   */
  public boolean isCompound(final String cdmId) throws CDMException {
    return getCdmProperties(cdmId).getProperty(PROPERTY_CDM_REFS, null) != null;
  }

  /**
   * If CDM is compound then it returns list of referenced CDM identifiers.
   * Throws an exception if it is not.
   * 
   * @param cdmId
   * @return Array of referenced CDM IDs.
   * @throws CDMException
   */
  public String[] getReferencedCdmList(final String cdmId)
      throws CDMException {
    final String refs = getCdmProperties(cdmId).getProperty(
        PROPERTY_CDM_REFS, null);
    if (refs == null || refs.trim().isEmpty()) {
      throw new CDMException("Not a compound CDM");
    }
    return refs.split("[,;]+", 0);
  }

  /**
   * Sets referenced CDM identifiers to this CDM. This CDM is compound, from
   * now on.
   * 
   * @param cdmId
   * @param refs
   *          Referenced CDM IDs
   * @return
   * @throws CDMException
   */
  public void setReferencedCdmList(final String cdmId, final String[] refs)
      throws CDMException {
    final Properties p = getCdmProperties(cdmId);
    p.setProperty(PROPERTY_CDM_REFS, StringUtils.join(refs, ","));
    updateProperties(cdmId, p);
  }

  /**
   * Returns base dir (CDM root) for specified CDM; usualy ${cdm.baseDir}.
   * 
   * @param cdmId
   * @return
   * @throws CDMException
   */
  public File getCdmLinkDir(final String cdmId) throws CDMException {
    File link = new File(getBaseDir(cdmId), CDMEncodeUtils.encodeForFilename(cdmSchema.getCdmDirPrefix() + cdmId));

    return link;
  }

  /**
   * Returns base dir link location (CDM root) for specified CDM; usualy ${cdm.baseDir}.
   * 
   * @param cdmId
   * @return
   * @throws CDMException
   */
  public File getCdmDir(final String cdmId) throws CDMException {
    File path;

    if (!RESOLVE_LINKS) {
      // If resolving not enabled, return the link location
      return getCdmLinkDir(cdmId);
    }

    // Try to locate cdm real path in a hash map
    if (realCDMDirs.containsKey(cdmId)) {
      path = realCDMDirs.get(cdmId);
      //try to find file - retry for sure
      if (!path.exists()) {
        if (!path.exists()) {
          path = resolveLink(cdmId);
        }
      }
    }
    else {
      path = resolveLink(cdmId);
    }

    return path;
  }

  private File resolveLink(final String cdmId) {
    File path;
    // If not found, resolve it and add it to the hash map
    File link = new File(getBaseDir(cdmId), CDMEncodeUtils.encodeForFilename(cdmSchema.getCdmDirPrefix() + cdmId));
    String realPath = getRealPath(link.getAbsolutePath());
    path = new File(realPath);
    realCDMDirs.put(cdmId, path);
    return path;
  }

  /**
   * Returns base data dir (under CDM root) for specified CDM; usualy
   * ${cdm.baseDir}/data.
   * 
   * @param cdmId
   * @return
   * @throws CDMException
   */
  public File getCdmDataDir(final String cdmId) throws CDMException {
    return new File(getCdmDir(cdmId), bagitHelper.getDataDirectory());
  }

  /**
   * Returns dir for GIT data. It's NOT created automatically. Retruned value
   * is ${cdm.gitDir}/${cdmId}.git.
   * 
   * @param cdmId
   * @return
   * @throws CDMException
   */
  public File getGitDir(final String cdmId) throws CDMException {
    return new File(TmConfig.instance().getString("cdm.gitDir"),
        CDMEncodeUtils.encodeForFilename(cdmId + ".git"));
  }

  /**
   * Returns dir with raw (unprocessed) image data.
   * 
   * @param cdmId
   * @return
   * @throws CDMException
   */
  public File getRawDataDir(final String cdmId) throws CDMException {
    return new File(getCdmDataDir(cdmId), cdmSchema.getRawDataDirName());
  }

  /**
   * Returns post processing image data dir.
   * 
   * @param cdmId
   * @return
   * @throws CDMException
   */
  public File getPostprocessingDataDir(final String cdmId)
      throws CDMException {
    return new File(getCdmDataDir(cdmId),
        cdmSchema.getPostprocessingDataDirName());
  }

  /**
   * Returns "master copy" dir for specified CDM
   * 
   * @param cdmId
   * @return
   * @throws CDMException
   */
  public File getMasterCopyDir(final String cdmId) throws CDMException {
    return new File(getCdmDataDir(cdmId), cdmSchema.getMasterCopyDirName());
  }

  /**
   * Returns "user copy" dir for specified CDM
   * 
   * @param cdmId
   * @return
   * @throws CDMException
   */
  public File getUserCopyDir(final String cdmId) throws CDMException {
    return new File(getCdmDataDir(cdmId), cdmSchema.getUserCopyDirName());
  }

  public File getImagesPDFDir(final String cdmId) throws CDMException {
    return new File(getCdmDataDir(cdmId), cdmSchema.getImagesPDFDirName());
  }

  /**
   * Returns thumbnails dir for specified CDM
   * 
   * @param cdmId
   * @return
   * @throws CDMException
   */
  public File getThumbnailDir(final String cdmId) throws CDMException {
    return new File(getCdmDataDir(cdmId), cdmSchema.getThumbnailDirName());
  }

  /**
   * Returns administrative metadata dir for specified CDM
   * 
   * @param cdmId
   * @return
   * @throws CDMException
   */
  public File getAmdDir(final String cdmId) throws CDMException {
    return new File(getCdmDataDir(cdmId), cdmSchema.getAmdDirName());
  }

  public File getAltoDir(final String cdmId) throws CDMException {
    return new File(getCdmDataDir(cdmId), cdmSchema.getAltoDirName());
  }

  public File getTxtDir(final String cdmId) throws CDMException {
    return new File(getCdmDataDir(cdmId), cdmSchema.getTxtDirName());
  }

  public File getLogsDir(final String cdmId) throws CDMException {
    return new File(getCdmDataDir(cdmId), cdmSchema.getLogsDirName());
  }

  public File getWarcsDataDir(final String cdmId) throws CDMException {
    return new File(getCdmDataDir(cdmId), cdmSchema.getWaDataDirName());
  }

  public File getLogsDataDir(final String cdmId) throws CDMException {
    return new File(getCdmDataDir(cdmId), cdmSchema.getWaDataDirName());
  }

  public File getMiscDir(final String cdmId) throws CDMException {
    return new File(getCdmDataDir(cdmId), cdmSchema.getMiscDirName());
  }

  public File getWorkspaceDir(final String cdmId) throws CDMException {
    return new File(getCdmDataDir(cdmId), CDMConstants.WORKSPACE_DIR_NAME);
  }

  public File getLtpLogDir(final String cdmId) throws CDMException {
    return new File(getWorkspaceDir(cdmId), cdmSchema.getLtpLogDirName());
  }

  public File getPremisDir(final String cdmId) throws CDMException {
    checkNotNull(cdmId, "cdmId must not be null");
    return new File(getWorkspaceDir(cdmId), cdmSchema.getPremisDirName());
  }

  public File getOcrDir(final String cdmId) throws CDMException {
    checkNotNull(cdmId, "cdmId must not be null");
    return new File(getWorkspaceDir(cdmId), cdmSchema.getOcrDirName());
  }

  public File getHardLinksToCreateFile(final String cdmId) throws CDMException {
    checkNotNull(cdmId, "cdmId must not be null");
    return new File(getWorkspaceDir(cdmId), cdmSchema.getHardLinksToCreateFileName());
  }

  public File getTransformationsDir(final String cdmId) throws CDMException {
    checkNotNull(cdmId, "cdmId must not be null");
    return new File(getWorkspaceDir(cdmId), cdmSchema.getTransformationsDirName());
  }

  public File getValidationDir(final String cdmId) throws CDMException {
    checkNotNull(cdmId, "cdmId must not be null");
    return new File(getWorkspaceDir(cdmId), cdmSchema.getValidationDirName());
  }

  public File getUrnDir(final String cdmId) throws CDMException {
    checkNotNull(cdmId, "cdmId must not be null");
    return new File(getWorkspaceDir(cdmId), cdmSchema.getUrnDirName());
  }

  public File getMixDir(final String cdmId) throws CDMException {
    checkNotNull(cdmId, "cdmId must not be null");
    return new File(getWorkspaceDir(cdmId), cdmSchema.getMixDirName());
  }

  public File getScansDir(final String cdmId) throws CDMException {
    checkNotNull(cdmId, "cdmId must not be null");
    return new File(getWorkspaceDir(cdmId), cdmSchema.getScansDirName());
  }

  public File getAlephNotificationDir(final String cdmId) throws CDMException {
    checkNotNull(cdmId, "cdmId must not be null");
    return new File(getWorkspaceDir(cdmId), cdmSchema.getAlephNotificationDirName());
  }

  public File getAlephNotificationResponseDir(final String cdmId) throws CDMException {
    checkNotNull(cdmId, "cdmId must not be null");
    return new File(getWorkspaceDir(cdmId), cdmSchema.getAlephNotificationResponseDirName());
  }

  public File getFlatDataDir(final String cdmId) throws CDMException {
    checkNotNull(cdmId, "cdmId must not be null");
    return new File(getCdmDataDir(cdmId), cdmSchema.getFlatDataDirName());
  }

  public File getScantailorConfigsDir(final String cdmId) throws CDMException {
    checkNotNull(cdmId, "cdmId must not be null");
    return new File(getWorkspaceDir(cdmId), cdmSchema.getScantailorConfigsDirName());
  }

  public File getScantailorTempOutDir(final String cdmId) throws CDMException {
    checkNotNull(cdmId, "cdmId must not be null");
    return new File(getScantailorConfigsDir(cdmId), cdmSchema.getScantailorTempOutDirName());
  }

  public File getBackupDir(final String cdmId) throws CDMException {
    checkNotNull(cdmId, "cdmId must not be null");
    return new File(getWorkspaceDir(cdmId), cdmSchema.getBackupDirName());
  }

  public File getResultsDir(final String cdmId) throws CDMException {
    checkNotNull(cdmId, "cdmId must not be null");
    return new File(getWorkspaceDir(cdmId), cdmSchema.getResultsDirName());
  }

  public File getSIP1Dir(final String cdmId) throws CDMException {
    checkNotNull(cdmId, "cdmId must not be null");
    return new File(getWorkspaceDir(cdmId), cdmSchema.getSIP1DirName());
  }

  public File getSIP2Dir(final String cdmId) throws CDMException {
    checkNotNull(cdmId, "cdmId must not be null");
    return new File(getWorkspaceDir(cdmId), cdmSchema.getSIP2DirName());
  }

  public File getPreviewDir(final String cdmId) throws CDMException {
    checkNotNull(cdmId, "cdmId must not be null");
    return new File(getCdmDataDir(cdmId), cdmSchema.getPreviewDirName());
  }

  public File getArcDir(final String cdmId) throws CDMException {
    return new File(getCdmDataDir(cdmId), cdmSchema.getArcDirName());
  }

  public File getWarcDir(final String cdmId) throws CDMException {
    return new File(getCdmDataDir(cdmId), cdmSchema.getWarcDirName());
  }

  public File getOriginalDataDir(final String cdmId) throws CDMException {
    checkNotNull(cdmId, "cdmId must not be null");
    return new File(getCdmDataDir(cdmId), cdmSchema.getOriginalDataDirName());
  }

  public File getRawDataArcDir(final String cdmId) throws CDMException {
    checkNotNull(cdmId, "cdmId must not be null");
    return new File(getRawDataDir(cdmId), cdmSchema.getArcDirName());
  }

  /**
   * Returns fixity file for specified CDM
   * 
   * @param cdmId
   * @return
   * @throws CDMException
   */
  public File getAlephFile(final String cdmId) throws CDMException {
    return new File(getCdmDataDir(cdmId), cdmSchema.getAlephFileName(cdmId));
  }

  /**
   * Returns urn Import document for specified CDM
   * 
   * @param cdmId
   * @return
   * @throws CDMException
   */
  public File getUrnXml(final String cdmId) throws CDMException {
    return new File(getUrnDir(cdmId), cdmSchema.getUrnXmlName());
  }

  /**
   * Returns urnnbn response file for specified CDM
   * 
   * @param cdmId
   * @return
   * @throws CDMException
   */
  public File getResolverResponseFile(final String cdmId) throws CDMException {
    return new File(getUrnDir(cdmId), cdmSchema.getResolverResponseFileName());
  }

  /**
   * Returns main METS file for specified CDM
   * 
   * @param cdmId
   * @return
   * @throws CDMException
   */
  public File getMetsFile(final String cdmId) throws CDMException {
    return new File(getCdmDataDir(cdmId), cdmSchema.getMetsFileName(cdmId));
  }

  /**
   * Returns main Valdiation file for specified CDM
   * 
   * @param cdmId
   * @return
   * @throws CDMException
   */
  public File getValidationFile(final String cdmId) throws CDMException {
    return new File(getWorkspaceDir(cdmId), cdmSchema.getValidationFileName());
  }

  /**
   * Returns main LTP_MD file for specified CDM. This file exists only after export from LTP.
   * 
   * @param cdmId
   * @return
   * @throws CDMException
   */
  public File getLtpMdFile(final String cdmId) throws CDMException {
    return new File(getCdmDataDir(cdmId), cdmSchema.getLtpMdFileName());
  }

  /**
   * Returns main MD5 file for specified CDM
   * 
   * @param cdmId
   * @return
   * @throws CDMException
   */
  public File getMD5File(final String cdmId) throws CDMException {
    return new File(getCdmDataDir(cdmId), cdmSchema.getMD5FileName(cdmId));
  }

  /**
   * Returns main MD5 file for specified CDM
   * 
   * @param cdmId
   * @return
   * @throws CDMException
   */
  public File getScansCsvFile(final String cdmId) throws CDMException {
    return new File(getScansDir(cdmId), cdmSchema.getScansCsvFileName(cdmId));
  }

  /**
   * Returns EM config file for specified CDM.
   * 
   * @param cdmId
   * @return EM config
   * @throws CDMException
   */
  public File getEmConfigFile(final String cdmId) throws CDMException {
    checkNotNull(cdmId, "cdmId must not be null");
    return new File(getCdmDataDir(cdmId), cdmSchema.getEmConfigFileName(cdmId));
  }

  public File getBackUpDir(final String cdmId) throws CDMException {
    return new File(getWorkspaceDir(cdmId), cdmSchema.getBackupDirName());
  }

  public File getFlatToPPMappingFile(final String cdmId) throws CDMException {
    checkNotNull(cdmId, "cdmId must not be null");
    return new File(getWorkspaceDir(cdmId), cdmSchema.getFlatToPPMappingFileName());
  }

  public File getOcrFilesListFile(final String cdmId) {
    checkNotNull(cdmId, "cdmId must not be null");
    return new File(getOcrDir(cdmId), cdmSchema.getOcrFilesListName(cdmId));
  }

  public File getTiffinfoFile(final String cdmId, final File imageFile) {
    checkNotNull(cdmId, "cdmId must not be null");
    checkNotNull(imageFile, "imageName must not be null");
    if (imageFile.getParentFile().getParentFile().getName().equals(cdmSchema.getOriginalDataDirName())) { //originalData images are stored in barcode folder
      return new File(new File(getMixDir(cdmId), imageFile.getParentFile().getParentFile().getName()), cdmSchema.getTiffinfoFileName(imageFile.getName()));
    }
    else {
      return new File(new File(getMixDir(cdmId), imageFile.getParentFile().getName()), cdmSchema.getTiffinfoFileName(imageFile.getName()));
    }
  }

  public File getValidationVersionFile(final String cdmId) {
    checkNotNull(cdmId, "cdmId must not be null");
    return new File(getWorkspaceDir(cdmId), cdmSchema.getValidationVersionFileName());
  }

  public File getOrderLogFile(final String cdmId) {
    checkNotNull(cdmId, "cdmId must not be null");
    return new File(getLogsDir(cdmId), cdmSchema.getOrderLogFileName());
  }

  public File getJpgTiffLocationFile(final String cdmId) {
    checkNotNull(cdmId, "cdmId must not be null");
    return new File(getScantailorConfigsDir(cdmId), cdmSchema.getJpegTiffLocationFileName());
  }

  public File getJpgTiffImagePath(String cdmId) {
    File jpgTiffLocFile = getJpgTiffLocationFile(cdmId);
    if (!jpgTiffLocFile.isFile()) {
      LOG.warn("Jpg-tiff location file doesn't exist: {}.", jpgTiffLocFile);
      return null;
    }
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(getJpgTiffLocationFile(cdmId)));
      return new File(reader.readLine());
    }
    catch (Exception e) {
      throw new SystemException("Error while reading jpgTiffLocation. ", ErrorCodes.ERROR_WHILE_READING_FILE);
    }
    finally {
      //Close the BufferedReader
      try {
        if (reader != null) {
          reader.close();
        }
      }
      catch (IOException ex) {
        throw new SystemException("Error while closing jpgTiffLocation file. ", ErrorCodes.ERROR_WHILE_READING_FILE);
      }
    }
  }

  /**
   * Returns list of (all) mandatory directories
   * 
   * @param cdmId
   * @return
   * @throws CDMException
   */
  public List<File> getMandatoryDirs(final String cdmId) throws CDMException {
    final List<File> dirs = new ArrayList<File>();
    dirs.add(getRawDataDir(cdmId));
    dirs.add(getPostprocessingDataDir(cdmId));
    dirs.add(getMasterCopyDir(cdmId));
    dirs.add(getUserCopyDir(cdmId));
    dirs.add(getAmdDir(cdmId));
    dirs.add(getAltoDir(cdmId));
    dirs.add(getTxtDir(cdmId));
    dirs.add(getFlatDataDir(cdmId));
    dirs.add(getPreviewDir(cdmId));
    return dirs;
  }

  /** Loads CDM properties. */
  @RetryOnFailure(attempts = 2)
  public Properties loadCdmProperties(final File cdmDir) throws CDMException {
    final Properties p = new Properties();
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(new File(cdmDir, CDM_PROPERTIES_NAME));
      p.loadFromXML(fis);
    }
    catch (final Exception ex) {
      throw new CDMException("Can't read cdm properties", ex);
    }
    finally {
      IOUtils.closeQuietly(fis);
    }
    return p;
  }

  /** Saves CDM properties. */
  @RetryOnFailure(attempts = 2)
  public void saveCdmProperties(final File cdmDir, final Properties props)
      throws CDMException {
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(new File(cdmDir, CDM_PROPERTIES_NAME));
      props.storeToXML(fos, null);
    }
    catch (final Exception ex) {
      throw new CDMException("Can't write cdm properties", ex);
    }
    finally {
      IOUtils.closeQuietly(fos);
    }
  }

  /** Initializes CDM properties. */
  private void createInitialProperties(final File cdmDir) throws CDMException {
    final Properties p = new Properties();
    p.setProperty("cdmVersion", CDM_VERSION);
    p.setProperty("cdmCreated", String.valueOf(new Date()));
    saveCdmProperties(cdmDir, p);
  }

  public Document getMods(final String cdmId, final String transformationXslName, final String genreType, final String idType) throws CDMException {
    final File alephFile = getAlephFile(cdmId);
    if (alephFile == null || !alephFile.exists()) {
      return null;
    }
    try {
      final Document modsDoc = CDMMarc2Mods.transformAlephMarcToMods(alephFile, transformationXslName, cdmId, genreType, idType);
      return modsDoc;
    }
    catch (final Exception ex) {
      throw new CDMException("Can't transform aleph metadata to MODS: " + ex, ex);
    }
  }

  /**
   * Returns dir for specified CDM if this dir type exists in CDMSchema.
   * 
   * @param cdmId
   * @return
   * @throws CDMException
   */
  public File getDir(final String cdmId, final String dirLabel) throws CDMException {
    return new File(getCdmDataDir(cdmId), cdmSchema.getDirName(dirLabel));
  }

  /**
   * Vracia shard code pre dane cdmId - posledny znak MD5_hex(cdmId).
   * 
   * @param cdmId
   * @return
   */
  private String getShardCode(String cdmId) {
    if (cdmId == null) {
      return null;
    }
    String md5Hex = DigestUtils.md5Hex(cdmId);
    return md5Hex.substring(md5Hex.length() - 1);
  }

  public CDMSchema getCdmSchema() {
    return cdmSchema;
  }

/*	return storage with biggest free space in same priority section, if free space is higher 
 * than threshold */
//	private File getBaseDirForLink() {
//		for (int i = 0; i < priorityCount; i++) {
//			File[] storagesInSamePriority = storages.get(i).toArray(new File[storages.get(i).size()]);		                    
//			Arrays.sort(storagesInSamePriority, new Comparator<File>() {
//				public int compare(File f1, File f2) {
//					return Long.valueOf(f1.getUsableSpace()).compareTo(
//							f2.getUsableSpace());
//				}
//			});
//			File maxFreeStorage = storagesInSamePriority[storagesInSamePriority.length-1]; 
//			LOG.debug("disk free space: {}: {}", maxFreeStorage, maxFreeStorage.getUsableSpace());
//			if (maxFreeStorage.getUsableSpace() > diskSpaceTreshold) {
//				return maxFreeStorage;
//			}
//		}
//		LOG.error("There is insufficient free disk space in all storages");
//		throw new SystemException("There is insufficient free disk space in all storages");
//	}

  /**
   * Returns random storage from set of suitable storages in same priority. Suitable storages
   * in same priority are storages with free space under threshold.
   * 
   * @param cdmId
   * @return random suitable storage
   */
  private File getBaseDirForLink(String cdmId) {
    for (int i = 0; i < priorityCount; i++) {
      File[] storagesInSamePriority = storages.get(i).toArray(new File[storages.get(i).size()]);
      ArrayList<File> suitableStorages = new ArrayList<File>();
      for (File storage : storagesInSamePriority) {
        if (storage.getUsableSpace() > diskSpaceTreshold) {
          suitableStorages.add(storage);
        }
      }
      if (!suitableStorages.isEmpty()) {
        int randomStorageIdx = dice.nextInt(suitableStorages.size());
        File suitableStorage = suitableStorages.get(randomStorageIdx);
        LOG.debug("disk free space: {}: {}", suitableStorage, suitableStorage.getUsableSpace());
        return suitableStorage;
      }
    }
    LOG.error("There is insufficient free disk space in all storages");
    throw new SystemException("There is insufficient free disk space in all storages");
  }

  private void createLink(File baseDir, File linkDir, boolean overwrite) {
    File targetDir = new File(new File(baseDir, linkedCdmDirName), linkDir.getName());
    LOG.debug("targetDir for link: " + targetDir.getAbsolutePath());
    if (overwrite) {
      FileUtils.deleteQuietly(targetDir);
    }
    if (!targetDir.mkdirs()) {
      throw new CDMException("Can't create CDM directory: " + targetDir);
    }
    LOG.info("{} started.", symlinkDesc);
    callScript(symlinkCmd, linkDir.getAbsolutePath(), targetDir.getAbsolutePath(), symlinkDesc);
    if (!linkDir.exists())
      throw new SystemException("CDM link directory was not created.");
  }

  private String getRealPath(String cdmDirPath) {
    cdmDirPath = cdmDirPath.replace("\\", "/");
    String preparedCmd = CYGWIN_BIN + "realpath.exe " + cdmDirPath;
    LOG.debug("command: " + preparedCmd);
    int exitStatus;
    SysCommandExecutor cmdExecutor = new SysCommandExecutor();
    try {
      exitStatus = cmdExecutor.runCommand(preparedCmd);
    }
    catch (Exception e) {
      LOG.error("Error at calling {} command! {}", preparedCmd, e.getCause());
      throw new SystemException("Error at calling " + preparedCmd + " command!", ErrorCodes.EXTERNAL_CMD_ERROR);
    }
    // Ignore cmdError, check exitStatus only. cmdError is filled also by warnings.   
    String cmdError = cmdExecutor.getCommandError();
    if (cmdError != null && cmdError.length() > 0) {
      LOG.error("Warning at calling cmd: " + preparedCmd + " cmdError: " + cmdError);
    }
    if (exitStatus != 0) {
      LOG.error("Error at calling cmd: " + preparedCmd + " exitStatus: " + exitStatus);
      throw new SystemException("Error at calling cmd: " + preparedCmd + " exitStatus: " + exitStatus, ErrorCodes.EXTERNAL_CMD_ERROR);
    }

    String commandOutput = cmdExecutor.getCommandOutput().trim();
    if (commandOutput != null && !commandOutput.isEmpty()) {
      LOG.info("Output for command: " + preparedCmd + " is: " + commandOutput);
    }
    return commandOutput;
  }

  private void callScript(String cmd, String link, String target, String desc) {
    // quote pathname
    link = "\"" + link + "\"";
    target = "\"" + target + "\"";
    String preparedCmd = cmd.replace("${source}", link).replace("${target}", target);
    LOG.debug("command: " + preparedCmd);
    int exitStatus;
    SysCommandExecutor cmdExecutor = new SysCommandExecutor();
    try {
      exitStatus = cmdExecutor.runCommand(preparedCmd);
    }
    catch (Exception e) {
      LOG.error("Error at calling {} script! {}", desc, e.getCause());
      throw new SystemException("Error at calling " + desc + " script!", ErrorCodes.EXTERNAL_CMD_ERROR);
    }
    // Ignore cmdError, check exitStatus only. cmdError is filled also by warnings.		
    String cmdError = cmdExecutor.getCommandError();
    if (cmdError != null && cmdError.length() > 0) {
      LOG.error("Warning at calling {} script: " + preparedCmd + " cmdError: " + cmdError, desc);
    }
    if (exitStatus != 0) {
      LOG.error("Error at calling {} script: " + preparedCmd + " exitStatus: " + exitStatus, desc);
      throw new SystemException("Error at calling " + desc + " script: " + preparedCmd + " exitStatus: " + exitStatus, ErrorCodes.EXTERNAL_CMD_ERROR);
    }
    String commandOutput = cmdExecutor.getCommandOutput().trim();
    if (commandOutput != null && !commandOutput.isEmpty()) {
      LOG.info("Output for command: " + preparedCmd + " is: " + commandOutput);
    }
  }

  /**
   * Return base dir dependent on cdmId (shard code of cdmId). Base dir sa ziskava pre dane cdmId podla hash tabulky.
   * Klucom je posledny znak MD5_hex(cdmId). Hodnota je lokacia pre dane cdmId.
   * 
   * @param cdmId
   * @return
   */
  public File getBaseDir(String cdmId) {
    if (baseDir != null) {
      return baseDir;
    }
    String shardCode = getShardCode(cdmId);
    String location = (String) BASE_DIR_HASH_TABLE.get(shardCode);
    if (location == null) {
      throw new CDMException("Base dir hash table doesn't contain key for shard code: " + shardCode);
    }
    return new File(location);
  }

  public File getRecycleBinDir(String cdmId) {
    return new File(getBaseDir(cdmId), RECYCLE_SUBDIR);
  }

  /**
   * Return base dir dependent on cdmId (shard code of cdmId). Base dir sa ziskava pre dane cdmId podla hash tabulky.
   * Klucom je posledny znak MD5_hex(cdmId). Hodnota je lokacia pre dane cdmId.
   * 
   * @param cdmId
   * @return
   */
  public Set<File> getAllBaseDirs() {
    final Collection<Object> locations = BASE_DIR_HASH_TABLE.values();
    final Set<File> result = new HashSet<File>();
    for (Object loc : locations) {
      result.add(new File((String) loc));
    }
    return result;
  }

  /**
   * Returns all recycle-bin dirs, based on current configuration (
   * 
   * @return
   */
  public Set<File> getAllRecycleBinDirs() {
    final Set<File> baseDirs = getAllBaseDirs();
    final Set<File> result = new HashSet<File>();
    for (File f : baseDirs) {
      result.add(new File(f, RECYCLE_SUBDIR));
    }
    return result;
  }

  @RetryOnFailure(attempts = 3)
  private void retriedDeleteQuietly(File target) {
    FileUtils.deleteQuietly(target);
  }

  @RetryOnFailure(attempts = 3)
  private void retriedDeleteDirectory(File target) throws IOException {
    FileUtils.deleteDirectory(target);
  }

  @RetryOnFailure(attempts = 3)
  private void retriedMoveDirectory(File srcDir, File destDir) throws IOException {
    FileUtils.moveDirectory(srcDir, destDir);
  }

  private static void deleteEventAndAgent(String cdmId, int deleteAfterEvent) {
    CDM cdm = new CDM();
    File[] files = cdm.getAmdDir(cdmId).listFiles();

    for (int k = 0; k < files.length; k++) {
      File file = files[k];
      try {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        System.out.println(file.getName());

        // optional, but recommended
        // read this -
        // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
        doc.getDocumentElement().normalize();

        Set<Node> setOfNodes = new HashSet<Node>();

        NodeList nodeList = doc.getElementsByTagName("mets:digiprovMD");
        for (int i = 0; i < nodeList.getLength(); i++) {
          Node node = nodeList.item(i);
          Element element = (Element) node;

          String id = element.getAttribute("ID");

          int numberFromId = 0;
          try {
            numberFromId = Integer.parseInt(id.substring(
                id.length() - 3, id.length()));
            // System.out.println(numberFromId);
          }
          catch (NumberFormatException ex) {
            ex.printStackTrace();
          }

          if (numberFromId > deleteAfterEvent) {
            // System.out.println(numberFromId);
            setOfNodes.add(node);
            // node.getParentNode().removeChild(node);
          }
        }

        for (Node node : setOfNodes) {
          node.getParentNode().removeChild(node);
        }

        doc.normalize();
        OutputFormat format = new OutputFormat(doc);
        format.setLineWidth(65);
        format.setIndenting(true);
        format.setIndent(2);
        format.setEncoding("UTF-8");
        Writer outxml = new FileWriterWithEncoding(file, "UTF-8");
        XMLSerializer serializer = new XMLSerializer(outxml, format);
        serializer.serialize(doc);

        /*
         * TransformerFactory transformerFactory =
         * TransformerFactory.newInstance(); Transformer transformer =
         * transformerFactory.newTransformer();
         * 
         * DOMSource source = new DOMSource(doc); StreamResult result =
         * new StreamResult(file);
         * 
         * transformer.transform(source, result);
         */

      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

}
