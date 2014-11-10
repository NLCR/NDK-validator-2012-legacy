package com.logica.ndk.tm.utilities.ocr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvWriter;
import com.google.common.base.Preconditions;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.em.EmConstants;

public class OcrForRDCHelper {

  private static final Logger LOG = LoggerFactory.getLogger(OcrForRDCHelper.class);

  public void createFile(String taskId, Map<String, OcrResultInfo> ocrResultMap) {
    LOG.info("Creating ocr result file for RDC. taskId: " + taskId);
    Preconditions.checkNotNull(taskId);
    Preconditions.checkNotNull(ocrResultMap);
    String dirPath = TmConfig.instance().getString("process.ocr.filePathForRDC");
    LOG.debug("Target directory path: " + dirPath);
    SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss", Locale.ENGLISH);
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    String time = df.format(cal.getTime());

    String[] HEADER = { "taskId", "OCR", "Pages count", "Licences count", "Time" };
    File rdcFile = new File(dirPath, taskId + "-" + time + ".csv");
    CsvWriter csvWriter = null;

    try {
      csvWriter = new CsvWriter(new FileWriterWithEncoding(rdcFile, "UTF-8", false), EmConstants.CSV_COLUMN_DELIMITER);
      csvWriter.setTextQualifier(EmConstants.CSV_TEXT_QUALIFIER);
      csvWriter.setForceQualifier(true);
      csvWriter.writeRecord(HEADER);

      for (String key : ocrResultMap.keySet()) {
        String[] record = { taskId, key, String.valueOf(ocrResultMap.get(key).getNumberOfPages()), String.valueOf(ocrResultMap.get(key).getNumberOfLicences()), time };
        LOG.info("Record writing to OCR csv file for RDC:" + record.toString());
        LOG.info("OCR profile: " + key);
        LOG.info("Pages count: " + ocrResultMap.get(key).getNumberOfPages());
        LOG.info("Licences count: " + ocrResultMap.get(key).getNumberOfLicences());
        csvWriter.writeRecord(record);
      }
    }
    catch (IOException e) {
      LOG.error("Error: " , e);
      throw new SystemException("Creating csv file error", e, ErrorCodes.CREATING_FILE_ERROR);
    } finally {
      csvWriter.close();
    }
  }
}
