package com.logica.ndk.tm.utilities.transformation.em;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.logica.ndk.commons.utils.DateUtils;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.premis.PremisCsvHelper;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord.EmPageType;

/**
 * @author ondrusekl
 */
public class EmCsvHelper {
  private static final Logger log = LoggerFactory.getLogger(EmCsvHelper.class);

  public static Map<String, EmCsvRecord> getIdRecordsMap(final CsvReader reader) {
    checkNotNull(reader, "reader must not be null");
    try {
      final Map<String, EmCsvRecord> recordsByIntEntity = new HashMap<String, EmCsvRecord>();

      reader.readHeaders();
      while (reader.readRecord()) {
        log.debug("pageId: " + reader.get("pageId"));
        log.debug("profilOCR: " + reader.get("profilOCR"));
        log.debug("pageType: " + reader.get("pageType"));
        final EmCsvRecord record = new EmCsvRecord(
            reader.get("pageId"),
            reader.get("pageLabel"),
            EmPageType.valueOf(reader.get("pageType")),
            Integer.valueOf(reader.get("pageOrder")),
            reader.get("pageOrderLabel"),
            reader.get("dmdId"),
            reader.get("scanId"),
            reader.get("scanType"),
            reader.get("scanNote"),
            reader.get("admid"),
            reader.get("scanMode"),
            reader.get("profilOCR"),
            reader.get("OCRResult"));
        recordsByIntEntity.put(record.getPageId(), record);
      }
      return recordsByIntEntity;
    }
    catch (final Exception e) {
      throw new BusinessException("Parse EM config file failed", e, ErrorCodes.ABSTRACT_EM_UTITLITY_PARSE);
    }
    finally {
      reader.close();
    }
  }

  public static Multimap<String, EmCsvRecord> getRecordsGroupedByOcrProfile(final CsvReader reader) {
    checkNotNull(reader, "reader must not be null");

    try {
      final Multimap<String, EmCsvRecord> recordsByIntEntity = ArrayListMultimap.<String, EmCsvRecord> create();

      reader.readHeaders();
      while (reader.readRecord()) {
        log.debug("pageId: " + reader.get("pageId"));
        log.debug("profilOCR: " + reader.get("profilOCR"));
        final EmCsvRecord record = new EmCsvRecord(
            reader.get("pageId"),
            reader.get("pageLabel"),
            EmPageType.valueOf(reader.get("pageType")),
            Integer.valueOf(reader.get("pageOrder")),
            reader.get("pageOrderLabel"),
            reader.get("dmdId"),
            reader.get("scanId"),
            reader.get("scanType"),
            reader.get("scanNote"),
            reader.get("admid"),
            reader.get("scanMode"),
            reader.get("profilOCR"),
            reader.get("OCRResult"));
        recordsByIntEntity.put(record.getProfilOCR(), record);
      }

      // sort internal lists
      for (String key : recordsByIntEntity.keySet()) {
        Collections.sort((List<EmCsvRecord>) recordsByIntEntity.get(key));
      }

      return recordsByIntEntity;
    }
    catch (final Exception e) {
      throw new BusinessException("Parse EM config file failed", e, ErrorCodes.ABSTRACT_EM_UTITLITY_PARSE);
    }
    finally {
      reader.close();
    }
  }

  public static Multimap<String, EmCsvRecord> getRecordsGroupedByDmdId(final CsvReader reader) {
    checkNotNull(reader, "reader must not be null");

    try {
      final Multimap<String, EmCsvRecord> recordsByIntEntity = ArrayListMultimap.<String, EmCsvRecord> create();

      reader.readHeaders();
      while (reader.readRecord()) {
        log.debug("pageId: " + reader.get("pageId"));
        log.debug("dmdId: " + reader.get("dmdId"));
        log.debug("pageType: " + reader.get("pageType"));
        final EmCsvRecord record = new EmCsvRecord(
            reader.get("pageId"),
            reader.get("pageLabel"),
            EmPageType.valueOf(reader.get("pageType")),
            Integer.valueOf(reader.get("pageOrder")),
            reader.get("pageOrderLabel"),
            reader.get("dmdId"),
            reader.get("scanId"),
            reader.get("scanType"),
            reader.get("scanNote"),
            reader.get("admid"),
            reader.get("scanMode"),
            reader.get("profilOCR"),
            reader.get("OCRResult"));
        recordsByIntEntity.put(record.getDmdId(), record);
      }

      // sort internal lists
      for (String key : recordsByIntEntity.keySet()) {
        Collections.sort((List<EmCsvRecord>) recordsByIntEntity.get(key));
      }

      return recordsByIntEntity;
    }
    catch (final Exception e) {
      throw new BusinessException("Parse EM config file failed", e, ErrorCodes.ABSTRACT_EM_UTITLITY_PARSE);
    }
    finally {
      reader.close();
    }
  }

  public static List<EmCsvRecord> getRecords(final CsvReader reader) {
    checkNotNull(reader, "reader must not be null");

    try {
      final List<EmCsvRecord> records = new ArrayList<EmCsvRecord>();

      reader.readHeaders();
      while (reader.readRecord()) {
        String pageType = reader.get("pageType");
        records.add(new EmCsvRecord(
            reader.get("pageId"),
            reader.get("pageLabel"),
            EmPageType.valueOf(reader.get("pageType")),
            Integer.valueOf(reader.get("pageOrder")),
            reader.get("pageOrderLabel"),
            reader.get("dmdId"),
            reader.get("scanId"),
            reader.get("scanType"),
            reader.get("scanNote"),
            reader.get("admid"),
            reader.get("scanMode"),
            reader.get("profilOCR"),
            reader.get("OCRResult")));
      }

      return records;
    }
    catch (final Exception e) {
      throw new BusinessException("Parse EM config file failed", e, ErrorCodes.ABSTRACT_EM_UTITLITY_PARSE);
    }
    finally {
      reader.close();
    }
  }

  public static CsvReader getCsvReader(final String inFile) {
    checkNotNull(inFile, "inFile must not be null");
    checkArgument(!inFile.isEmpty(), "inFile must not be empty");

    CsvReader reader = null;
    try {
      //reader = new CsvReader(inFile);
      reader = new CsvReader(new FileInputStream(inFile), Charset.forName("UTF-8"));
      //reader = new CsvReader(new InputStreamReader(new FileInputStream(inFile)), "UTF-8");
      reader.setDelimiter(EmConstants.CSV_COLUMN_DELIMITER);
      reader.setTextQualifier(EmConstants.CSV_TEXT_QUALIFIER);
      reader.setUseComments(true);

      return reader;
    }
    catch (final FileNotFoundException e) {
      throw new SystemException(format("Read file %s failed", inFile),e, ErrorCodes.CSV_READING);
    }
  }

  public static CsvWriter getCsvWriter(final String outFile) {
    checkNotNull(outFile, "outFile must not be null");
    checkArgument(!outFile.isEmpty(), "outFile must not be empty");
    CsvWriter writer;
    try {
      writer = new CsvWriter(new FileWriterWithEncoding(outFile, "UTF-8", false), EmConstants.CSV_COLUMN_DELIMITER);
    }
    catch (IOException e) {
      throw new SystemException("Error while creating CsvWriter for file: " + outFile, ErrorCodes.CSV_WRITING);
    }
    writer.setTextQualifier(EmConstants.CSV_TEXT_QUALIFIER);
    writer.setForceQualifier(true);

    return writer;
  }

  //TODO consider retrieving scanType and scanNote from scans.csv in one method

  public static String getScanType(String cdmId, String scanId) {
    CsvReader csvRecords = null;
    CDM cdm = new CDM();
    try {
      csvRecords = new CsvReader(cdm.getScansCsvFile(cdmId).getAbsolutePath());
    }
    catch (Exception e) {
      //throw new SystemException("Retrieving csv file failed.", e);
      log.warn("CSV record reading failed. scanType set as empty.");
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
          return csvRecords.get("scanTypeCode");
        }
      }

    }
    catch (IOException e) {
      //throw new SystemException("Error while reading csv.", e);
      log.info(cdm.getScansCsvFile(cdmId) + ":CSV record reading failed. scanType set as empty.");
    }
    finally {
      if (csvRecords != null) {
        csvRecords.close();
      }
    }

    return null;
  }

  //retrieves scanNote from scans.csv
  public static String getScanNote(String cdmId, String scanId) {
    CsvReader csvRecords = null;
    CDM cdm = new CDM();
    try {
      csvRecords = new CsvReader(new InputStreamReader(new FileInputStream(cdm.getScansCsvFile(cdmId)), "UTF-8"));
    }

    catch (Exception e) {
      //throw new SystemException("Retrieving csv file failed.", e);
      log.warn(cdm.getScansCsvFile(cdmId) + ":CSV record reading failed. scanNote set as empty.");
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
          return csvRecords.get("note");
        }
      }
    }
    catch (IOException e) {
      //throw new SystemException("Error while reading csv.", e);
      log.info("CSV record reading failed. scanType set as empty.");
    }
    finally {
      if (csvRecords != null) {
        csvRecords.close();
      }
    }
    return null;
  }

  //scanId is the number before first underscore
  public static String getScanId(String file) {
    if (file == null) {
      return null;
    }
    return file.split("_")[0];
  }

  public static String getAmdSecTagValue(File metsFile) {
    SAXReader saxReader = new SAXReader();
    Document metsDoc = null;
    try {
      metsDoc = saxReader.read(metsFile);
    }
    catch (Exception e) {
      log.info("Reading mets failed. AmdSec set as empty.");
      return "";
    }
    XPath xPath = DocumentHelper.createXPath("//mets:amdSec");
    xPath.setNamespaceURIs(ImmutableMap.<String, String> of("mets", "http://www.loc.gov/METS/"));
    Node node = xPath.selectSingleNode(metsDoc);
    String amdSec = ((Element) node).attributeValue("ID");
    return amdSec;
  }

  public static List<EmCsvRecord> updateEmRecordsByMap(List<EmCsvRecord> records, Map<String, EmCsvRecord> idRecordsMap) {
    for (EmCsvRecord record : records) {
//      System.out.println(idRecordsMap.get(record.getPageId()).getOCRResult());
      record.setOCRResult(idRecordsMap.get(record.getPageId()).getOCRResult());
//      System.out.println(record.getOCRResult());
    }
    return records;
  }

  public static void writeCsvFile(List<EmCsvRecord> records, String cdmId, boolean append, boolean defaultSort) throws IOException {
    List<EmCsvRecord> csvRecordsList = null;
    File emConfigFile = null;
    CDM cdm = new CDM();
    if (cdm.getEmConfigFile(cdmId).exists()) {
      emConfigFile = cdm.getEmConfigFile(cdmId);
    }
    // nacitame existujuce zaznamy z existujuceho CSV
    if (append == true && emConfigFile != null) {
      csvRecordsList = getRecords(getCsvReader(emConfigFile.getAbsolutePath()));
    }
    // delete file
    emConfigFile.delete();
    // pridame zaznamy z CSV do nasho zoznamu ak tam este nie je
    if (csvRecordsList != null) {
      for (EmCsvRecord emFromCsv : csvRecordsList) {
        if (!contains(records, emFromCsv)) {
          records.add(emFromCsv);
        }
      }
    }
    // zapiseme CSV subor
    try {
      emConfigFile.createNewFile();
    }
    catch (IOException e) {
      throw new SystemException("Error while creating file", ErrorCodes.CREATING_FILE_ERROR);
    }
    final String outFile = cdm.getEmConfigFile(cdmId).getAbsolutePath();
    CsvWriter writer = null;
    try {
      writer = getCsvWriter(outFile);
      //write comment
      writer.writeComment(format(" Config file for EM. Created %s", DateUtils.toXmlDateTime(new Date()).toXMLFormat()));
      // write header
      writer.writeRecord(EmCsvRecord.HEADER);
      // write body
      if (records != null) {
        // sort first
        if (defaultSort == true) {
          Collections.sort(records);
        }
        for (EmCsvRecord record : records) {
          writer.writeRecord(record.asCsvRecord());
        }
      }
    }
    finally {
      if (writer != null) {
        writer.close();
      }
    }
  }

  /**
   * Obsahuje list uz tento zaznam. Nechcem riesit cez euals v EmCsvRecord, toto je transparentnejsie.
   * 
   * @param records
   * @param em
   * @return
   */
  private static boolean contains(List<EmCsvRecord> records, EmCsvRecord em) {
    for (EmCsvRecord csvRecord : records) {
      if (csvRecord.getPageId().equals(em.getPageId())) {
        return true;
      }
    }
    return false;
  }
  public static void main(String[] args) {
    String cdmId="0484d440-430c-11e4-aded-005056827e51";
    CDM cdm=new CDM();    
    List<PremisCsvRecord> records = PremisCsvHelper.getRecords(cdm.getEmConfigFile(cdmId), cdm, cdmId);
  }
}
