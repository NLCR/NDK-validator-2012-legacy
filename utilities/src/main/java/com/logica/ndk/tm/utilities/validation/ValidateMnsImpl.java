/**
 * 
 */
package com.logica.ndk.tm.utilities.validation;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.em.ValidationViolation;

/**
 * @author kovalcikm
 */
public class ValidateMnsImpl extends AbstractUtility {

  ValidationViolationsWrapper result;
  CDM cdm;
  private static final String MNS_EX_FOLDER = "EX";
  private static final String FILES_TO_TRANSFORM_SUFFIX = "utility.validateMns.sourceExt";

  public ValidationViolationsWrapper execute(String cdmId, Boolean throwException) {
    log.info("ValidateMnsImpl started. Validation of cdmId: " + cdmId);
    cdm = new CDM();
    result = new ValidationViolationsWrapper();

    File exDir = new File(cdm.getRawDataDir(cdmId) + File.separator + MNS_EX_FOLDER);

    IOFileFilter fileFilter = new WildcardFileFilter(TmConfig.instance().getStringArray(FILES_TO_TRANSFORM_SUFFIX), IOCase.INSENSITIVE);
    final Collection<File> listJpgFiles = FileUtils.listFiles(exDir, fileFilter, FileFilterUtils.falseFileFilter());
    final Collection<File> listAllFiles = FileUtils.listFiles(exDir, FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter());
    
    log.info(exDir.getAbsolutePath()+"contains "+listJpgFiles.size()+" jpg files");
    log.info(exDir.getAbsolutePath()+"contains "+listAllFiles.size()+" files.");
    if (listJpgFiles.size() != listAllFiles.size()) {
      result.add(new ValidationViolation("Content of EX folder is wrong.", exDir.getAbsolutePath()+" folder should contain only JPG files."));
    }

    if ((result != null) && (result.getViolationsList().size() > 0)) {
      if (throwException) {
        throw new ValidationException("Validation error(s):\n" + result.printResult(), ErrorCodes.VALIDATE_MNS_EX_FOLDER);
      }
      else {
        log.info("Validation error(s):\n" + result.printResult());
      }
    }
    log.info("ValidateMnsImpl finished.");
    return result;

  }

}
