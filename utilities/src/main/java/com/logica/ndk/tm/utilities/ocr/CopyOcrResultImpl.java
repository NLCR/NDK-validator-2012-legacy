/**
 * 
 */
package com.logica.ndk.tm.utilities.ocr;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.logica.ndk.commons.ocr.OcrProfileHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.io.CopyToImpl;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvHelper;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord;

/**
 * @author kovalcikm
 */
public class CopyOcrResultImpl extends AbstractUtility {

  private final String RESULTS_FILE_MASK = TmConfig.instance().getString("process.ocr.resultFileSuffix");
  private static final String[] OCR_ENGINE_PROFILES = TmConfig.instance().getStringArray("process.ocr.profiles");

  public String execute(String cdmId, String ocr, String ocrFont, String language) {
    log.info("Utility CopuOcrResult started. cdmId: " + cdmId);
    log.info(String.format("Parameters: ocr=%s, ocrFont=%s, language=%s", ocr, ocrFont, language));
    Preconditions.checkNotNull(cdmId);

    //date for folder of exceptions
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    Date date = new Date();
    String dateTime = dateFormat.format(date);

    Multimap<String, EmCsvRecord> recordsGroupedByOcrProfile = EmCsvHelper.getRecordsGroupedByOcrProfile(EmCsvHelper.getCsvReader(cdm.getEmConfigFile(cdmId).getAbsolutePath()));
    for (String key : recordsGroupedByOcrProfile.keySet()) {
      if (!Arrays.asList(OCR_ENGINE_PROFILES).contains(key)) {
        continue;
      }
      OcrProfileHelper ocrProfileHelper = new OcrProfileHelper();
      ocrProfileHelper.setOcr(key);

      final String ocrEngineOutputDir = ocrProfileHelper.retrieveFromConfig("outputDir");
      final String ocrEngineExceptionDir = ocrProfileHelper.retrieveFromConfig("exceptionsDir");

      File outputCdm = new File(ocrEngineOutputDir, cdmId);
      File exceptionsCdm = new File(ocrEngineExceptionDir, cdmId);

      CopyToImpl copyTo = new CopyToImpl();

      if (outputCdm.exists()) {
        log.info("Going to copy ocr output result for cdmId:" + cdmId);
        copyTo.copy(outputCdm.getAbsolutePath(), cdm.getOcrDir(cdmId).getAbsolutePath() + File.separator + "output", RESULTS_FILE_MASK);
      }

      if (exceptionsCdm.exists()) {
        log.info("Going to copy ocr exception result for cdmId:" + cdmId);
        copyTo.copy(exceptionsCdm.getAbsolutePath(), cdm.getOcrDir(cdmId).getAbsolutePath() + File.separator + "exception" + File.separator + dateTime, RESULTS_FILE_MASK);
      }
    }
    return ResponseStatus.RESPONSE_OK;
  }
}
