/**
 * 
 */
package com.logica.ndk.tm.utilities.ocr;

import static java.lang.String.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.google.common.base.Preconditions;
import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author kovalcikm
 */
public class EvaluateOcrResultImpl extends AbstractUtility {

  public String execute(String cdmId, Integer ocrPagesExeption) {
    log.info("EvaluateOcrResult started. cdmId: " + cdmId);
    Preconditions.checkNotNull(cdmId);
    Preconditions.checkNotNull(ocrPagesExeption);

    log.info("OCR pages exception count: " + ocrPagesExeption);

    if (ocrPagesExeption > 0) {
      throw new BusinessException("OCR generated exception file. OCR result directory: " + cdm.getOcrDir(cdmId), ErrorCodes.OCR_EXCEPTION_FILE);
    }

    int pagesCount = cdm.getMasterCopyDir(cdmId).list().length;
    if (pagesCount == cdm.getAltoDir(cdmId).list().length && pagesCount == cdm.getTxtDir(cdmId).list().length) {
      log.info("OCR successfully processed " + pagesCount + " files.");
    }

    if (pagesCount != cdm.getAltoDir(cdmId).list().length || pagesCount != cdm.getTxtDir(cdmId).list().length) {
      throw new SystemException(format("Pages count is not same. Master copy: %d, alto: %d, txt: %d", pagesCount, cdm.getAltoDir(cdmId).list().length, cdm.getTxtDir(cdmId).list().length), ErrorCodes.WRONG_FILES_COUNT);
    }
    
    //create copy of EM.csv for resolving OCR profiles (in case return to EM and changed)
    File previousEmFile = new File(cdm.getCdmDataDir(cdmId), FilenameUtils.getBaseName(cdm.getEmConfigFile(cdmId).getName()) + "_previous.csv");
    try {
      retriedCopyFile(cdm.getEmConfigFile(cdmId), previousEmFile);
    }
    catch (IOException e) {
      throw new SystemException("Unable to create file:"+previousEmFile.getAbsolutePath(), ErrorCodes.COPY_FILES_FAILED);
    }

    log.info("EvaluateOcrResult finished. cdmId: " + cdmId);
    return ResponseStatus.RESPONSE_OK;
  }
  
  @RetryOnFailure(attempts = 3)
  private void retriedCopyFile(File source, File destination) throws IOException {
      FileUtils.copyFile(source, destination);
  }
}
