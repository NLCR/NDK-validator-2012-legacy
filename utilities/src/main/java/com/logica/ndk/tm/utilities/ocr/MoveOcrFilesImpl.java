package com.logica.ndk.tm.utilities.ocr;

import java.io.File;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.logica.ndk.commons.ocr.OcrProfileHelper;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.io.MoveToImpl;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvHelper;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord;

/**
 * @author kovalcikm
 */
public class MoveOcrFilesImpl extends AbstractUtility {

  public String execute(String cdmId) {
    Preconditions.checkNotNull(cdmId);
    log.info("Utility MoveOcr files started. cdmId:" + cdmId);

    MoveToImpl moveTo = new MoveToImpl();
    OcrProfileHelper ocrProfileHelper = new OcrProfileHelper();

    Multimap<String, EmCsvRecord> recordsGroupedByOcrProfile = EmCsvHelper.getRecordsGroupedByOcrProfile(EmCsvHelper.getCsvReader(cdm.getEmConfigFile(cdmId).getAbsolutePath()));
    for (String key : recordsGroupedByOcrProfile.keySet()) {

      ocrProfileHelper.setOcr(key);

      String ocrEngineTransferDir = ocrProfileHelper.retrieveFromConfig("transferDir");
      String ocrEngineInDir = ocrProfileHelper.retrieveFromConfig("inputDir");

      log.debug("OCR transferDir resolved to: " + ocrEngineTransferDir);
      log.debug("OCR inputDir resolved to: " + ocrEngineInDir);

      String sourceDir = ocrEngineTransferDir + File.separator + cdmId;
      String targetDir = ocrEngineInDir + File.separator + cdmId;

      log.debug("sourceDir resolved to: " + sourceDir);
      log.debug("targetDir resolved to: " + targetDir);

      moveTo.moveDir(sourceDir, targetDir, ".*(?<!.tmp)$");//don't move .tmp files
    }

    log.info("Utility MoveOcr files finished. cdmId:" + cdmId);
    return ResponseStatus.RESPONSE_OK;
  }

  public static void main(String[] args) {
    new MoveOcrFilesImpl().execute("9e978ec0-486b-11e3-9d34-00505682629d");
  }
}
