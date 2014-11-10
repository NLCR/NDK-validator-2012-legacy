package com.logica.ndk.tm.utilities.premis;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.logica.ndk.commons.utils.DateUtils;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord.Operation;

public class PremisCsvHelper {

  protected final static Logger LOG = LoggerFactory.getLogger(PremisCsvHelper.class);
  

  public static List<PremisCsvRecord> getRecords(final File file, final CDM cdm, final String cdmId) {
    checkNotNull(file, "file must not be null");
    final List<PremisCsvRecord> records = new ArrayList<PremisCsvRecord>();
    CsvReader reader = null;
    try {
      reader = getCsvReader(file);
      reader.readHeaders();
      while (reader.readRecord()) {
        final PremisCsvRecord record = new PremisCsvRecord(
            DateUtils.toDate(reader.get("dateTime")),
            reader.get("utility"),
            reader.get("utilityVersion"),
            Operation.valueOf(reader.get("operation")),
            reader.get("eventDir"),
            reader.get("agent"),
            reader.get("agentVersion"),
            reader.get("agentNote"),
            reader.get("agentRole"),
            new File(cdm.getCdmDir(cdmId), reader.get("file")),
            PremisCsvRecord.OperationStatus.valueOf(reader.get("status")),
            reader.get("formatDesignationName"),
            reader.get("formatRegistryKey"),
            reader.get("preservationLevelValue"));
        records.add(record);
      }
      return records;
    }
    catch (final Exception e) {
      LOG.error(format("Reading CSV file %s failed.", file) , e);
      throw new SystemException(format("Reading CSV file %s failed.", file), ErrorCodes.CSV_READING);
    }
    finally {
      if (reader != null) {
        reader.close();
      }
    }
  }
  
  public static List<PremisCsvRecord> getWaRecords(final File file, final CDM cdm, final String cdmId) {
    checkNotNull(file, "file must not be null");
    final List<PremisCsvRecord> records = new ArrayList<PremisCsvRecord>();
    CsvReader reader = null;
    try {
      reader = getCsvReader(file);      
      reader.readHeaders();    
      
      while (reader.readRecord()) {
        String fileName = reader.get("file");
        fileName = fileName.substring(fileName.lastIndexOf('\\') + 1);
        if(fileName.contains(".")){ 
          fileName = fileName.substring(0, fileName.indexOf("."));
        }
        
        String waType = reader.get("formatDesignationName").equals("application/warc") ? "WARC" : "ARC";
        
        final PremisCsvRecord record = new PremisCsvRecord(
            DateUtils.toDate(reader.get("dateTime")),
            reader.get("utility"),
            reader.get("utilityVersion"),
            Operation.valueOf(reader.get("operation")),
            waType,
            reader.get("agent"),
            reader.get("agentVersion"),
            reader.get("agentNote"),
            reader.get("agentRole"),
            new File(cdm.getCdmDir(cdmId), reader.get("file")),
            PremisCsvRecord.OperationStatus.valueOf(reader.get("status")),
            reader.get("formatDesignationName"),
            reader.get("formatRegistryKey"),
            reader.get("preservationLevelValue"),
            waType + "_" + fileName);
        System.out.println(record.getFile().getAbsolutePath());
        records.add(record);
      }
      return records;
    }
    catch (final Exception e) {
      LOG.error(format("Reading CSV file %s failed.", file) , e);
      throw new SystemException(format("Reading CSV file %s failed.", file), ErrorCodes.CSV_READING);
    }
    finally {
      if (reader != null) {
        reader.close();
      }
    }
  }

  public static Map<String, PremisCsvRecord> getFileRecordMap(final File file, final CDM cdm, final String cdmId) {
    checkNotNull(file, "file must not be null");
    final Map<String, PremisCsvRecord> records = new HashMap<String, PremisCsvRecord>();
    CsvReader reader = null;
    try {
      reader = getCsvReader(file);
      reader.readHeaders();
      while (reader.readRecord()) {
        final PremisCsvRecord record = new PremisCsvRecord(
            DateUtils.toDate(reader.get("dateTime")),
            reader.get("utility"),
            reader.get("utilityVersion"),
            Operation.valueOf(reader.get("operation")),
            reader.get("eventDir"),
            reader.get("agent"),
            reader.get("agentVersion"),
            reader.get("agentNote"),
            reader.get("agentRole"),
            new File(cdm.getCdmDir(cdmId), reader.get("file")),
            PremisCsvRecord.OperationStatus.valueOf(reader.get("status")),
            reader.get("formatDesignationName"),
            reader.get("formatRegistryKey"),
            reader.get("preservationLevelValue"));
        records.put(new File(reader.get("file")).getName(), record);
      }
      return records;
    }
    catch (final Exception e) {
      LOG.error(format("Reading CSV file %s failed.", file) , e);
      throw new SystemException(format("Reading CSV file %s failed.", file), ErrorCodes.CSV_READING);
    }
    finally {
      if (reader != null) {
        reader.close();
      }
    }
  }

  public static void updateRecords(String cdmId, Map<String, PremisCsvRecord> recordsMap, List<PremisCsvRecord> updatedRecords, File premisCsvFile) throws IOException {
    for (PremisCsvRecord record : updatedRecords) {
      recordsMap.put(record.getFile().getName(), record);
    }
      writeCsvFile(new ArrayList(recordsMap.values()), premisCsvFile, new CDM(), cdmId);

  }

  public static CsvReader getCsvReader(final File file) {
    checkNotNull(file, "file must not be null");
    try {
      final CsvReader reader = new CsvReader(new FileReader(file));
      reader.setDelimiter(PremisConstants.CSV_COLUMN_DELIMITER);
      reader.setTrimWhitespace(true);
      reader.setTextQualifier(PremisConstants.CSV_TEXT_QUALIFIER);
      reader.setUseComments(true);
      return reader;
    }
    catch (final FileNotFoundException e) {
      throw new SystemException(format("Reading CSV file %s failed", file.getAbsolutePath()), ErrorCodes.CSV_READING);
    }
  }

  public static void writeCsvFile(List<PremisCsvRecord> records, File premisCsvFile, CDM cdm, String cdmId) throws IOException {
    checkNotNull(records, "records must not be null");
    checkNotNull(premisCsvFile, "remisFile must not be null");
    // delete file
    if (premisCsvFile.exists()) {
      premisCsvFile.delete();
    }
    // zapiseme CSV subor
    try {
      premisCsvFile.createNewFile();
    }
    catch (IOException e) {
      throw new SystemException("Error while creating file", ErrorCodes.CREATING_FILE_ERROR);
    }
    CsvWriter writer = null;
    try {
      writer = getCsvWriter(premisCsvFile);
      //write comment
      writer.writeComment(format(" Premis events log file. Created %s", DateUtils.toXmlDateTime(new Date()).toXMLFormat()));
      // write header
      writer.writeRecord(PremisCsvRecord.HEADER);
      // write body
      if (records != null) {
        for (PremisCsvRecord record : records) {
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

  public static CsvWriter getCsvWriter(final File file) {
    checkNotNull(file, "file must not be null");
    CsvWriter csvWriter;
    try {
      csvWriter = new CsvWriter(new FileWriter(file, true), PremisConstants.CSV_COLUMN_DELIMITER);
    }
    catch (IOException e) {
      throw new SystemException("Error while creating CsvWriter", ErrorCodes.CSV_WRITING);
    }
    csvWriter.setTextQualifier(PremisConstants.CSV_TEXT_QUALIFIER);
    csvWriter.setForceQualifier(true);
    return csvWriter;
  }
}
