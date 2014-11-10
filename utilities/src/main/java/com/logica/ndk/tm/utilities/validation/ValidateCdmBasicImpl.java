package com.logica.ndk.tm.utilities.validation;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.dom4j.DocumentException;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.METSException;

import com.google.common.base.Joiner;
import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.em.ValidationViolation;
import com.logica.ndk.tm.utilities.validator.validator.Validator;

public class ValidateCdmBasicImpl extends AbstractCdmValidation {
  
  ValidationViolationsWrapper result;

  public ValidationViolationsWrapper validate(String cdmId, Boolean throwException) {
    log.info("validate(" + cdmId + ")");
    checkNotNull(cdmId);

    result = new ValidationViolationsWrapper();

    // validate exist
    validateExistsResources(result, cdmId);

    // validate cdm "bag"
    //TODO temporary commented
    //validateBagit(result, cdmId);

    // validate MD5
    // ValidationViolationsWrapper validateMD5Result = new ValidateMD5Impl().execute(cdmId, throwException);
    // result.getViolationsList().addAll(validateMD5Result.getViolationsList());

    // validate UTF-8
    //validateEncodingUTF8(result, cdmId);
    validateMixFiles(cdmId, result);
    try {
      checkUuid(cdmId, result);
    }
    catch (Exception e) {
      log.error("Error reading mets file for cdmId: " + cdmId, e);
      throw new SystemException("Error reading mets file for cdmId: " + cdmId, e, ErrorCodes.VALIDATE_CDM_BASIC);
    }
       
    if ((result != null) && (result.getViolationsList().size() > 0)) {
      Validator.printResutlToFile(cdmId, Joiner.on(System.getProperty("line.separator")).join(result.violationsList));
      if (throwException) {
        throw new ValidationException("Validation error(s):\n" + result.printResult(), ErrorCodes.VALIDATE_CDM_BASIC);
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
