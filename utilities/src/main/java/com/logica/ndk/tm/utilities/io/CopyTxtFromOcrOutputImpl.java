/**
 * 
 */
package com.logica.ndk.tm.utilities.io;

import java.io.File;
import java.util.Arrays;

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
public class CopyTxtFromOcrOutputImpl extends AbstractUtility {

  private CopyToImpl copyTo;
  private static final String[] OCR_ENGINE_PROFILES = TmConfig.instance().getStringArray("process.ocr.profiles");

  public String execute(String cdmId, String ocr, String ocrFont, String language, String targetPath, String... wildcards) {

    Multimap<String, EmCsvRecord> recordsGroupedByOcrProfile = EmCsvHelper.getRecordsGroupedByOcrProfile(EmCsvHelper.getCsvReader(cdm.getEmConfigFile(cdmId).getAbsolutePath()));
    for (String key : recordsGroupedByOcrProfile.keySet()) {
      if (!Arrays.asList(OCR_ENGINE_PROFILES).contains(key)) {
        continue;
      }
      OcrProfileHelper ocrProfileHelper = new OcrProfileHelper();
      ocrProfileHelper.setOcr(key);

      final String ocrEngineOutputDir = ocrProfileHelper.retrieveFromConfig("outputDir");

      File outputCdm = new File(ocrEngineOutputDir, cdmId);

      if (outputCdm.exists()) {
        log.info("Going to copy ocr outputs for cdmId:" + cdmId);
        copyTo = new CopyToImpl();
        copyTo.copy(outputCdm.getAbsolutePath(), targetPath, wildcards);
      }
    }
    return ResponseStatus.RESPONSE_OK;
  }
}
