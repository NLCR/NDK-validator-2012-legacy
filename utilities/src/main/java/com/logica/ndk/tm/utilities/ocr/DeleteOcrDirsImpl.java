/**
 * 
 */
package com.logica.ndk.tm.utilities.ocr;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Multimap;
import com.logica.ndk.commons.ocr.OcrProfileHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvHelper;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord;

/**
 * @author kovalcikm
 */
public class DeleteOcrDirsImpl extends AbstractUtility {

  private static final String[] OCR_ENGINE_PROFILES = TmConfig.instance().getStringArray("process.ocr.profiles");

  public String execute(String cdmId) {
    log.info("DeleteOcrDir started for cdmId: " + cdmId);

    Multimap<String, EmCsvRecord> recordsGroupedByOcrProfile = EmCsvHelper.getRecordsGroupedByOcrProfile(EmCsvHelper.getCsvReader(cdm.getEmConfigFile(cdmId).getAbsolutePath()));
    for (String key : recordsGroupedByOcrProfile.keySet()) {
      if (!Arrays.asList(OCR_ENGINE_PROFILES).contains(key)) {
        continue;
      }
      OcrProfileHelper ocrProfileHelper = new OcrProfileHelper();
      ocrProfileHelper.setOcr(key);
      final String ocrEngineInputDir = ocrProfileHelper.retrieveFromConfig("inputDir");
      final String ocrEngineOutputDir = ocrProfileHelper.retrieveFromConfig("outputDir");
      final String ocrEngineExceptionDir = ocrProfileHelper.retrieveFromConfig("exceptionsDir");

      File inputCdm = new File(ocrEngineInputDir, cdmId);
      File outputCdm = new File(ocrEngineOutputDir, cdmId);
      File exceptionsCdm = new File(ocrEngineExceptionDir, cdmId);

      log.info("Input OCR cdm dir:" + inputCdm);
      if (inputCdm.exists()) {
        log.info("Deleting: " + inputCdm.getAbsolutePath());
        FileUtils.deleteQuietly(inputCdm);
      }
      log.info("Output OCR cdm dir:" + outputCdm);
      if (outputCdm.exists()) {
        log.info("Deleting: " + outputCdm.getAbsolutePath());
        FileUtils.deleteQuietly(outputCdm);
      }

      if (exceptionsCdm.exists()) {
        log.info("Deleting: " + exceptionsCdm.getAbsolutePath());
        FileUtils.deleteQuietly(exceptionsCdm);
      }

    }

    return ResponseStatus.RESPONSE_OK;
  }
}
