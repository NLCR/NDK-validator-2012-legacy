package com.logica.ndk.tm.utilities.validation;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.em.ValidationViolation;
import com.logica.ndk.tm.utilities.jhove.JhoveService;
import com.logica.ndk.tm.utilities.transformation.JhoveException;

/**
 * Validate flatData dir with files if all files are valid TIFF files and are not compressed.
 * 
 * @author Rudolf Daco
 */
public class ValidateImagesForPostprocImpl extends AbstractUtility {

  public ValidationViolationsWrapper validate(String cdmId, Boolean throwException) {
    log.info("validate(" + cdmId + ")");
    checkNotNull(cdmId);

    final ValidationViolationsWrapper result = new ValidationViolationsWrapper();
    final CDM cdm = new CDM();
    File flatDataDir = cdm.getFlatDataDir(cdmId);
    IOFileFilter fileFilter = new RegexFileFilter(TmConfig.instance().getString("utility.validateImagesForPostproc.sourceFileFilterRegexp"));
    final Collection<File> listFiles = FileUtils.listFiles(flatDataDir, fileFilter, FileFilterUtils.falseFileFilter());
    try {
      JhoveService jhoveService = new JhoveService();
      for (final File file : listFiles) {
        if (!file.isDirectory()) {

          if (jhoveService.isUncompressedTiff(file) == false) {
            result.add(new ValidationViolation("ValidateImagesForPostproc error", "Not Uncompressed Tiff image: " + file.getAbsolutePath()));
          }
        }
      }
    }
    catch (JhoveException e) {
      throw new ValidationException(e);
    }

    if ((result != null) && (result.getViolationsList().size() > 0)) {
      if (throwException) {
        throw new ValidationException("Validation error(s):\n" + result.printResult(), ErrorCodes.VALIDATE_IMAGES_FOR_POSTPROC);
      }
      else {
        log.info("Validation error(s):\n" + result.printResult());
      }
    }
    else {
      log.info("No validation error(s)");
    }
    return result;
  }
}
