/**
 * 
 */
package com.logica.ndk.tm.cdm;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.METSException;

import com.csvreader.CsvReader;
import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.commons.utils.DateUtils;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord;

/**
 * @author kovalcikm
 */
public class FormatMigrationHelper {

  private static final Logger LOG = LoggerFactory.getLogger(CDMMetsHelper.class);

  public static final String SKIP_MIX_UPDATE_FORMAT_MIGRATION_PARAM_NAME = "skipMixUpdate";

  private final static String AMD_SEC_ID_FORMAT = "PAGE_%04d";
  public final static String AMD_METS_FILE_PREFIX = "AMD_METS_";
  public final static String PREMIS_PREFIX = "PREMIS_";
  private static final String VIRTUAL_SCANNER_CODE = TmConfig.instance().getString("format-migration.virtualScannerCode");
  private Map<String, String> fileOriginsMap = null;
  private int originsFileExists = 0;

  public boolean isFormatMigration(String cdmImportType) {
    String[] importTypes = TmConfig.instance().getStringArray("format-migration.types");
    for (String imporType : importTypes) {
      if (imporType.equals(cdmImportType)) {
        return true;
      }
    }
    return false;
  }

  public boolean isFileFormatMigration(String cdmId, String fileName) {
    // lazy check file
    if (originsFileExists == 0) {
      CDM cdm = new CDM();
      File originsFile = new File(cdm.getWorkspaceDir(cdmId) + File.separator + "fileOrigins.csv");
      if (originsFile.exists()) {
        originsFileExists = 2;

        // lazy load map
        if (fileOriginsMap == null) {
          fileOriginsMap = new HashMap<String, String>();
          CsvReader reader = null;
          try {
            reader = new CsvReader(originsFile.getAbsolutePath());
            reader.readHeaders();
            while (reader.readRecord()) {
              fileOriginsMap.put(reader.get("fileName"), reader.get("origin"));
            }
          }
          catch (Exception e) {
            e.printStackTrace();
          }
          finally {
            if (reader != null)
              reader.close();
          }
        }
      }
      else
        originsFileExists = 1;
    }

    // if origins file exists check from amp
    if (originsFileExists == 2) {
      String value = fileOriginsMap.get(fileName);
      if (value != null && value.equals("format-migration"))
        return true;
    }

    // if file does not exist or if it was not found before or it has no format-migration origin
    return false;
  }

  public String getProfile(File target) {
    if (target.getName().equals(CDMSchemaDir.MC_DIR.getDirName())) {
      return getMCProfile();
    }
    else {
      if (target.getName().equals(CDMSchemaDir.UC_DIR.getDirName())) {
        return getUCProfile();
      }
      else {
        return null;
      }
    }
  }

  public String getMCProfile() {
    return TmConfig.instance().getString("format-migration.mc-profile");
  }

  public String getUCProfile() {
    return TmConfig.instance().getString("format-migration.uc-profile");
  }

  public static void main(String[] args) throws Exception {

    ArrayList<File> flatFiles = new ArrayList<File>();
    //new FormatMigrationHelper().createMETSForImagesAfterConvert("7b5f4830-efc3-11e3-a269-00505682629d", flatFiles);
    //new FormatMigrationHelper().createMETSForImagesAfterConvert("26b27690-fc75-11e3-a6d9-00505682629d", flatFiles);
    new FormatMigrationHelper().createMETSForImagesAfterConvert("bdd61540-4248-11e4-8cd0-00505682629d", flatFiles);
  }

  public void createMETSForImagesAfterConvert(String cdmId, Collection<File> flatFiles) throws CDMException, METSException, SAXException, IOException, ParserConfigurationException, DocumentException {
    checkNotNull(cdmId, "cdmId must not be null");

    CDM cdm = new CDM();
    CDMMetsHelper metsHelper = new CDMMetsHelper();
    final File outDir = cdm.getAmdDir(cdmId);

    int pageCounter = 0;
    File emCsv = cdm.getEmConfigFile(cdmId);
    final List<EmCsvRecord> records = metsHelper.getListFromEmCsv(cdmId);
    for (EmCsvRecord record : records) {
      String pageName = record.getPageId();
      LOG.debug("Generating AMD METS for page " + pageName);
      boolean virtualScan = isVirtualScanFile(cdmId, record.getScanId());
      boolean isPackageType = (cdm.getCdmProperties(cdmId).getProperty("importType") != null && cdm.getCdmProperties(cdmId).getProperty("importType").equals("PACKAGE")) ? true : false;
      String oldName = metsHelper.getOldName(pageName, cdmId);
      String oldNameNoPrefix = StringUtils.substringAfter(oldName, "_");
      int index_1L = StringUtils.lastIndexOf(oldName, "_1L");
      int index_2R = StringUtils.lastIndexOf(oldName, "_2R");
      String oldNameNoPP = oldName;
      if (index_1L > 0 || index_2R > 0)
      {
        int index = index_1L >= index_2R ? index_1L : index_2R;
        oldNameNoPP = StringUtils.substring(oldNameNoPP, 0, index);
        oldNameNoPrefix = StringUtils.substringAfter(oldNameNoPP, "_");
      }

      String nameNoPrefix = StringUtils.substringAfter(pageName, "_");
      int index_1Lname = StringUtils.lastIndexOf(pageName, "_1L");
      int index_2Rname = StringUtils.lastIndexOf(pageName, "_2R");
      String nameNoPP = pageName;
      if (index_1Lname > 0 || index_2Rname > 0)
      {
        int index = index_1Lname >= index_2Rname ? index_1Lname : index_2Rname;
        nameNoPP = StringUtils.substring(nameNoPP, 0, index);
        nameNoPrefix = StringUtils.substringAfter(nameNoPP, "_");
      }     
      // For non-migration images create regular AMD METS
      if (!virtualScan) {
        LOG.debug("Not a vritual scan: " + record.getScanId() + ", generating standard AMD METS");
        Element mainMods = (Element) metsHelper.getMainMODS(cdm, cdmId);
        String label = metsHelper.getDocumentLabel(mainMods, cdmId);
        File file = new File(cdm.getPostprocessingDataDir(cdmId) + File.separator, oldName + ".tif");
        pageCounter = metsHelper.createMETSForImage(file, pageCounter, cdmId, label, cdm.getPostprocessingDataDir(cdmId), flatFiles);
        continue;
      }

      // For non-FM files from rescan-addscan from LTP
      if (!isFileFormatMigration(cdmId, oldName) && isPackageType) {
        LOG.debug("Not a vritual scan from FM: " + record.getScanId() + ", generating standard AMD METS");
        Element mainMods = (Element) metsHelper.getMainMODS(cdm, cdmId);
        String label = metsHelper.getDocumentLabel(mainMods, cdmId);
        File file = new File(cdm.getPostprocessingDataDir(cdmId) + File.separator, oldName + ".tif");
        pageCounter = metsHelper.createMETSForImage(file, pageCounter, cdmId, label, cdm.getPostprocessingDataDir(cdmId), flatFiles);
        continue;
      }

      try {
        LOG.debug("Virtual scan or FM from LTP: " + record.getScanId() + ", generating FM AMD METS");
        org.dom4j.Document document = DocumentHelper.createDocument();
        org.dom4j.Element metsElement = document.addElement(new QName("mets", metsHelper.nsMets));
        metsElement.add(metsHelper.nsPremis);
        metsElement.add(metsHelper.nsMix);
        metsElement.add(metsHelper.nsXsi);

        metsElement
            .addAttribute(
                "xsi:schemaLocation",
                "http://www.w3.org/2001/XMLSchema-instance http://www.w3.org/2001/XMLSchema.xsd http://www.loc.gov/METS/ http://www.loc.gov/standards/mets/mets.xsd http://www.loc.gov/mix/v20 http://www.loc.gov/standards/mix/mix20/mix20.xsd info:lc/xmlns/premis-v2 http://www.loc.gov/standards/premis/premis.xsd");

        metsElement.addAttribute("TYPE", metsHelper.getDocumentType(cdmId));

//        metsElement.addAttribute("LABEL", label); TODO

        org.dom4j.Element metsHdrElement = metsElement.addElement(new QName("metsHdr", metsHelper.nsMets));
        XMLGregorianCalendar currentDate = DateUtils.toXmlDateTime(new Date(emCsv.lastModified()));
        metsHdrElement.addAttribute("CREATEDATE", currentDate.toXMLFormat());
        metsHdrElement.addAttribute("LASTMODDATE", currentDate.toXMLFormat());

        org.dom4j.Element agentElement = metsHdrElement.addElement(new QName("agent", metsHelper.nsMets));
        agentElement.addAttribute("ROLE", "CREATOR");
        agentElement.addAttribute("TYPE", "ORGANIZATION");

        agentElement.addElement(new QName("name", metsHelper.nsMets))
            .setText("NDK_TM");

        org.dom4j.Element amdSecElement = metsElement.addElement(new QName("amdSec", metsHelper.nsMets));
        amdSecElement.addAttribute("ID", format(AMD_SEC_ID_FORMAT, pageCounter++));

        int objId = 1;
        //OBJ_001 (originalData) 
        if (isPackageType) {
          metsHelper.addPremisObjSection(cdmId, amdSecElement, pageName, CDMSchemaDir.ORIGINAL_DATA.getDirName(), objId);
        }
        else {
          metsHelper.addPremisObjSection(cdmId, amdSecElement, oldNameNoPrefix, CDMSchemaDir.ORIGINAL_DATA.getDirName(), objId);
        }
        objId++;

        //OBJ_002 (flatData)    
        metsHelper.addPremisObjSection(cdmId, amdSecElement, oldNameNoPP, CDMSchemaDir.FLAT_DATA_DIR.getDirName(), objId);
        objId++;

        //OBJ_003 (PP)        
        metsHelper.addPremisObjSection(cdmId, amdSecElement, metsHelper.getOldName(pageName, cdmId), CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName(), objId);
        objId++;

        //OBJ_004 (masterCopy)
        metsHelper.addPremisObjSection(cdmId, amdSecElement, pageName, CDMSchemaDir.MC_DIR.getDirName(), objId);
        objId++;

        //OBJ_005 (alto)
        metsHelper.addPremisObjSection(cdmId, amdSecElement, pageName, CDMSchemaDir.ALTO_DIR.getDirName(), objId);
        objId++;

        //MIX_001 - originalData
        org.dom4j.Element mixTechMDElement;
        org.dom4j.Element mixMdWrapElement;
        File mixFile;
        org.dom4j.Document mixDocument;
        Namespace mixNamespace;
        org.dom4j.Element mixXmlDataElement;

        int mixId = 1;
        mixTechMDElement = amdSecElement.addElement(new QName("techMD", metsHelper.nsMets));
        mixTechMDElement.addAttribute("ID", format("MIX_%03d", mixId++));

        mixMdWrapElement = mixTechMDElement.addElement(new QName("mdWrap", metsHelper.nsMets));
        mixMdWrapElement.addAttribute("MDTYPE", "NISOIMG");
        mixMdWrapElement.addAttribute("MIMETYPE", "text/xml");

        mixFile = null;
        IOFileFilter filterFileTypes = new WildcardFileFilter(oldNameNoPrefix + ".*.xml.mix");
        List<File> mixFileAsList = (List<File>) FileUtils.listFiles(new File(cdm.getMixDir(cdmId) + File.separator + CDMSchemaDir.ORIGINAL_DATA.getDirName()), filterFileTypes, FileFilterUtils.trueFileFilter());
        if (mixFileAsList.size() != 1) {
          throw new SystemException("Mix file for originalData for page " + pageName + " not found", ErrorCodes.FILE_NOT_FOUND);
        }
        mixFile = mixFileAsList.get(0);

        //org.dom4j.Document mixDocument = DocumentHelper.parseText(FileUtils.readFileToString(mixFile));
        mixDocument = DocumentHelper.parseText(retriedReadFileToString(mixFile));
        XMLHelper.qualify(mixDocument, metsHelper.nsMix);

        mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("");
        mixDocument.getRootElement().remove(mixNamespace);
        mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("mix");
        mixDocument.getRootElement().remove(mixNamespace);

        mixXmlDataElement = mixMdWrapElement.addElement(new QName("xmlData", metsHelper.nsMets));
        mixXmlDataElement.add(mixDocument.getRootElement());

        //MIX_002 - flatData
        mixTechMDElement = amdSecElement.addElement(new QName("techMD", metsHelper.nsMets));
        mixTechMDElement.addAttribute("ID", format("MIX_%03d", mixId++));

        mixMdWrapElement = mixTechMDElement.addElement(new QName("mdWrap", metsHelper.nsMets));
        mixMdWrapElement.addAttribute("MDTYPE", "NISOIMG");
        mixMdWrapElement.addAttribute("MIMETYPE", "text/xml");

        mixFile = new File(cdm.getWorkspaceDir(cdmId), "mix/" + CDMSchemaDir.FLAT_DATA_DIR.getDirName() + "/" + oldNameNoPP + ".tif.xml.mix");

        //mixDocument = DocumentHelper.parseText(FileUtils.readFileToString(mixFile));
        mixDocument = DocumentHelper.parseText(retriedReadFileToString(mixFile));
        XMLHelper.qualify(mixDocument, metsHelper.nsMix);

        mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("");
        mixDocument.getRootElement().remove(mixNamespace);
        mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("mix");
        mixDocument.getRootElement().remove(mixNamespace);

        mixXmlDataElement = mixMdWrapElement.addElement(new QName("xmlData", metsHelper.nsMets));
        mixXmlDataElement.add(mixDocument.getRootElement());

        //MIX_003 - PP
        mixTechMDElement = amdSecElement.addElement(new QName("techMD", metsHelper.nsMets));
        mixTechMDElement.addAttribute("ID", format("MIX_%03d", mixId++));

        mixMdWrapElement = mixTechMDElement.addElement(new QName("mdWrap", metsHelper.nsMets));
        mixMdWrapElement.addAttribute("MDTYPE", "NISOIMG");
        mixMdWrapElement.addAttribute("MIMETYPE", "text/xml");

        mixFile = new File(cdm.getWorkspaceDir(cdmId), "mix/" + CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName() + "/" + metsHelper.getOldName(pageName, cdmId) + ".tif.xml.mix");

        //mixDocument = DocumentHelper.parseText(FileUtils.readFileToString(mixFile));
        mixDocument = DocumentHelper.parseText(retriedReadFileToString(mixFile));
        XMLHelper.qualify(mixDocument, metsHelper.nsMix);

        mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("");
        mixDocument.getRootElement().remove(mixNamespace);
        mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("mix");
        mixDocument.getRootElement().remove(mixNamespace);

        mixXmlDataElement = mixMdWrapElement.addElement(new QName("xmlData", metsHelper.nsMets));
        mixXmlDataElement.add(mixDocument.getRootElement());

        //MIX_004 - masterCopy
        mixTechMDElement = amdSecElement.addElement(new QName("techMD", metsHelper.nsMets));
        mixTechMDElement.addAttribute("ID", format("MIX_%03d", mixId++));

        mixMdWrapElement = mixTechMDElement.addElement(new QName("mdWrap", metsHelper.nsMets));
        mixMdWrapElement.addAttribute("MDTYPE", "NISOIMG");
        mixMdWrapElement.addAttribute("MIMETYPE", "text/xml");

        mixFile = new File(cdm.getWorkspaceDir(cdmId), "mix/" + CDMSchemaDir.MC_DIR.getDirName() + "/" + metsHelper.getOldName(pageName, cdmId) + ".tif.jp2.xml.mix");

        //mixDocument = DocumentHelper.parseText(FileUtils.readFileToString(mixFile));
        mixDocument = DocumentHelper.parseText(retriedReadFileToString(mixFile));
        XMLHelper.qualify(mixDocument, metsHelper.nsMix);

        mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("");
        mixDocument.getRootElement().remove(mixNamespace);
        mixNamespace = mixDocument.getRootElement().getNamespaceForPrefix("mix");
        mixDocument.getRootElement().remove(mixNamespace);

        mixXmlDataElement = mixMdWrapElement.addElement(new QName("xmlData", metsHelper.nsMets));
        mixXmlDataElement.add(mixDocument.getRootElement());

        //*****************Events****************************
        int evtId = 1;
        if (isPackageType) {
          evtId = metsHelper.addPremisEvtSection(cdmId, amdSecElement, pageName, CDMSchemaDir.ORIGINAL_DATA.getDirName(), evtId);
        }
        else {
          evtId = metsHelper.addPremisEvtSection(cdmId, amdSecElement, oldNameNoPrefix, CDMSchemaDir.ORIGINAL_DATA.getDirName(), evtId);
        }
        evtId = metsHelper.addPremisEvtSection(cdmId, amdSecElement, oldNameNoPP, CDMSchemaDir.FLAT_DATA_DIR.getDirName(), evtId);
        evtId = metsHelper.addPremisEvtSection(cdmId, amdSecElement, metsHelper.getOldName(pageName, cdmId), CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName(), evtId);
        evtId = metsHelper.addPremisEvtSection(cdmId, amdSecElement, pageName, CDMSchemaDir.MC_DIR.getDirName(), evtId);
        evtId = metsHelper.addPremisEvtSection(cdmId, amdSecElement, pageName, CDMSchemaDir.ALTO_DIR.getDirName(), evtId);

        //*****************Agents****************************
        int agentId = 1;
        if (isPackageType) {
          agentId = metsHelper.addPremisAgentSection(cdmId, amdSecElement, pageName, CDMSchemaDir.ORIGINAL_DATA.getDirName(), agentId++);
        }
        else {
          agentId = metsHelper.addPremisAgentSection(cdmId, amdSecElement, oldNameNoPrefix, CDMSchemaDir.ORIGINAL_DATA.getDirName(), agentId++);
        }
        agentId = metsHelper.addPremisAgentSection(cdmId, amdSecElement, oldNameNoPP, CDMSchemaDir.FLAT_DATA_DIR.getDirName(), agentId++);
        agentId = metsHelper.addPremisAgentSection(cdmId, amdSecElement, metsHelper.getOldName(pageName, cdmId), CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName(), agentId++);
        agentId = metsHelper.addPremisAgentSection(cdmId, amdSecElement, pageName, CDMSchemaDir.MC_DIR.getDirName(), agentId++);
        agentId = metsHelper.addPremisAgentSection(cdmId, amdSecElement, pageName, CDMSchemaDir.ALTO_DIR.getDirName(), agentId++);

        File outputFile = new File(outDir, AMD_METS_FILE_PREFIX + pageName + ".xml");

        CDMMetsHelper.writeToFile(document, outputFile);
      }
      catch (Exception e) {
        LOG.error(format("Write METS file for %s failed.", pageName) + e, e);
        throw new SystemException(format("Write METS file for %s failed.", pageName), e);
      }
    }
  }

  @RetryOnFailure(attempts = 3)
  private String retriedReadFileToString(File file) throws IOException {
    return FileUtils.readFileToString(file, "UTF-8");
  }

  public boolean isVirtualScanFile(String cdmId, File f) {
    ScansHelper sh = new ScansHelper();
    String scanId = sh.getScanId(f.getAbsolutePath());
    String scannerCode = sh.getScanParameter(cdmId, scanId, ScansHelper.COLUMN_SCANNER_CODE);

    return VIRTUAL_SCANNER_CODE.equals(scannerCode);
  }

  public boolean isVirtualScanFile(String cdmId, String scanId) {
    ScansHelper sh = new ScansHelper();
    String scannerCode = sh.getScanParameter(cdmId, scanId, ScansHelper.COLUMN_SCANNER_CODE);

    return VIRTUAL_SCANNER_CODE.equals(scannerCode);
  }

  public boolean isScanFileFromValidScans(String cdmId, File f) {
    ScansHelper sh = new ScansHelper();
    String scanId = sh.getScanId(f.getAbsolutePath());
    String validity = sh.getScanParameter(cdmId, scanId, ScansHelper.COLUMN_SCAN_VALIDITY);

    return Boolean.parseBoolean(validity);
  }

}
