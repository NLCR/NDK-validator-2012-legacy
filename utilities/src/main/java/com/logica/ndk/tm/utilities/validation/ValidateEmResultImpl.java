package com.logica.ndk.tm.utilities.validation;

import static com.google.common.base.Preconditions.checkNotNull;

import org.dom4j.io.SAXReader;

import com.google.common.base.Joiner;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.em.ValidationViolation;
import com.logica.ndk.tm.utilities.validator.validator.ValidationResult;
import com.logica.ndk.tm.utilities.validator.validator.Validator;

/**
 * Validuje vysledok po EM este pred akymikolvek dalsimi cinnostami.
 * 
 * @author Rudolf Daco
 */
public class ValidateEmResultImpl extends ValidateBiblioMetadata {

  public final static String DOCUMENT_TYPE_PERIODICAL = "Periodical";
  public final static String DOCUMENT_TYPE_MONOGRAPH = "Monograph";
  private static String XPATH_TO_UUID = "//mets:mets/mets:dmdSec[@ID=\"{TYPE}\"]/mets:mdWrap/mets:xmlData/mods:mods/mods:identifier[@type=\"uuid\"]/text()";
  private static String MODS_VOLUME = "MODSMD_VOLUME_0001";
  private static String MODS_TITLE = "MODSMD_TITLE_0001";

  public ValidationViolationsWrapper validate(String cdmId, Boolean throwException) throws SystemException {
    log.info("ValidateEmResult start cdmId: " + cdmId);
    checkNotNull(cdmId);
    final ValidationViolationsWrapper result = new ValidationViolationsWrapper();

    CDM cdm = new CDM();
    org.dom4j.Document metsDocument;
    CDMMetsHelper helper = new CDMMetsHelper();

    SAXReader reader = new SAXReader();

    // validate exist
    validateExistsResources(result, cdmId);

    try {
      metsDocument = reader.read(cdm.getMetsFile(cdmId));

      if (helper.getDocumentType(cdmId).equals(DOCUMENT_TYPE_PERIODICAL)) {
        validateUuids(result, cdmId);
        ValidationResult validatePeriodical = validatePeriodical(metsDocument, "periodical_em_result", "Bibliographic metadata violation", cdmId);
        result.getViolationsList().addAll(validatePeriodical.getErrors().getViolationsList());
        
      }else if(helper.isMultiPartMonograph(cdmId)){
        validateUuids(result, cdmId);
        ValidationResult validatePeriodical = validateMonograph(metsDocument, "monograph_em_result", "Bibliographic metadata violation", cdmId);
        result.getViolationsList().addAll(validatePeriodical.getErrors().getViolationsList());
      }
      
    }
    catch (Exception e) {
      log.error("Error while retrieving document type from METS: " , e);
      throw new SystemException("Error while retrieving document type from METS", ErrorCodes.WRONG_METS_FORMAT);
    }

    //validation for periodical VOLUME

    // vyhodnoceni validace
    log.info("validate " + result);
    if (result != null && !result.getViolationsList().isEmpty()) {
      if (throwException) {
        throw new ValidationException("Validation error(s):\n" + result.printResult(), ErrorCodes.VALIDATE_EM_RESULT);
      }
      else {
        log.warn("Validation error(s):\n" + result.printResult());
      }
    }
    else {
      log.info("No validation error(s)");
    }
    log.info("ValidateEmResult end cdmId: " + cdmId);
    return result;
  }

  private void validateUuids(ValidationViolationsWrapper result, String cdmId) {
    CDM cdm = new CDM();
    CDMMetsHelper helper = new CDMMetsHelper();

    String titleUuid = helper.getValueFormMets(XPATH_TO_UUID.replace("{TYPE}", MODS_TITLE), cdm, cdmId);
    String volumeUuid = helper.getValueFormMets(XPATH_TO_UUID.replace("{TYPE}", MODS_VOLUME), cdm, cdmId);
    log.debug("Title uuid: " + titleUuid + ", volume uuid: " +volumeUuid);
    if (titleUuid == null || titleUuid.isEmpty()) {
      result.violationsList.add(new ValidationViolation("Bibliographic metadata violation", "Uuid for title cannot be empty"));
    }
    else if (volumeUuid == null || titleUuid.isEmpty()) {
      result.violationsList.add(new ValidationViolation("Bibliographic metadata violation", "Uuid for volume cannot be empty"));
    }    
    else if (titleUuid.equals(volumeUuid)) {
      result.violationsList.add(new ValidationViolation("Bibliographic metadata violation", "Uuid for title and volume cannot be same"));
    }
    Validator.printResutlToFile(cdmId, "Errors: \n" + Joiner.on("\n").join(result.getViolationsList().listIterator()));
  }
}
