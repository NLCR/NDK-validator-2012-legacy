package com.logica.ndk.tm.utilities.transformation.em;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.logica.ndk.commons.utils.DateUtils;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.FormatMigrationHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord.EmPageType;

/**
 * @author ondrusekl
 */
public class CreateEmConfigImpl extends AbstractUtility {

  private static final String MAPPING_CONFIG_PATH = "process.ocr.wf-profile-mapping";
  private final CDM cdm = new CDM();
  private static final String RESCAN_TYPE_CODE = "customInclude";
  private static final String FOR_DELETION_TYPE_CODE = "forDeletion";
  private static final String NORMAL_PAGE_CODE = "normalPage";
  private static final String COVER_TYPE_CODE = "cover";
  private static final String FREE_SCAN_TYPE = "FREE";
  private static final String CUSTOM_INCLUDE_TYPES = "utility.emCsvRecord.mapping.scanModeCustomInclude";
  private static final String SPECIAL_SCAN_TYPES = "utility.emCsvRecord.mapping.specialScanTreat.specialScanTypes";
  List<EmCsvRecord> frontRecordsList;
  List<EmCsvRecord> middleRecordsList;
  List<EmCsvRecord> endRecordsList;
  List<EmCsvRecord> csvRecordsList;
  List<EmCsvRecord> specialRecordsList;
  Integer specialScansFrontCount;
  private String ocr = "";

  public Integer create(final String cdmId, final String ocr) {
    checkNotNull(cdmId, "cdmId must not be null");

    log.info("create started");

    if (ocr != null) {
      this.ocr = ocr;
    }

    final File dir ="K4".equals(cdm.getCdmProperties(cdmId).getProperty("importType"))?cdm.getOriginalDataDir(cdmId):cdm.getPostprocessingDataDir(cdmId);

    frontRecordsList = new ArrayList<EmCsvRecord>();
    middleRecordsList = new ArrayList<EmCsvRecord>();
    endRecordsList = new ArrayList<EmCsvRecord>();
    csvRecordsList = new ArrayList<EmCsvRecord>();
    specialRecordsList = new ArrayList<EmCsvRecord>();

    //FIXME - nebude pouze pro utility.fileChar
    final String[] cfgExts = TmConfig.instance().getStringArray("utility.fileChar.imgExtensions");
    final IOFileFilter fileFilter = new WildcardFileFilter(cfgExts, IOCase.INSENSITIVE);
    final IOFileFilter dirFilter = FileFilterUtils.falseFileFilter();
    final List<File> listFiles = new ArrayList<File>(FileUtils.listFiles(dir, fileFilter, dirFilter));
    if(new FormatMigrationHelper().isFormatMigration(cdm.getCdmProperties(cdmId).getProperty("importType"))){
      Collections.sort(listFiles, new Comparator<File>() {
        
        @Override
        public int compare(File arg0, File arg1) {
          String file1Name = arg0.getName().substring(arg0.getName().indexOf("_"));
          String file2Name = arg1.getName().substring(arg1.getName().indexOf("_"));
          
          return file1Name.compareToIgnoreCase(file2Name);
        }
      });
    }
    
    File emConfigFile;
    //if csv exists
    if (cdm.getEmConfigFile(cdmId).exists()) {
      emConfigFile = cdm.getEmConfigFile(cdmId);
      csvRecordsList = getListFromEmCsv(cdmId);
      divideToFrontMiddleEndLists(csvRecordsList, cdmId);
      try {
        log.info("Try to copy emConfigFile to backup directory");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss.sss");
        String emCsvBackUpFileName = FilenameUtils.getBaseName(emConfigFile.getName()) + "_" + dateFormat.format(new Date()) + ".csv";
        File backUpDir = cdm.getBackUpDir(cdmId);
        Files.copy(emConfigFile, new File(backUpDir, emCsvBackUpFileName));
        log.info("Copy of emConfigFile complete");
      }
      catch (IOException ex) {
        log.error("Error at copy emConfigFile to backup: " + ex);
        throw new SystemException("Error at copy emConfigFile to backup", ex, ErrorCodes.BACKUP_METADATA_FAILED);
      }
      log.warn("csv file for scans already exists and will be regenerated.");
      emConfigFile.delete();
    }

    emConfigFile = cdm.getEmConfigFile(cdmId);
    try {
      emConfigFile.createNewFile();
    }
    catch (IOException e) {
      log.error("Error while creating file ", e);
      throw new SystemException("Error while creating file", e, ErrorCodes.CREATING_FILE_ERROR);
    }


    for (File f : listFiles) {
      resolveScan(f, cdmId);
    }
    //final String[] files = dir.list();

    final String outFile = cdm.getEmConfigFile(cdmId).getAbsolutePath();
    CsvWriter writer = null;
    try {
      writer = EmCsvHelper.getCsvWriter(outFile);
      //write comment
      writer.writeComment(format(" Config file for EM. Created %s", DateUtils.toXmlDateTime(new Date()).toXMLFormat()));
      // write header
      writer.writeRecord(EmCsvRecord.HEADER);
      // write body
      Integer countOfProcessedFiles = 0;
      //String dmdId = getDmdId(cdmId);

      countOfProcessedFiles = frontRecordsList.size() + middleRecordsList.size() + endRecordsList.size() + specialRecordsList.size();

      int pageOrderCounter = 1;

      for (EmCsvRecord record : frontRecordsList) {
        record.setPageOrder(pageOrderCounter++);
        writer.writeRecord(record.asCsvRecord());
      }

      for (EmCsvRecord record : specialRecordsList) {
        record.setPageOrder(pageOrderCounter++);
        writer.writeRecord(record.asCsvRecord());
      }

      for (EmCsvRecord record : middleRecordsList) {
        record.setPageOrder(pageOrderCounter++);
        writer.writeRecord(record.asCsvRecord());
      }

      for (EmCsvRecord record : endRecordsList) {
        record.setPageOrder(pageOrderCounter++);
        writer.writeRecord(record.asCsvRecord());
      }

      log.info("create finished");
      return countOfProcessedFiles;
    }
    catch (final IOException e) {
      throw new SystemException(format("Write into CSV file %s failed.", outFile), ErrorCodes.CSV_WRITING);
    }
    finally {
      if (writer != null) {
        writer.close();
      }
    }
  }

  private String getDmdId(String cdmId) {
    // Mods Id from Mets
    File metsFile = cdm.getMetsFile(cdmId);
    SAXReader saxReader = new SAXReader();
    Document metsDoc = null;
    try {
      metsDoc = saxReader.read(metsFile);
    }
    catch (Exception e) {
      throw new SystemException("Exception while reading mets file", ErrorCodes.ERROR_WHILE_READING_FILE);
    }
    XPath xPath = DocumentHelper.createXPath("//mets:dmdSec");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mets", "http://www.loc.gov/METS/"));
    Node node = xPath.selectSingleNode(metsDoc);
    String dmdId = ((Element) node).attributeValue("ID");
    return dmdId;
  }

  private String getAmdSec(String scanFile, String cdmId) {
    String[] extensions = { "xml" };
    List<File> files = (List<File>) FileUtils.listFiles(cdm.getAmdDir(cdmId), extensions, true);
    String name = null;
    scanFile = FilenameUtils.removeExtension(scanFile);
    File metsFile;
    for (File f : files) {
      name = FilenameUtils.removeExtension(f.getName()).split("METS_")[1];
      if (name.equals(scanFile)) {
        return EmCsvHelper.getAmdSecTagValue(f);
      }
    }

    return null;
  }

  //retrieves validity from scans.csv
  private boolean getScanValidity(String cdmId, String file) {

    String scanId = file.split("_")[0]; //scanId is the number before first underscore
    CsvReader csvRecords = null;
    try {
      csvRecords = new CsvReader(cdm.getScansCsvFile(cdmId).getAbsolutePath());
    }
    catch (Exception e) {
      throw new SystemException("Retrieving csv file failed.", ErrorCodes.FILE_NOT_FOUND);
    }

    try {
      csvRecords.setDelimiter(EmConstants.CSV_COLUMN_DELIMITER);
      csvRecords.setTrimWhitespace(true);
      csvRecords.setTextQualifier(EmConstants.CSV_TEXT_QUALIFIER);
      csvRecords.readHeaders();

      while (csvRecords.readRecord()) {
        if (csvRecords.get("scanId").equals(scanId)) {
          if (csvRecords.get("validity").equalsIgnoreCase("true")) {
            return true;
          }
          else {
            return false;
          }
        }
      }

    }
    catch (IOException e) {
      throw new SystemException("Error while reading csv.", ErrorCodes.CSV_READING);
    }
    finally {
      if (csvRecords != null) {
        csvRecords.close();
      }
    }
    return false; //TODO co vratit ak sa zaznam nenajde?
  }

//retrieves scanMode from scans.csv
  private String getScanMode(String cdmId, String file) {

    String scanId = file.split("_")[0]; //scanId is the number before first underscore
    CsvReader csvRecords = null;
    try {
      csvRecords = new CsvReader(cdm.getScansCsvFile(cdmId).getAbsolutePath());
    }
    catch (Exception e) {
      //throw new SystemException("Retrieving csv file failed.", e);
      log.warn("CSV record reading failed. scanMode set as empty.", ErrorCodes.FILE_NOT_FOUND);
      if (csvRecords != null) {
        csvRecords.close();
      }
      return "";
    }

    try {
      csvRecords.setDelimiter(EmConstants.CSV_COLUMN_DELIMITER);
      csvRecords.setTrimWhitespace(true);
      csvRecords.setTextQualifier(EmConstants.CSV_TEXT_QUALIFIER);
      csvRecords.readHeaders();

      while (csvRecords.readRecord()) {
        if (csvRecords.get("scanId").equals(scanId)) {
          return csvRecords.get("scanMode");
        }
      }

    }
    catch (IOException e) {
      //throw new SystemException("Error while reading csv.", e);
      log.info("CSV record reading failed. scanMode set as empty.");
    }
    finally {
      if (csvRecords != null) {
        csvRecords.close();
      }
    }

    return null;
  }

//records already processed in EM keeps their order
  private void divideToFrontMiddleEndLists(List<EmCsvRecord> csvScansList, String cdmId) {

    if ((csvScansList == null) || (csvScansList.size() == 0)) {
      return;
    }

    for (EmCsvRecord record : csvScansList) {
      File imgFile = new File(cdm.getPostprocessingDataDir(cdmId) + File.separator + record.getPageLabel());
      if (imgFile.exists()) {
        middleRecordsList.add(record);

      }

    }
  }

  //method for getting list od records from csv file
  private List<EmCsvRecord> getListFromEmCsv(String cdmId) {
    final List<EmCsvRecord> records = new ArrayList<EmCsvRecord>();
    File csvFile = cdm.getEmConfigFile(cdmId);
    try {
      final CsvReader reader = new CsvReader(new FileInputStream(csvFile), Charset.forName("UTF-8"));
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
      log.debug(String.format("getListFromEmCsv - Exception: %s", e));
      throw new SystemException(format("Reading CSV file %s failed.", csvFile.getAbsolutePath()), ErrorCodes.CSV_READING);
    }
    return records;
  }

  //resolves each scan, set parameters and change order
  private void resolveScan(File file, String cdmId) {
    log.info("Resolving scan " + file.getName() + "started.");
    List<Object> customIncludeTypes = TmConfig.instance().getList(CUSTOM_INCLUDE_TYPES);
    List<Object> specialScanTypes = TmConfig.instance().getList(SPECIAL_SCAN_TYPES);
    String pageId = FilenameUtils.removeExtension(file.getName());
    boolean csvContainsScan = false;
    EmCsvRecord foundRecord = null;

    if ((csvRecordsList != null) && (!csvRecordsList.isEmpty())) {
      for (EmCsvRecord csvRecord : csvRecordsList) { //is scan already in csv?
        if (csvRecord.getPageId().equals(pageId)) {
          csvContainsScan = true;
          foundRecord = csvRecord;
          break;
        }
      }
    }

    if (csvContainsScan) { //if scan is already in csv then check validity
      log.info(file.getName() + " is in csv. Validity check.");
      if (getScanValidity(cdmId, foundRecord.getPageId())) {
        log.info(foundRecord.getPageId() + " is already in csv and is valid.");
      }
      else { //if scan is not valid changed type to forDeletion and will be put to the end of the list
        csvRecordsList.remove(foundRecord);
        middleRecordsList.remove(foundRecord);
        specialRecordsList.remove(foundRecord);
        if (!foundRecord.getPageType().equals(EmPageType.forDeletion)) {
          foundRecord.setPageType(EmPageType.forDeletion);
          endRecordsList.add(foundRecord);
        }
      }
    }
    else { //scan is not in CSV yet
      log.info(file.getName() + "is not yet in csv.");
      String scanId = EmCsvHelper.getScanId(file.getName());
      if (customIncludeTypes.contains(getScanMode(cdmId, file.getName()))) { // Rescan/Doscan                     
        final EmCsvRecord newRecord = new EmCsvRecord(
            pageId,
            file.getName(),
            EmPageType.customInclude,
            1,
            "",
            "",
            pageId.split("_")[0],//z flat data je to prefix filu
            EmCsvHelper.getScanType(cdmId, scanId),
            EmCsvHelper.getScanNote(cdmId, scanId),
            getAmdSec(file.getName(), cdmId),
            getScanMode(cdmId, file.getName()),
            ocr,
            "");
        frontRecordsList.add(newRecord);
      }
      else { //free scan
        if (FREE_SCAN_TYPE.equals(EmCsvHelper.getScanType(cdmId, scanId))) {
          log.info(file.getName() + " is free type of scan ");

          final EmCsvRecord newRecord = new EmCsvRecord(
              pageId,
              file.getName(),
              EmPageType.normalPage,
              1,
              "",
              "",
              pageId.split("_")[0],//z flat data je to prefix filu
              EmCsvHelper.getScanType(cdmId, scanId),
              EmCsvHelper.getScanNote(cdmId, scanId),
              getAmdSec(file.getName(), cdmId),
              getScanMode(cdmId, file.getName()),
              ocr,
              "");
          middleRecordsList.add(newRecord);

        }
        else { //special scan type
          log.info(file.getName() + " is special scan type.");
          final EmCsvRecord newRecord = new EmCsvRecord(
              pageId,
              file.getName(),
              EmPageType.cover,
              1,
              "",
              "",
              pageId.split("_")[0],//z flat data je to prefix filu
              EmCsvHelper.getScanType(cdmId, scanId),
              EmCsvHelper.getScanNote(cdmId, scanId),
              getAmdSec(file.getName(), cdmId),
              getScanMode(cdmId, file.getName()),
              ocr,
              "");
          specialRecordsList.add(newRecord);
        }
      }
    }

  }
  
  public static void main(String[] args){
    new CreateEmConfigImpl().create("d226c740-0e65-11e4-ba84-00505682629d", null);
  }
  
}
