/**
 * 
 */
package com.logica.ndk.tm.utilities.premis;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;

import com.csvreader.CsvWriter;
import com.logica.ndk.commons.ocr.OcrProfileHelper;
import com.logica.ndk.commons.utils.DateUtils;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMSchema;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord.Operation;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord.OperationStatus;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvHelper;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord;

/**
 * @author kovalcikm
 */
public class GeneratePremisManualOCRImpl extends AbstractUtility {

  private static final String ALTO_CSV = CDMSchemaDir.ALTO_DIR.getDirName() + ".csv";
  private static final String AGENT_ROLE = "software";
  private static final String FORMAT_DESIGNATION_NAME = "text/xml";
  private static final String FORMAT_REGISTRY_KEY = "fmt/101";
  private static final String PRESERVATION_LEVEL_VALUE = "preservation";
  private static final String UTILITY_NAME = "GeneratePremisManualOCR";
  private static final String MANUAL_OCR = "MANUAL_OCR";
  private static final String XML_EXT = ".xml";

  public Integer execute(String cdmId, String agent) {
    log.info("GeneratePremisManualOCRImpl execute started.");
    checkNotNull(cdmId);
    CDM cdm = new CDM();

    List<EmCsvRecord> emRecords = EmCsvHelper.getRecords(EmCsvHelper.getCsvReader(cdm.getEmConfigFile(cdmId).getAbsolutePath()));

    Integer count = 0;
    final File altoTransformationsFile = new File(cdm.getTransformationsDir(cdmId) + File.separator + ALTO_CSV);
    Map<String, PremisCsvRecord> fileRecordMap = PremisCsvHelper.getFileRecordMap(altoTransformationsFile, cdm, cdmId); //premis csv records in Map<String fileName, PremisCsvRecord record>
    List<PremisCsvRecord> updatedRecords = new ArrayList<PremisCsvRecord>(); //collection of records which were already in premis csv and need to be updated

    for (EmCsvRecord record : emRecords) {
      if (record.getProfilOCR().equals(MANUAL_OCR)) {
        count++;
        record.setOCRResult(String.valueOf(100));
        String altoFileName = record.getPageLabel() + XML_EXT;
        PremisCsvRecord premisRecord = fileRecordMap.get(altoFileName);
        premisRecord.setUtility("GeneratePremisManualOCRImpl");
        fileRecordMap.put(altoFileName, premisRecord);
      }
    }

    //update premis records
    try {
      PremisCsvHelper.updateRecords(cdmId, fileRecordMap, updatedRecords, altoTransformationsFile);
    }
    catch (IOException e1) {
      throw new SystemException("Unable to update records in csv file:" + altoTransformationsFile, e1, ErrorCodes.CSV_WRITING);
    }

    //write records with set ocr results
    try {
      EmCsvHelper.writeCsvFile(emRecords, cdmId, false, false);
    }
    catch (IOException e) {
      throw new SystemException("Unable to rewrite EM.csv for: " + cdmId, e, ErrorCodes.CSV_WRITING);
    }

    log.info("GeneratePremisManualOCRImpl execute finished");
    return count;
  }

}
