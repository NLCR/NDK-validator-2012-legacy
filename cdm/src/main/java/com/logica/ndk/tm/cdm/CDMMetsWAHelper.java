/**
 * 
 */
package com.logica.ndk.tm.cdm;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.activation.MimetypesFileTypeMap;
import javax.xml.XMLConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.Agent;
import au.edu.apsr.mtk.base.AmdSec;
import au.edu.apsr.mtk.base.Div;
import au.edu.apsr.mtk.base.DmdSec;
import au.edu.apsr.mtk.base.FLocat;
import au.edu.apsr.mtk.base.FileGrp;
import au.edu.apsr.mtk.base.FileSec;
import au.edu.apsr.mtk.base.METS;
import au.edu.apsr.mtk.base.METSException;
import au.edu.apsr.mtk.base.METSWrapper;
import au.edu.apsr.mtk.base.MdWrap;
import au.edu.apsr.mtk.base.MetsHdr;
import au.edu.apsr.mtk.base.StructMap;
import au.edu.apsr.mtk.base.TechMD;

import com.csvreader.CsvReader;
import com.logica.ndk.commons.utils.DateUtils;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Enumerator;
import com.logica.ndk.tm.utilities.transformation.em.EmConstants;

/**
 * @author kovalcikm
 */
public class CDMMetsWAHelper {

  private static final Logger LOG = LoggerFactory.getLogger(CDMMetsHelper.class);
  public final static String WA_DMDSEC_ID_MODS_FORMAT = "MODSMD_DOC_%04d";
  public final static String WA_FILE_ID_FORMAT = "TXT_%04d";
  public final static String WA_STRUCT_MAP_DIV_ID_FORMAT = "DIV_P_DOCUMENT_%04d";
  public final static String WA_AMDSEC_ID = "AMDWARC0001";
  public final static String PREMIS_PREFIX = "PREMIS_";

  private final static String STRUCT_MAP_PHYSICAL_DIV_ID_FORMAT = "DIV_P_PAGE_%04d";
  private final static String STRUCT_MAP_LOGICAL_DIV_ID_FORMAT = "ART_0001_%04d";

  public final static String AMD_METS_FILE_PREFIX = "AMD_METS_";

  private static final String FILE_EXT_MODS = TmConfig.instance().getString("utility.warcDump.waModsExtension");
  private static final String TIMESTAMP14ISO8601Z = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  private final MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();

  private final static String OBJ_ID_FORMAT = "OBJ_%03d";
  private final static String EVT_ID_FORMAT = "EVT_%03d";
  private final static String AMD_SEC_ID_FORMAT = "PAGE_%04d";
  private final static String AGENT_ID_FORMAT = "AGENT_%03d";

  public final static String FILE_GRP_ID_LOGS = "LOGSGRP";
  public final static String FILE_GRP_ID_WARC = "WARCGRP";
  public final static String FILE_GRP_ID_ARC = "ARCGRP";
  public final static String FILE_GRP_ID_AMD = "TECHMDGRP";
  public final static String FILE_GRP_ID_TXT = "TXTGRP";

  public final static String FILE_ID_PREFIX_AMD = "AMD_";
  public final static String FILE_ID_PREFIX_TXT = "TXT_";
  public final static String FILE_ID_PREFIX_LOG = "LOG_";
  public final static String FILE_ID_PREFIX_ARC = "ARC_";
  public final static String FILE_ID_PREFIX_WARC = "WARC_";

  protected Namespace nsMets = new Namespace("mets", "http://www.loc.gov/METS/");
  protected Namespace nsPremis = new Namespace("premis", "info:lc/xmlns/premis-v2");
  protected Namespace nsXsi = new Namespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
  protected Namespace nsXlink = new Namespace("xlink", "http://www.w3.org/1999/xlink");

  private final static String DEFAULT_CREATOR = TmConfig.instance().getString("cdm.mets.defaultCreator", "");
  private final static String DEFAULT_ARCHIVIST = TmConfig.instance().getString("cdm.mets.defaultArchivist", "");

  public final static String WA_FILE_GRP_WARC_ID = "WARC0001";
  public final static String WA_FILE_GRP_WARC_FILE_ID = "WARC_0001";
  public final static String DOCUMENT_TYPE_WEB_ARCHIVE = "WA";
  public final static String DOCUMENT_TYPE_WEB_ARCHIVE_HARVEST = "HARVEST";
  public final static Enumerator DOCUMENT_TYPE_HARVEST_ENUM = new Enumerator(297374L, DOCUMENT_TYPE_WEB_ARCHIVE_HARVEST);

  public static final String HARVEST_CMD_ID = "harvestCmdId";
  public static final String HARVEST_IE_ID = "harvestIEId";

  private void addDmdSecsWA(METS mets, CDM cdm, String cdmId, Document mods) throws ParserConfigurationException, METSException, SAXException, IOException, CDMException, DocumentException {
    // add mods into dmdSection
    mods.getDocumentElement().setAttribute("ID", "MODS_TITLE_0001");
    DmdSec dmd = mets.newDmdSec();
    dmd.setID("MODSMD_TITLE");
    MdWrap mdw = dmd.newMdWrap();
    mdw.setMDType("MODS");
    mdw.setMIMEType("text/xml");
    mdw.setXmlData(mods.getDocumentElement());
    dmd.setMdWrap(mdw);
    mets.addDmdSec(dmd);
  }

  public void createMetsForWARCs(final String cdmId, final File inDir) {
    checkNotNull(cdmId, "cdmId must not be null");

    CDM cdm = new CDM();
    CDMMetsHelper cdmMetsHelper = new CDMMetsHelper();

    final File outDir = cdm.getAmdDir(cdmId);

    FileFilter filter = new WildcardFileFilter(new String[] { "*.warc.gz" }, IOCase.INSENSITIVE);
    int pageCounter = 0;
    if ((inDir != null) && inDir.exists()) {
      for (File file : inDir.listFiles(filter)) {
        String pageName = file.getName().substring(0, file.getName().indexOf("."));
        try {

          org.dom4j.Document document = DocumentHelper.createDocument();
          org.dom4j.Element metsElement = document.addElement(new QName("mets", nsMets));
          metsElement.add(nsPremis);
          metsElement.add(nsXlink);
          metsElement.add(nsXsi);
          metsElement.addAttribute("TYPE", cdmMetsHelper.getDocumentType(cdmId));
          Element mainMods = (Element) cdmMetsHelper.getMainMODS(cdm, cdmId);
          if (mainMods != null) {
            metsElement.addAttribute("LABEL", cdmMetsHelper.getDocumentLabel(mainMods, cdmId));
          }
          else {
            metsElement.addAttribute("LABEL", cdmId);
          }

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
          
          File[] premisFiles = premisDir.listFiles();
          if(premisFiles.length > 0) {
            for(int i = 0; i < premisFiles.length; i++){
              File premiseFile = premisFiles[i];
              if(premiseFile.getName().contains("WARC")){
                //WARC
                cdmMetsHelper.addPremisObjSection(cdmId, amdSecElement, pageName, "WARC", i + 1);
              } else {
                //ARC
                cdmMetsHelper.addPremisObjSection(cdmId, amdSecElement, pageName, "ARC", i + 1);
              }
            }
            
            /*int objId = 1;
            //ARC
            File premisFile = new File(cdm.getPremisDir(cdmId), PREMIS_PREFIX + "ARC" + "_" + pageName + ".xml");
            if (premisFile.exists()) {
              cdmMetsHelper.addPremisObjSection(cdmId, amdSecElement, pageName, "ARC", objId);
              objId++;
            } else {

              // WARC
              cdmMetsHelper.addPremisObjSection(cdmId, amdSecElement, pageName, "WARC", objId);
            }*/

          }
          else {
            LOG.debug("Empty PREMIS Dir...");
          }

          //*****************Events****************************
          /*int evtId = 1;
          File premisFile = new File(cdm.getPremisDir(cdmId), PREMIS_PREFIX + "ARC" + "_" + pageName + ".xml");
          if (premisFile.exists()) {
            evtId = cdmMetsHelper.addPremisEvtSection(cdmId, amdSecElement, pageName, cdm.getArcDir(cdmId).getName(), evtId);
          } else {
            evtId = cdmMetsHelper.addPremisEvtSection(cdmId, amdSecElement, pageName, inDir.getName().equals("data") ? "WARC" : "ARC", evtId);
          }*/
          
          for(int i = 0; i < premisFiles.length; i++){
              File premiseFile = premisFiles[i];
              if(premiseFile.getName().contains("WARC")){
                //WARC
                cdmMetsHelper.addPremisEvtSection(cdmId, amdSecElement, pageName, inDir.getName().equals("data") ? "WARC" : "ARC", i + 1);
              } else {
                //ARC
                cdmMetsHelper.addPremisEvtSection(cdmId, amdSecElement, pageName, cdm.getArcDir(cdmId).getName(), i + 1);
              }
            }
            
          //*****************Agents****************************
          /*int agentId = 1;
          if (premisFile.exists()) {
            agentId = cdmMetsHelper.addPremisAgentSection(cdmId, amdSecElement, pageName, cdm.getArcDir(cdmId).getName(), agentId++);
          } else {
            agentId = cdmMetsHelper.addPremisAgentSection(cdmId, amdSecElement, pageName, inDir.getName().equals("data") ? "WARC" : "ARC", agentId++);
          }*/
          
          for(int i = 0; i < premisFiles.length; i++){
            File premiseFile = premisFiles[i];
            if(premiseFile.getName().contains("WARC")){
              //WARC
              cdmMetsHelper.addPremisAgentSection(cdmId, amdSecElement, pageName, inDir.getName().equals("data") ? "WARC" : "ARC", i + 1);
            } else {
              //ARC
              cdmMetsHelper.addPremisAgentSection(cdmId, amdSecElement, pageName, cdm.getArcDir(cdmId).getName(), i + 1);
            }
          }

          File outputFile = new File(outDir, AMD_METS_FILE_PREFIX + pageName + ".xml");
          cdmMetsHelper.writeToFile(document, outputFile);

          LOG.info("Write METS file for page {} into file {}", pageName, outputFile);

        }
        catch (Exception e) {
          throw new SystemException(format("Write METS file for %s failed.", e, file), ErrorCodes.WRITE_TO_METS_FAILED);
        }
      }
    }
  }

  private void addAmdSecWA(METS mets, Document premisDoc) throws METSException, SAXException, IOException, ParserConfigurationException {
    AmdSec amdSec = mets.newAmdSec();
    TechMD techMD = amdSec.newTechMD();
    techMD.setID(format(OBJ_ID_FORMAT, 1));
    amdSec.addTechMD(techMD);
    MdWrap mdw = techMD.newMdWrap();
    mdw.setMDType("PREMIS");
    mdw.setMIMEType("text/xml");
    mdw.setXmlData(premisDoc.getDocumentElement().getElementsByTagName("object").item(0));
    Element element = (Element) mdw.getXmlData();
    element.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:premis", "info:lc/xmlns/premis-v21");
    techMD.setMdWrap(mdw);
    mets.addAmdSec(amdSec);
    amdSec.setID(WA_AMDSEC_ID);
  }

  public void createWAMets(File output, CDM cdm, String cdmId, Document mods) throws METSException, CDMException,
      IOException, SAXException, ParserConfigurationException, DocumentException {
    final FileOutputStream fos = new FileOutputStream(output);

    try {
      LOG.debug("Exporting to METS: " + cdmId);

      METSWrapper mw = new METSWrapper();
      METS mets = mw.getMETSObject();
      String documentType = cdm.getCdmProperties(cdmId).getProperty("documentType");
      mets.setType(documentType);
      mets.setLabel(documentType + " " + cdmId);
      MetsHdr mh = mets.newMetsHdr();
      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");//TODO to Zetko ve specifikaci neni
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
      agent2.setName(DEFAULT_ARCHIVIST);
      mh.addAgent(agent2);
      mets.setMetsHdr(mh);

      addDmdSecsWA(mets, cdm, cdmId, mods);

      mw.write(fos);

    }
    finally {
      IOUtils.closeQuietly(fos);
    }
  }

  public FileSec addFileGroupsWA(METS mets, FileSec fileSec, CDM cdm, String cdmId) throws METSException, CDMException, IOException, SAXException, ParserConfigurationException {
    final File rootDir = cdm.getCdmDataDir(cdmId);
    CDMMetsHelper metsHelper = new CDMMetsHelper();
    FileSec fs = null;
    if (fileSec != null) {
      fs = fileSec;
    }
    else {
      fs = mets.newFileSec();
    }

    if (metsHelper.getDocumentType(cdmId).equals(CDMMetsWAHelper.DOCUMENT_TYPE_WEB_ARCHIVE_HARVEST)) {
      metsHelper.addFiles(fs, mets, cdm.getLogsDataDir(cdmId), null, rootDir, FILE_GRP_ID_LOGS, "Logs", FILE_ID_PREFIX_LOG, cdmId);
    }
    else{
      String packageType = cdm.getCdmProperties(cdmId).getProperty("packageType");
      
      if(packageType.equals("ARC")){
        metsHelper.addFiles(fs, mets, cdm.getWarcsDataDir(cdmId), null, rootDir, FILE_GRP_ID_ARC, "ARC", FILE_ID_PREFIX_ARC, cdmId);
        metsHelper.addFiles(fs, mets, cdm.getWarcsDataDir(cdmId), null, rootDir, FILE_GRP_ID_WARC, "WARC", FILE_ID_PREFIX_WARC, cdmId);
      } else{
        metsHelper.addFiles(fs, mets, cdm.getWarcsDataDir(cdmId), null, rootDir, FILE_GRP_ID_WARC, "WARC", FILE_ID_PREFIX_WARC, cdmId);
      }
    }
    metsHelper.addFiles(fs, mets, cdm.getTxtDir(cdmId), null, rootDir, FILE_GRP_ID_TXT, "Text", FILE_ID_PREFIX_TXT, cdmId);
    metsHelper.addFiles(fs, mets, cdm.getAmdDir(cdmId), null, rootDir, FILE_GRP_ID_AMD, "Technical Metadata", FILE_ID_PREFIX_AMD, cdmId);

    mets.setFileSec(fs);
    return fs;
  }

  private List<String> getTxtFilesForWarc(String warcFileName, String cdmId) {
    CDM cdm = new CDM();
    List<String> txtFilesNames = new ArrayList<String>();
    CsvReader csvRecords = null;
    try {
      File mappingFile = new File(cdm.getWorkspaceDir(cdmId) + File.separator + "warc2txtMapping.csv");
      if (!mappingFile.exists())
        return null;
      csvRecords = new CsvReader(cdm.getWorkspaceDir(cdmId) + File.separator + "warc2txtMapping.csv");
      csvRecords.setDelimiter(EmConstants.CSV_COLUMN_DELIMITER);
      csvRecords.setTrimWhitespace(true);
      csvRecords.setTextQualifier(EmConstants.CSV_TEXT_QUALIFIER);
      csvRecords.readHeaders();

      while (csvRecords.readRecord()) {
        if (csvRecords.get("WARC").equals(warcFileName)) {
          txtFilesNames.add(csvRecords.get("TXT"));
        }
      }
    }
    catch (IOException e) {
      throw new SystemException("Error while reading csv file.", ErrorCodes.CSV_READING);
    }
    return txtFilesNames;

  }

  public void addFileGroups(String cdmId) throws SAXException, IOException, ParserConfigurationException, METSException {
    CDM cdm = new CDM();
    Collection<File> amdSecFiles = FileUtils.listFiles(cdm.getAmdDir(cdmId), FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter());
    int counter = 1;
    CDMMetsHelper metsHelper = new CDMMetsHelper();
    for (File f : amdSecFiles) {
      addFileGroups(f, cdm, cdmId, counter);
      metsHelper.prettyPrint(f);
      counter++;
    }
  }

  public void addFileGroups(File metsFile, CDM cdm, String cdmId, int counter) throws SAXException, IOException, ParserConfigurationException, METSException {
    LOG.debug("Adding file groups to METS file " + metsFile.getName());
    Document metsDocument = XMLHelper.parseXML(metsFile);
    METSWrapper mw = new METSWrapper(metsDocument);
    METS mets = mw.getMETSObject();
    CDMMetsHelper metsHelper = new CDMMetsHelper();
    FileSec fileSec = mets.newFileSec();
    String packageType = cdm.getCdmProperties(cdmId).getProperty("packageType");
    
    if(packageType.equals("ARC")){
      addAmdSecFileGroups(fileSec, mets, metsFile, cdmId, cdm.getWarcsDataDir(cdmId), FILE_GRP_ID_ARC, "ARC", FILE_ID_PREFIX_ARC, counter);
      addAmdSecFileGroups(fileSec, mets, metsFile, cdmId, cdm.getWarcsDataDir(cdmId), FILE_GRP_ID_WARC, "WARC", FILE_ID_PREFIX_WARC, counter);
    } else{
      addAmdSecFileGroups(fileSec, mets, metsFile, cdmId, cdm.getWarcsDataDir(cdmId), FILE_GRP_ID_WARC, "WARC", FILE_ID_PREFIX_WARC, counter);
    }
    
    metsHelper.writeMetsWrapper(metsFile, mw);
    LOG.debug("Section added");
  }

  public void addAmdSecFileGroups(FileSec fileSec, METS mets, File metsFile, String cdmId, File dir, String groupId, String groupUse, String idPrefix, int counter) throws METSException, IOException {
    CDM cdm = new CDM();
    CDMMetsHelper metsHelper = new CDMMetsHelper();
    File rootDir = cdm.getCdmDataDir(cdmId);
    FileSec fs = null;
    if (fileSec != null) {
      fs = fileSec;
    }
    else {
      fs = mets.newFileSec();
    }

    LOG.info("Adding files to file group in amdSec" + groupId);
    final FileGrp fg = fs.newFileGrp();
    fg.setUse(groupUse);
    fg.setID(groupId);
    // list files form dir
    //IOFileFilter fileFilter = FileFilterUtils.trueFileFilter();
    IOFileFilter fileFilter = null;
    if(groupUse.equals("ARC")){
      fileFilter = FileFilterUtils.suffixFileFilter(".arc.gz", IOCase.INSENSITIVE);
    } else{
      fileFilter = FileFilterUtils.suffixFileFilter(".warc.gz", IOCase.INSENSITIVE);
    }

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
    File warcFile = null;
    for (File oneFile : files) {
      StringBuilder sb = new StringBuilder(oneFile.getName());

      if (!FilenameUtils.getBaseName(oneFile.getName()).contains(FilenameUtils.getBaseName(oneFile.getName()))) //lepsia kontrola
        continue;

      warcFile = oneFile;
      au.edu.apsr.mtk.base.File f = fg.newFile();
      f.setID(idPrefix + String.format("%04d", counter));
      f.setSize(FileUtils.sizeOf(oneFile));
      f.setMIMEType(mimeTypesMap.getContentType(oneFile));
      f.setChecksumType("MD5");
      f.setChecksum(metsHelper.getMD5Checksum(oneFile));
      f.setSeq(String.valueOf(counter - 1));
      f.setCreated(df.format(new Date(oneFile.lastModified())));
      FLocat fl = f.newFLocat();
      fl.setHref(metsHelper.getRelativePath(oneFile, rootDir));
      fl.setLocType("URL");
      f.addFLocat(fl);
      fg.addFile(f);
    }
    fs.addFileGrp(fg);

    if(groupUse.equals("WARC")){
      //pridanie prislusnych TXT suborov
      final FileGrp fgTxt = fs.newFileGrp();
      fgTxt.setUse(FILE_GRP_ID_TXT);
      fgTxt.setID("Text");
  
      List<String> txtFileNames = getTxtFilesForWarc(warcFile.getName(), cdmId);
  
      for (String txtName : txtFileNames) {
        File txtFile = new File(cdm.getTxtDir(cdmId) + File.separator + txtName);
        if (!txtFile.exists())
          continue; //TODO - moze nastat?
        au.edu.apsr.mtk.base.File f = fgTxt.newFile();
        f.setID("TXT_" + String.format("%04d", counter++));
        f.setSize(FileUtils.sizeOf(txtFile));
        f.setMIMEType(mimeTypesMap.getContentType(txtFile));
        f.setChecksumType("MD5");
        f.setChecksum(metsHelper.getMD5Checksum(txtFile));
        f.setSeq(String.valueOf(counter - 1));
        f.setCreated(df.format(new Date(txtFile.lastModified())));
        FLocat fl = f.newFLocat();
        fl.setHref(metsHelper.getRelativePath(txtFile, rootDir));
        fl.setLocType("URL");
        f.addFLocat(fl);
        fgTxt.addFile(f);
      }
    
      fs.addFileGrp(fgTxt);
    }
    mets.setFileSec(fs);
  }

  public void generateModsForHarvest(String harvestId, String targetDirPath, String cdmId) {
    File targetDir = new File(targetDirPath);
    if (!targetDir.exists())
      targetDir.mkdir();
    File modsFile = new File(targetDirPath, getModsFileName(cdmId));

    CDM cdm = new CDM();
    File logsDir = cdm.getLogsDataDir(cdmId);
    String harvestFilename = logsDir.list(new SuffixFileFilter("harvest.xml"))[0];
    File harvestFile = new File(logsDir, harvestFilename);

    Map<String, List<String>> valueMap;
    String dateString;
    //CDMMetsHelper cdmHelper = new CDMMetsHelper();
    try {
      //valueMap = cdmHelper.getIdentifiersFromDC(harvestFile, "DCMD_CRAWL_0001");
    	ModsForHarvest harvest = new ModsForHarvest();
    	valueMap = harvest.getIdentifierFromHarvest(harvestFile, "DCMD_CRAWL_0001");
    	dateString = harvest.getDateFromHarvest(harvestFile);
    }
    catch (Exception e) {
      throw new SystemException("Error while reading harvest.xml", e, ErrorCodes.ERROR_WHILE_READING_FILE);
    }

    if (valueMap.containsKey("title") && valueMap.get("title").size() > 0) {
      try {
        writeToMods(harvestId, modsFile, valueMap.get("title").get(0), dateString);
      }
      catch (Exception e) {
        throw new SystemException("Generating MODS for harvest type entity failed. cdmId: " + cdmId, e, ErrorCodes.GENERATING_MODS_HARVEST_FAILED);
      }
    }
    else {
      throw new SystemException("Missing title in harvest.xml", ErrorCodes.MISSING_HARVEST_ELEMENT);
    }
  }

  private static void writeToMods(String harvestId, File targetFile, String titleValue, String date) throws TransformerException, ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory dbfac = PerThreadDocBuilderFactory.getDocumentBuilderFactory();
    DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
    Document doc = docBuilder.newDocument();

    Element root = doc.createElement("mods:mods");
    root.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:mods", "http://www.loc.gov/mods/v3");
    doc.appendChild(root);
    Element titleInfo = doc.createElement("mods:titleInfo");
    root.appendChild(titleInfo);
    Element title = doc.createElement("mods:title");
    title.appendChild(doc.createTextNode(titleValue));
    titleInfo.appendChild(title);
    Element originInfo = doc.createElement("mods:originInfo");
    Element dateCaptured = doc.createElement("mods:dateCaptured");
    dateCaptured.setAttribute("encoding", "w3cdtf");
    //SimpleDateFormat format = new SimpleDateFormat(TIMESTAMP14ISO8601Z);
    //dateCaptured.appendChild(doc.createTextNode(format.format(date)));
    dateCaptured.appendChild(doc.createTextNode(date));
    originInfo.appendChild(dateCaptured);
    root.appendChild(originInfo);
    Element recordInfo = doc.createElement("mods:recordInfo");
    Element recordIdentifier = doc.createElement("mods:recordIdentifier");
    recordIdentifier.appendChild(doc.createTextNode(harvestId));
    recordInfo.appendChild(recordIdentifier);
    root.appendChild(recordInfo);
    Element identifier = doc.createElement("mods:identifier");
    identifier.setAttribute("type", "uuid");
    identifier.appendChild(doc.createTextNode(harvestId));
    root.appendChild(identifier);
    XMLHelper.writeXML(doc, targetFile);
  }
  
  private static String getValueFromOrderLog(Element element, String nodeName, String elementName) {
    NodeList list = element.getElementsByTagName(nodeName);
    if (list == null || list.getLength() != 1) {
      throw new SystemException("Element: " + element.getNodeName() + " does not contain just 1 node <" + nodeName + ">");
    }
    for (Node child = list.item(0).getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child.getNodeType() == Node.ELEMENT_NODE &&
          elementName.equals(child.getNodeName())) {
        return child.getTextContent();
      }
    }

    //throw new SystemException("Node: " + nodeName + "does not contain element: " + elementName);
    return null;
  }

  private String getModsFileName(String cdmId) {
    return cdmId + FILE_EXT_MODS;
  }

  public void addDummyStructMaps(File metsFile, CDM cdm, String cdmId) throws SAXException, IOException, ParserConfigurationException, METSException, CDMException, DocumentException {
    LOG.debug("Adding empty physical structure map from cdm " + cdmId + ", METS " + metsFile.getName());
    Document metsDocument = XMLHelper.parseXML(metsFile);
    METSWrapper mw = new METSWrapper(metsDocument);
    METS mets = mw.getMETSObject();
    CDMMetsHelper metsHelper = new CDMMetsHelper();
    // PHYSICAL
    StructMap sm = mets.newStructMap();
    sm.setLabel("Physical_Structure");
    sm.setType(CDMMetsHelper.STRUCT_MAP_TYPE_PHYSICAL);
    mets.addStructMap(sm);
    // 1 parent div
    Div pDiv = sm.newDiv();
    pDiv.setLabel(mets.getLabel());
    pDiv.setType(mets.getType());
    pDiv.setID("DIV_P_0000");
    pDiv.setDmdID("MODSMD_TITLE");
    sm.addDiv(pDiv);
    Div d = pDiv.newDiv();
    d.setID(format(STRUCT_MAP_PHYSICAL_DIV_ID_FORMAT, 0));

    metsHelper.writeMetsWrapper(metsFile, mw);
    LOG.debug("Physical structure added");

    LOG.debug("Adding empty logical structure map from cdm " + cdmId + ", METS " + metsFile.getName());

    // PHYSICAL
    StructMap smLog = mets.newStructMap();
    smLog.setLabel("Logical_Structure");
    smLog.setType(CDMMetsHelper.STRUCT_MAP_TYPE_LOGICAL);
    mets.addStructMap(smLog);
    // 1 parent div
    Div pDivLog = smLog.newDiv();
    pDivLog.setLabel(mets.getLabel());
    pDivLog.setType(mets.getType());
    pDivLog.setID("TITLE_0001");
    pDivLog.setDmdID("MODSMD_TITLE");
    smLog.addDiv(pDivLog);
    Div dLog = pDivLog.newDiv();
    dLog.setID(format(STRUCT_MAP_LOGICAL_DIV_ID_FORMAT, 0));

    metsHelper.writeMetsWrapper(metsFile, mw);
    LOG.debug("Logical structure added");

  }

  public static void main(String[] args) {
    CDM c = new CDM();
    CDMMetsWAHelper h = new CDMMetsWAHelper();
    h.createMetsForWARCs("57d32eb0-c609-11e3-87fe-00505682629d", c.getWarcsDataDir("57d32eb0-c609-11e3-87fe-00505682629d"));
    //h.generateModsForHarvest("WARC_1_SMALL", "D:/test-data", "WARC_1_SMALL");
  }

}
