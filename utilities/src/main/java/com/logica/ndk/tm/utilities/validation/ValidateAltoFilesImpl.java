package com.logica.ndk.tm.utilities.validation;

import java.io.File;

import com.google.common.base.Joiner;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.em.ValidationViolation;
import com.logica.ndk.tm.utilities.validator.validator.Validator;

/**
 * @author brizat
 */
public class ValidateAltoFilesImpl extends AbstractUtility {

  public static final String ALTO_XSD = "xsd/alto-v2.0.xsd";

  public ValidationViolationsWrapper validate(String cdmId, Boolean throwException) throws SystemException {
    log.info("Utility validateAltoFiles started");
    ValidationViolationsWrapper result = new ValidationViolationsWrapper();

    CDM cdm = new CDM();
    File altoDir = cdm.getAltoDir(cdmId);

    if (!altoDir.exists()) {
      throw new SystemException("Alto dir does not exist", ErrorCodes.VALIDATE_ALTO_FILES_DIR_NOT_EXIST);
    }

    File[] altoFiles = altoDir.listFiles();
    for (File altoFile : altoFiles) {
      log.debug("Validating file: " + altoFile.getName());
      try {
        XMLHelper.validateXML(altoFile, ALTO_XSD);
      }
      catch (Exception ex) {
        log.error("Error while validating file: " + altoFile.getName() + ", excetion: " + ex);
        result.add(new ValidationViolation("Alto file: " +altoFile.getName() + " using " + ALTO_XSD, ex.getMessage()));
      }
    }

    if ((result != null) && (result.getViolationsList().size() > 0)) {
      log.info("Validation error(s):\n" + result.printResult());
      Validator.printResutlToFile(cdmId, "Validation alto files: \n " + Joiner.on("\n").join(result.violationsList));      
      if (throwException) {        
        throw new ValidationException("Validation error(s):\n" + result.printResult(), ErrorCodes.VALIDATE_ALTO_FILES);
      }
    } else {
      log.info("No validation error(s)");
    }
    
    return result;
  }
}
