/**
 * 
 */
package com.logica.ndk.tm.utilities.validation;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.csvreader.CsvReader;
import com.google.common.base.Joiner;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.em.ValidationViolation;
import com.logica.ndk.tm.utilities.jhove.MixHelper;
import com.logica.ndk.tm.utilities.transformation.em.EmConstants;
import com.logica.ndk.tm.utilities.validator.validator.Validator;

/**
 * @author kovalcikm
 */
public class ValidatePPOutputImpl extends AbstractUtility {

  private static final long MAX_SCAN_SIZE = TmConfig.instance().getLong("utility.validateCDM.maxScanSize");
  private static final int A4mm2_SIZE = 62370;
  ValidationViolationsWrapper result;
  private static final String[] MIX_SUFFIX = { "*.mix" };

  public ValidationViolationsWrapper execute(String cdmId, Boolean throwException) {
    log.info("ValidatePPOutput started. Validation of (" + cdmId + ")");
    checkNotNull(cdmId);
    result = new ValidationViolationsWrapper();
    CDM cdm = new CDM();

    log.info(format("Check if every valid record in %s has at least one scan.", cdm.getScansCsvFile(cdmId).getPath()));
    checkScansExist(cdmId);
    log.info(format("Checking scans existence finished."));

    log.info("Check scans size (MB).");
    checkScansMBSize(cdmId);
    log.info("Checking scan size (MB) finished.");

    log.info("Check scans area size.");
    checkScansAreaSize(cdmId);
    log.info("Checking scan area size. finished.");

    if ((result != null) && (result.getViolationsList().size() > 0)) {
      log.info("Validation error(s):\n" + result.printResult());
      Validator.printResutlToFile(cdmId, "Validation error(s): \n " + Joiner.on("\n").join(result.violationsList));
      if (throwException) {
        throw new ValidationException("Validation error(s):\n" + result.printResult(), ErrorCodes.VALIDATE_PPOUTPUT);
      }
    }
    else {
      log.info("No validation error(s)");
    }
    log.info("ValidatePPOutput finished.");
    return result;
  }

  private void checkScansMBSize(String cdmId) {
    IOFileFilter filter = new WildcardFileFilter(TmConfig.instance().getStringArray("utility.fileChar.imgExtensions"), IOCase.INSENSITIVE);
    Collection<File> filesPP = FileUtils.listFiles(cdm.getPostprocessingDataDir(cdmId), filter, FileFilterUtils.falseFileFilter());
    for (File file : filesPP) {
      if ((FileUtils.sizeOf(file) / 1024 / 1024) > MAX_SCAN_SIZE) {
        result.add(new ValidationViolation("File exceeds size limit.", "File: " + file.getAbsolutePath() + " is bigger than " + MAX_SCAN_SIZE + "MB"));
      }
    }
  }

  private void checkScansAreaSize(String cdmId) {
    MixHelper mixHelper;

    File mixDir = new File(cdm.getWorkspaceDir(cdmId).getAbsolutePath() + "/mix/" + CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName());
    Collection<File> mixPPFiles = FileUtils.listFiles(mixDir, MIX_SUFFIX, false);
    for (File file : mixPPFiles) {
      mixHelper = new MixHelper(file.getAbsolutePath());
      double width = (mixHelper.getImageWidth() / mixHelper.getHorizontalDpi()) * 25.4;
      double height = (mixHelper.getImageHeight() / mixHelper.getVerticalDpi()) * 25.4;
      double mm2area = width * height;
      if ((mm2area / A4mm2_SIZE) > 16) {
        result.add(new ValidationViolation("File exceeds area size limit.", "File: " + file.getAbsolutePath() + " is bigger A0"));
      }
    }
  }

  private void checkScansExist(String cdmId) {

    CsvReader csvRecords = null;
    try {
      csvRecords = new CsvReader(cdm.getScansCsvFile(cdmId).getAbsolutePath());
    }
    catch (Exception e) {
      result.add(new ValidationViolation("Error while reading csv", "File: " + cdm.getScansCsvFile(cdmId) + " reading failed."));
      if (csvRecords != null) {
        csvRecords.close();
      }
      return;
    }

    try {
      csvRecords.setDelimiter(EmConstants.CSV_COLUMN_DELIMITER);
      csvRecords.setTrimWhitespace(true);
      csvRecords.setTextQualifier(EmConstants.CSV_TEXT_QUALIFIER);
      csvRecords.readHeaders();

      String scanPrefix;
      while (csvRecords.readRecord()) {
        if (Boolean.parseBoolean(csvRecords.get("validity"))) {
          scanPrefix = csvRecords.get("scanId");
          if (!scanExists(scanPrefix, cdmId)) {
            result.add(new ValidationViolation("Scan does not exist", "Scan with scanId (prefix): " + scanPrefix + " does not exist."));
          }
        }
      }

    }
    catch (IOException e) {
      result.add(new ValidationViolation("Error while reading records in csv", "File: " + cdm.getScansCsvFile(cdmId) + " reading failed."));
    }
  }

  private boolean scanExists(String prefix, String cdmId) {
    CDM cdm = new CDM();
    Collection<File> filesPP = FileUtils.listFiles(cdm.getPostprocessingDataDir(cdmId), new PrefixFileFilter(prefix, IOCase.SENSITIVE), FileFilterUtils.falseFileFilter());
    if (filesPP.isEmpty()) {
      return false;
    }
    else {
      return true;
    }
  }
}
