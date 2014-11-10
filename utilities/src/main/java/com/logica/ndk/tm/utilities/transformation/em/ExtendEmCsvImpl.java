/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.em;

import static java.lang.String.format;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csvreader.CsvWriter;
import com.google.common.base.Preconditions;
import com.logica.ndk.commons.utils.DateUtils;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvHelper;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord;

/**
 * OCR profile for all pages without already defined profile is set to value from WF.
 * 
 * @author kovalcikm
 */
public class ExtendEmCsvImpl extends AbstractUtility {
  private static final String MANUAL_OCR = "MANUAL_OCR";

  public String execute(String cdmId, String ocr, String taskId) {
    Preconditions.checkNotNull(cdmId);
    Preconditions.checkNotNull(ocr);
    log.info("Utility ExtentEmCsv started. cdmId:" + cdmId);

    //add taskId if doesn't exist
    if (cdm.getCdmProperties(cdmId).getProperty("taskId") == null || cdm.getCdmProperties(cdmId).getProperty("taskId").isEmpty()) {
      cdm.updateProperty(cdmId, "taskId", taskId);
    }

    //extend EM.csv if exists
    if (!cdm.getEmConfigFile(cdmId).exists()) {
      log.warn("No EM.csv file. No extension will be provided.");
      return ResponseStatus.RESPONSE_OK;
    }

    List<EmCsvRecord> emRecords = EmCsvHelper.getRecords(EmCsvHelper.getCsvReader(cdm.getEmConfigFile(cdmId).getAbsolutePath()));
    for (EmCsvRecord record : emRecords) {
      if (record.getProfilOCR().isEmpty()) {
        record.setProfilOCR(ocr);
      }
      if (record.getProfilOCR().equals(MANUAL_OCR)) {
        record.setOCRResult("");
      }
    }
    CsvWriter writer = EmCsvHelper.getCsvWriter(cdm.getEmConfigFile(cdmId).getAbsolutePath());
    writer = EmCsvHelper.getCsvWriter(cdm.getEmConfigFile(cdmId).getAbsolutePath());

    try {
      writer.writeComment(format(" Config file for EM. Created %s", DateUtils.toXmlDateTime(new Date()).toXMLFormat()));
      // write header
      writer.writeRecord(EmCsvRecord.HEADER);
      for (EmCsvRecord record : emRecords) {
        //write comment
        writer.writeRecord(record.asCsvRecord());
      }
    }
    catch (IOException ex) {
      throw new SystemException("Error while writing em.csv.", ex, ErrorCodes.CSV_WRITING);
    }
    finally {
      writer.close();
    }

    return ResponseStatus.RESPONSE_OK;
  }
}
