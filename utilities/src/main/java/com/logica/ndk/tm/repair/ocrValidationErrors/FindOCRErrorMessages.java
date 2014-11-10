package com.logica.ndk.tm.repair.ocrValidationErrors;

import com.logica.ndk.tm.utilities.FileIOUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * User: krchnacekm
 */
public class FindOCRErrorMessages {

  private static final String OCR_EXCEPTION_FOLDER_SUFFIX = "\\data\\.workspace\\ocr\\exception";
  private CDMUtil cdmUtil;
  private Map<String, File> cdms;
  private OCRExceptionParser ocrExceptionParser;

  public FindOCRErrorMessages(final String cdmIdsFilePath) {
    this.cdmUtil = new CDMUtilImpl(cdmIdsFilePath);
    this.cdms = this.cdmUtil.getCDMPaths();
    this.ocrExceptionParser = new OCRExceptionParser();
  }

  public FindOCRErrorMessages(final CDMUtil cdmUtil, final String cdmIdsFilePath) {
    this.cdmUtil = new CDMUtilImpl(cdmIdsFilePath);
    this.cdms = this.cdmUtil.getCDMPaths();
    this.ocrExceptionParser = new OCRExceptionParser();
  }

  public Boolean writeErrorIntoFile() {
      final File result = new File("ocrErrors.csv");

      final List<OCRError> errors = getErrors();
      final StringBuilder text = new StringBuilder();
      for (OCRError error : errors) {
          text.append(String.format("%s;%s\n", error.getCdmId(), error.getError().replace('\n', ' ')));
         // text.append("\n\n---------------------------------------------\n\n");
      }

      FileIOUtils.writeToFile(result, text.toString(), "OCR Exceptions");

      return result.exists();
  }

  public List<OCRError> getErrors() {
    final List<OCRError> result = new ArrayList<OCRError>();

    for (String cdmIdKey : this.cdms.keySet()) {
      final File cdm = this.cdms.get(cdmIdKey);
      final String pathToExceptionsFolder = String.format("%s%s", cdm.getAbsolutePath(), OCR_EXCEPTION_FOLDER_SUFFIX);
      File ocrExceptionsFolder = new File(pathToExceptionsFolder);
      if (ocrExceptionsFolder.exists()) {
        final File newestException = findNewestExceptionFolder(ocrExceptionsFolder);
        if (newestException != null) {
          final List<File> ocrResults = getOcrResults(newestException);
          for (File ocrResultItem : ocrResults) {
            result.add(ocrExceptionParser.parseOCRException(cdmIdKey, ocrResultItem));
          }
        }
      }
    }

    return result;
  }

  private List<File> getOcrResults(final File newestException) {
    final List<File> ocrResults = new ArrayList<File>();
    final File[] ocrResultFiles = newestException.listFiles();
    for (File ocrResultItem : ocrResultFiles) {
      ocrResults.add(ocrResultItem);
    }
    return ocrResults;
  }

  private File findNewestExceptionFolder(File ocrExceptionsFolder) {
    final File[] files = ocrExceptionsFolder.listFiles();
    final List<File> directories = new ArrayList<File>();
    for (File file : files) {
      if (file.isDirectory()) {
        directories.add(file);
      }
    }

    Collections.sort(directories);
    if (!directories.isEmpty()) {
      return directories.get(directories.size() - 1);
    }
    else {
      return null;
    }
  }

}
