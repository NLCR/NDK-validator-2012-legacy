package com.logica.ndk.tm.utilities.transformation.sip1;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.jaxen.SimpleNamespaceContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.Div;
import au.edu.apsr.mtk.base.FileSec;
import au.edu.apsr.mtk.base.Fptr;
import au.edu.apsr.mtk.base.METS;
import au.edu.apsr.mtk.base.METSException;
import au.edu.apsr.mtk.base.METSWrapper;
import au.edu.apsr.mtk.base.StructMap;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.CDMMetsWAHelper;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.io.CopyToImpl;
import com.logica.ndk.tm.utilities.jhove.FileDateCreatedStrategyFactory;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvHelper;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord;

/**
 * @author Rudolf Daco
 */
public class CreateSIP1FromCDMImpl extends AbstractUtility {

  private static final String ALOWED_POSTFIXES = "*.xml";
  private static final String CREATE_SIP1_MAPPING_NODE = "utility.sip1.mapping";
  CopyToImpl copyUtil;
  Document dom;
  String cdmId = null;
  private String ALOWED_SUFIXES = "*.xml";
  private static final String FILE_NAME_FORMATS_PROP_NAME = "utility.sip1.fileNameFormat";
  private static final Properties FILE_NAME_FORMATS = TmConfig.instance().getProperties(FILE_NAME_FORMATS_PROP_NAME);
  private CDMMetsHelper metsHelper = new CDMMetsHelper();

  public Integer execute(String cdmId) throws IOException, SystemException, CDMException, DocumentException, SAXException, ParserConfigurationException, METSException {
    log.info("Creating SIP1 from CDM " + cdmId);
    // Get source folder
    CDM cdm = new CDM();
    File source = cdm.getCdmDataDir(cdmId);
    this.cdmId = cdmId;
    if (!source.exists())
    {
      log.error("Error CDM not exists - " + cdmId);
      throw new SystemException("Error CDM not exists", ErrorCodes.CDM_NOT_EXIST);
    }
    log.info("CDM dir: " + source.getAbsolutePath());
    String targetDirName = null;
    String documentType = cdm.getCdmProperties(cdmId).getProperty("documentType");
    if (documentType != null && (documentType.equals(CDMMetsWAHelper.DOCUMENT_TYPE_WEB_ARCHIVE) || documentType.equals(CDMMetsWAHelper.DOCUMENT_TYPE_WEB_ARCHIVE_HARVEST))) {

    }
    else {
      // get working dir folder. Remove content of this working folder (maybe there are some previous data)
      targetDirName = cdm.getSIP1Dir(cdmId).getAbsolutePath();
      File targetDir = new File(targetDirName);
      //FileUtils.deleteDirectory(targetDir);
      retriedDeleteDirectory(targetDir);
      targetDir.mkdir();
      log.info("Working dir: " + targetDirName);
      // copy all requested files and dirs to working dir
      copySelectedFolders(targetDirName, source.getAbsolutePath());

      // get list of files to rename
      List<FileToRename> filesToRename = getFilesToRename(targetDirName);
      //update mets before changing name
      upradeMets(cdmId);
      // rename files from list
      renameFiles(filesToRename);

      // consolidate AmdMets (change name of files in this xml)
      consolidateAmdMets(filesToRename);
      // consolidate Mets (change name of files, MD5, filesize)
      consolidateMets(filesToRename, targetDirName);
      // copy to LTP import folder
      /*String pendingTargetName = SIP1ImportConsts.SIP_IMPORT_DIR + SIP1ImportConsts.SIP_STATUS_PENDING + "_" + SIP1ImportConsts.SIP_CDM_PREFIX + cdmId;
      log.info("Copy to LTP import dir: " + pendingTargetName);
      FileUtils.copyDirectory(new File(targetDirName), new File(pendingTargetName));
      // rename folder to complete   
      String completeName = SIP1ImportConsts.SIP_IMPORT_DIR + SIP1ImportConsts.SIP_STATUS_COMPLETE + "_" + SIP1ImportConsts.SIP_CDM_PREFIX + cdmId;
      File pendingDir = new File(pendingTargetName);
      File completeDir = new File(completeName);
      log.info("Rename pending dir to complete.");
      pendingDir.renameTo(completeDir);
      log.info("Folder renamed to " + completeName);
       */
    }
    // count files
    final boolean recursive = TmConfig.instance().getBoolean("utility.fileChar.recursive", false);
    final IOFileFilter wildCardFilter = new WildcardFileFilter(ALOWED_SUFIXES, IOCase.INSENSITIVE);
    final IOFileFilter dirFilter = recursive ? FileFilterUtils.trueFileFilter() : FileFilterUtils.falseFileFilter();
    Collection<File> listFilesAfterProcess = FileUtils.listFiles(cdm.getAmdDir(cdmId), wildCardFilter, dirFilter);
    Integer countOfFilesAfterProcess = listFilesAfterProcess.size();
    log.info("SIP1 created");
    log.debug("Count of processed pages (count of xml files in amdSec directory): " + countOfFilesAfterProcess);
    return countOfFilesAfterProcess;
  }

  /**
   * Copies folders
   * 
   * @param mapOfObjects
   *          map with initialization
   * @param targetPath
   *          path of target folder (SIP1)
   * @param sourcePath
   *          path of source (CDM)
   * @throws SystemException
   */
  private void copySelectedFolders(String targetPath, String sourcePath) throws SystemException
  {
    HashMap<String, String> mapOfFolders = intializeHashMap();
    log.debug("Hash map initialized " + mapOfFolders.toString());
    copyUtil = new CopyToImpl();
    Iterator<Map.Entry<String, String>> iterator = mapOfFolders.entrySet().iterator();

    //get source folder and destination folder
    while (iterator.hasNext()) {
      Map.Entry<String, String> entry = iterator.next();
      String oldFolderName = entry.getKey();
      String newFolderName = entry.getValue();

      log.debug("Got source folder name (CDM): " + oldFolderName + " and target folder (SIP1): " + newFolderName + " from hashMap to be copied eventually renamed.");

      String source = sourcePath + File.separator + oldFolderName;
      log.debug("Going to copy " + source);

      File sourceFold = new File(source);

      if (sourceFold.exists())
      {
        String target = targetPath + File.separator + newFolderName;
        File targetFoldOrFile = new File(target);
        if (!targetFoldOrFile.exists() && targetFoldOrFile.isDirectory())
        {
          log.debug("Target fodler does not exist, creating new: " + target);
          targetFoldOrFile.mkdir();
        }

        copyUtil.copy(source, target, null);
        log.debug("Folder " + source + " was copied to " + target);
      }
      else
      {
        log.debug("Source folder " + source + " does not exist.");
      }
    }
  }

  /**
   * Returns initialization (using for test purposes)
   * 
   * @return
   * @throws SystemException
   */
  public HashMap<String, String> getHashMapInitialization() throws SystemException
  {
    return intializeHashMap();
  }

  /**
   * Initializes hashMap From configuration file
   * 
   * @throws SystemException
   */
  private HashMap<String, String> intializeHashMap() throws SystemException
  {
    HashMap<String, String> mapOfFolders = new HashMap<String, String>();
    List<Object> listOfFolders = TmConfig.instance().getList(CREATE_SIP1_MAPPING_NODE);
    for (int i = 0; i < listOfFolders.size(); i++)
    {

      String pairOfMapping = (String) listOfFolders.get(i);
      String[] pair = pairOfMapping.split("=");
      if (pair.length < 2)
      {
        log.error("Bad configuration in tm-config-defaults.xml file: " + pair);
        throw new SystemException("Bad configuration in tm-config-defaults.xml file: " + pair, ErrorCodes.INCORRECT_CONFIGURATION);
      }
      else
      {
        if (pair[0].contains("${cdmId}") || pair[1].contains("${cdmId}"))
        {
          String tempKey = pair[0];
          String tempValue = pair[1];
          pair[0] = tempKey.replace("${cdmId}", cdmId);
          pair[1] = tempValue.replace("${cdmId}", cdmId);
        }

        mapOfFolders.put(pair[0], pair[1]);
      }
    }
    return mapOfFolders;
  }

  /**
   * Ziska zoznam suborov na premenovanie.
   * 
   * @param targetDirName
   * @return
   * @throws CDMException
   * @throws SAXException
   * @throws IOException
   * @throws ParserConfigurationException
   * @throws METSException
   */
  private List<FileToRename> getFilesToRename(String targetDirName) throws CDMException, SAXException, IOException, ParserConfigurationException, METSException {
    log.info("getFilesToRename start for dir: " + targetDirName);
    List<FileToRename> list = new ArrayList<CreateSIP1FromCDMImpl.FileToRename>();
    // ziskaj subory podla zoznamu v METS
    list.addAll(getFilesToRenameFromMets(targetDirName));
    // ziskaj METS na premenovanie
    list.add(getFileToRenameForMets(targetDirName));
    log.info("getFilesToRename end");
    return list;
  }

  /**
   * Ziskaj zoznam suborov na premenovanie na zaklade METS. Pouzije sa fileSec z METS. Poradie suborov je dane na
   * zaklade order vo physical structMap.
   * 
   * @param targetDirName
   * @return
   * @throws CDMException
   * @throws SAXException
   * @throws IOException
   * @throws ParserConfigurationException
   * @throws METSException
   */
  private List<FileToRename> getFilesToRenameFromMets(String targetDirName) throws CDMException, SAXException, IOException, ParserConfigurationException, METSException {
    List<FileToRename> fileToRenameList = new ArrayList<FileToRename>();
    METS mets = metsHelper.getMetsObject(cdm, cdmId);
    List<StructMap> structMapList = mets.getStructMapByType(CDMMetsHelper.STRUCT_MAP_TYPE_PHYSICAL);
    if ((structMapList == null || structMapList.size() != 1 || structMapList.get(0) == null || structMapList.get(0).getDivs() == null || structMapList.get(0).getDivs().size() != 1)) {
      log.error("Incorrect format of METS file. There should be one structMap with type PHYSICAL. This structMap should contains one main div and several sub divs with fptr.");
      throw new SystemException("Incorrect format of METS file. There should be one structMap with type PHYSICAL. This structMap should contains one main div and several sub divs with fptr.", ErrorCodes.WRONG_METS_FORMAT);
    }
    Div mainDiv = structMapList.get(0).getDivs().get(0);
    String fileID = null;
    for (Div div : mainDiv.getDivs()) {
      String orderId = div.getOrder();
      for (Fptr fptr : div.getFptrs()) {
        fileID = fptr.getFileID();
        au.edu.apsr.mtk.base.File fileSecFile = mets.getFileSec().getFile(fileID);
        if (fileSecFile == null) {
          log.error("Incorrect format of METS file. Missing file in fileSec with id: " + fileID);
          throw new SystemException("Incorrect format of METS file. Missing file in fileSec with id: " + fileID, ErrorCodes.WRONG_METS_FORMAT);
        }
        log.debug("fileID = " + fileID + ", fileSecFile = " + fileSecFile + "fileSecFile.getFLocats().get(0) = " + fileSecFile.getFLocats().get(0));
        String fileHref = fileSecFile.getFLocats().get(0).getHref();
        File file = null;
        if (targetDirName != null) {
          file = new File(targetDirName, fileHref);
        }
        else {
          file = new File(cdm.getCdmDataDir(cdmId), fileHref);
        }
        String fileType = StringUtils.substringBefore(fileSecFile.getID(), "_");
        String fileNameFormat = getFileNameFormat(fileType);
//        String index = formatToIndex(orderId);
        String index = StringUtils.substringAfter(fileSecFile.getID(), "_");
        File newFile = createNewFile(file, fileNameFormat, cdmId, index);
        fileToRenameList.add(new FileToRename(file.getAbsolutePath(), newFile.getAbsolutePath(), fileID, index, fileType));
      }
    }
    return fileToRenameList;
  }

  /**
   * Zoznam suborov METS na premenovanie. Zatial mame iba jeden METS.
   * 
   * @param targetDirName
   * @return
   */
  private FileToRename getFileToRenameForMets(String targetDirName) {
    File metsFileOld = cdm.getMetsFile(cdmId);
    File metsFileCurrent;
    if (targetDirName != null) {
      metsFileCurrent = new File(targetDirName, metsFileOld.getName());
    }
    else {
      metsFileCurrent = metsFileOld;
    }
    File metsFileNew = createNewFile(metsFileCurrent, getFileNameFormat("METS"), cdmId, null);
    return new FileToRename(metsFileCurrent.getAbsolutePath(), metsFileNew.getAbsolutePath(), null, null, "METS");
  }

  /**
   * Premenej subory na zaklade zoznamu na premenovanie.
   * 
   * @param list
   */
  private void renameFiles(List<FileToRename> list) {
    log.info("renameFiles start");
    for (FileToRename fileToRename : list) {
      File oldFile = new File(fileToRename.getPathOld());
      File newFile = new File(fileToRename.getPathNew());
      log.info("Renaming file: " + oldFile.getAbsolutePath() + " to: " + newFile.getAbsolutePath());
      oldFile.renameTo(newFile);
    }
    log.info("renameFiles end");
  }

  /**
   * Na zaklade definovanych formatov v konfiguracii sa vygeneruje spravne nove meno pre subor.
   * 
   * @param file
   * @param pattern
   * @param uuid
   * @param index
   * @return
   */
  private File createNewFile(File file, String pattern, String uuid, String index) {
    String fileName = file.getName();
    String ext = StringUtils.substringAfterLast(fileName, ".");
    String newFileName = pattern;
    newFileName = newFileName.replace("${uuid}", uuid).replace("${ext}", ext);
    if (index != null) {
      newFileName = newFileName.replace("${index}", index);
    }
    return new File(file.getParentFile(), newFileName);
  }

  /**
   * Index ma foramt dddd (4 ciferne cislo).
   * 
   * @param orderId
   * @return
   */
  private String formatToIndex(String orderId) {
    if (orderId == null) {
      return null;
    }
    DecimalFormat f = new DecimalFormat("0000");
    return f.format(Integer.valueOf(orderId));
  }

  /**
   * Ziskaj format nazvu suboru pre dany "typ" zo zoznamu formatov ktory je dany konfiguraciou.
   * 
   * @param fileType
   * @return
   */
  private String getFileNameFormat(String fileType) {
    String fileNameFormat = (String) FILE_NAME_FORMATS.get(fileType);
    if (fileNameFormat == null) {
      log.error("Incorrect configuration of sip1. Missing configuration for type:" + fileType + " in property: " + FILE_NAME_FORMATS_PROP_NAME);
      throw new SystemException("Incorrect configuration of sip1. Missing configuration for type:" + fileType + " in property: " + FILE_NAME_FORMATS_PROP_NAME, ErrorCodes.INCORRECT_CONFIGURATION);
    }
    return fileNameFormat;
  }

  /**
   * Vykonaje zmeny v Amd METS. Uprav mena suborov ktore boli premenovane.
   * 
   * @param list
   * @throws DocumentException
   * @throws IOException
   * @throws SAXException
   * @throws METSException
   * @throws ParserConfigurationException
   */
  private void consolidateAmdMets(List<FileToRename> list) throws DocumentException, IOException, ParserConfigurationException, METSException, SAXException {
    SAXReader saxReader = new SAXReader();
    List<FileToRename> amdFiles = FileToRename.get(list, "AMD");
    for (FileToRename amdFileToRename : amdFiles) {
      File currentFile = new File(amdFileToRename.getPathNew());
      org.dom4j.Document doc = saxReader.read(currentFile);
      FileToRename fileToRename = FileToRename.get(list, "ALTO", amdFileToRename.getIndex());
      String replaceOld = new File(fileToRename.getPathOld()).getName();
      String replaceNew = new File(fileToRename.getPathNew()).getName();
      String xPathString = "//mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/premis:object/premis:originalName[.='" + replaceOld + "']";
      setElementText(doc, xPathString, replaceNew);
      fileToRename = FileToRename.get(list, "MC", amdFileToRename.getIndex());
      replaceOld = new File(fileToRename.getPathOld()).getName();
      replaceNew = new File(fileToRename.getPathNew()).getName();
      xPathString = "//mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/premis:object/premis:originalName[.='" + replaceOld + "']";
      setElementText(doc, xPathString, replaceNew);
      xPathString = "//mets:mets/mets:amdSec/mets:techMD/mets:mdWrap/mets:xmlData/premis:object/premis:originalName[.='" + replaceOld + "']";
      //premenovanie suborov v amdSec
      CDMMetsHelper.writeToFile(doc, currentFile);
      File targetDir = new File(cdm.getSIP1Dir(cdmId).getAbsolutePath());
      Document metsDocument = XMLHelper.parseXML(currentFile);
      METSWrapper mw = new METSWrapper(metsDocument);
      METS mets = mw.getMETSObject();

      fileToRename = FileToRename.get(list, "MC", amdFileToRename.getIndex());
      File newFile = new File(fileToRename.getPathNew());
      String href = targetDir.toURI().relativize(newFile.toURI()).getPath();
      au.edu.apsr.mtk.base.File file = mets.getFileSec().getFile(fileToRename.getMetsFileId());
      file = mets.getFileSec().getFile(fileToRename.getMetsFileId());
      file.getFLocats().get(0).setHref(href);

      fileToRename = FileToRename.get(list, "UC", amdFileToRename.getIndex());
      newFile = new File(fileToRename.getPathNew());
      href = targetDir.toURI().relativize(newFile.toURI()).getPath();
      file = mets.getFileSec().getFile(fileToRename.getMetsFileId());
      file = mets.getFileSec().getFile(fileToRename.getMetsFileId());
      file.getFLocats().get(0).setHref(href);

      fileToRename = FileToRename.get(list, "ALTO", amdFileToRename.getIndex());
      newFile = new File(fileToRename.getPathNew());
      href = targetDir.toURI().relativize(newFile.toURI()).getPath();
      file = mets.getFileSec().getFile(fileToRename.getMetsFileId());
      file = mets.getFileSec().getFile(fileToRename.getMetsFileId());
      file.getFLocats().get(0).setHref(href);

      fileToRename = FileToRename.get(list, "TXT", amdFileToRename.getIndex());
      newFile = new File(fileToRename.getPathNew());
      href = targetDir.toURI().relativize(newFile.toURI()).getPath();
      file = mets.getFileSec().getFile(fileToRename.getMetsFileId());
      file = mets.getFileSec().getFile(fileToRename.getMetsFileId());
      file.getFLocats().get(0).setHref(href);

      metsHelper.writeMetsWrapper(currentFile, mw);

    }
  }

  /**
   * Pomocna metoda pre upravu hodnoty v AMD Mets XML doumente.
   * 
   * @param doc
   * @param xPathString
   * @param text
   */
  private void setElementText(org.dom4j.Document doc, String xPathString, String text) {
    HashMap<String, String> nsMap = new HashMap<String, String>();
    nsMap.put("mets", "http://www.loc.gov/METS/");
    nsMap.put("premis", "info:lc/xmlns/premis-v2");
    nsMap.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    XPath xPath = doc.createXPath(xPathString);
    xPath.setNamespaceContext(new SimpleNamespaceContext(nsMap));
    @SuppressWarnings("rawtypes")
    List nodes = xPath.selectNodes(doc);
    for (Object object : nodes) {
      if (object instanceof Element) {
        Element el = (Element) object;
        el.setText(text);
      }
    }
  }

  /**
   * Uprav METS subor - mena suborov ktore boli premenovane a MD5 a fileSize pre AMD METS subory.
   * 
   * @param list
   * @param targetDirName
   * @throws SAXException
   * @throws IOException
   * @throws ParserConfigurationException
   * @throws METSException
   */
  private void consolidateMets(List<FileToRename> list, String targetDirName) throws SAXException, IOException, ParserConfigurationException, METSException {
    FileToRename metsFileToRename = FileToRename.get(list, "METS").get(0);
    File metsFile = new File(metsFileToRename.getPathNew());
    Document metsDocument = XMLHelper.parseXML(metsFile);
    METSWrapper mw = new METSWrapper(metsDocument);
    METS mets = mw.getMETSObject();

    File targetDir;
    if (targetDirName != null) {
      targetDir = new File(targetDirName);
    }
    else { // for WARCs and HARVEST
      targetDir = cdm.getCdmDataDir(cdmId);
    }
    // uprav odkazy na subory a ak ide o AMD METS aj MD5 a fileSize pretoze sme menili obsah tohto suboru
    for (FileToRename fileToRename : list) {
      if (fileToRename.getMetsFileId() != null) {
        File newFile = new File(fileToRename.getPathNew());
        String href = targetDir.toURI().relativize(newFile.toURI()).getPath();
        au.edu.apsr.mtk.base.File file = mets.getFileSec().getFile(fileToRename.getMetsFileId());
        file.getFLocats().get(0).setHref(href);
        // MD5 and size of file for AMD METS
        if (fileToRename.getType().equals("AMD")) {
          file.setChecksum(new CDMMetsHelper().getMD5Checksum(newFile));
          file.setSize(FileUtils.sizeOf(newFile));
        }
      }
    }
    metsHelper.writeMetsWrapper(metsFile, mw);
  }

  private void upradeMets(String cdmId) {
    log.info("Start updating mets");
    try {
      File emFile = cdm.getEmConfigFile(cdmId);
      if (emFile != null && emFile.exists()) {
        List<EmCsvRecord> emRecords = EmCsvHelper.getRecords(EmCsvHelper.getCsvReader(emFile.getAbsolutePath()));

        final String[] allowedPostfixes = { ALOWED_POSTFIXES };
        final IOFileFilter wildCardFilter = new WildcardFileFilter(allowedPostfixes, IOCase.INSENSITIVE);
        Collection<File> amdSecMets = FileUtils.listFiles(new File(cdm.getSIP1Dir(cdmId).getAbsolutePath() + File.separator + "amdSec"), wildCardFilter, FileFilterUtils.trueFileFilter());
        List<EmCsvRecord> emRecord = new ArrayList<EmCsvRecord>();
        log.info("Updating amdSec files. Files: " + amdSecMets);
        int counter = 1;
        for (File mets : amdSecMets) {
          emRecord.clear();
          for (EmCsvRecord record : emRecords) {
            if ( FilenameUtils.getBaseName(mets.getName()).endsWith(record.getPageId())){       
//            if (mets.getName().contains(record.getPageId())) {
              log.info("EM csv record for amdSec file found. amdSec will be updated: " + mets.getName());
              emRecord.add(record);
              metsHelper.removeFileSec(mets);
              if (metsHelper.addFileGroups(mets, cdm, cdmId, counter, FileDateCreatedStrategyFactory.getDateCreatedStrategy(cdmId))) {
                counter++;
              }
              metsHelper.removeStructs(mets, null);
              metsHelper.addPhysicalStructMap(mets, cdm, cdmId, emRecord, false);
            }
          }
          metsHelper.prettyPrint(mets);
        }
      }
    }
    catch (Exception ex) {
      log.error("Error while updating amd mets file. " + ex.getMessage());
      throw new SystemException("Error while updating amd mets file", ex, ErrorCodes.UPDATE_METS_FAILED);
    }
  }

  /**
   * Reprezentuje jeden subor na premenovanie.
   * 
   * @author Rudolf Daco
   */
  public static class FileToRename {
    /**
     * Absolutna cesta reprezentujuca povodny nazov suboru.
     */
    private String pathOld;
    /**
     * Absolutna cesta reprezentujuca novy nazov suboru.
     */
    private String pathNew;
    /**
     * ID suboru pouzivany v METS ak existuje.
     */
    private String metsFileId;
    /**
     * Index (poradie) suboru ktory sa pouziva v jeho novom nazve.
     */
    private String index;
    /**
     * Typ suboru: MC, UC, AMD, TXT, METS.
     */
    private String type;

    public FileToRename() {

    }

    public FileToRename(String pathOld, String pathNew, String metsFileId, String index, String type) {
      super();
      this.pathOld = pathOld;
      this.pathNew = pathNew;
      this.metsFileId = metsFileId;
      this.index = index;
      this.type = type;
    }

    public static List<FileToRename> get(List<FileToRename> list, String type) {
      List<FileToRename> selected = new ArrayList<FileToRename>();
      for (FileToRename fileToRename : list) {
        if (type.equals(fileToRename.getType())) {
          selected.add(fileToRename);
        }
      }
      return selected;
    }

    public static FileToRename get(List<FileToRename> list, String type, String index) {
      List<FileToRename> selected = new ArrayList<FileToRename>();
      for (FileToRename fileToRename : list) {
        if (type.equals(fileToRename.getType()) && index.equals(fileToRename.getIndex())) {
          selected.add(fileToRename);
        }
      }
      if (selected.size() > 1) {
        throw new SystemException("There is more than 1 fileToRename with the same type and index in list: " + StringUtils.join(list, ","), ErrorCodes.SIP1_RENAMING_FAILED);
      }
      if (selected.size() == 1) {
        return selected.get(0);
      }
      return null;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public String getPathOld() {
      return pathOld;
    }

    public void setPathOld(String pathOld) {
      this.pathOld = pathOld;
    }

    public String getPathNew() {
      return pathNew;
    }

    public void setPathNew(String pathNew) {
      this.pathNew = pathNew;
    }

    public String getMetsFileId() {
      return metsFileId;
    }

    public void setMetsFileId(String metsFileId) {
      this.metsFileId = metsFileId;
    }

    public String getIndex() {
      return index;
    }

    public void setIndex(String index) {
      this.index = index;
    }

  }
  
  @RetryOnFailure(attempts = 3)
  private void retriedDeleteDirectory(File target) throws IOException {
      FileUtils.deleteDirectory(target);
  }
}
