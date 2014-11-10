/**
 * 
 */
package com.logica.ndk.tm.utilities.validation;

import java.util.HashMap;

import org.dom4j.Document;

import com.logica.ndk.tm.utilities.em.ValidationViolation;
import com.logica.ndk.tm.utilities.validator.structures.ValidationTemplate;
import com.logica.ndk.tm.utilities.validator.validator.ValidationResult;
import com.logica.ndk.tm.utilities.validator.validator.Validator;

/**
 * @author kovalcikm
 */
public class ValidateBiblioMetadata extends AbstractCdmValidation {
  
  public ValidationResult validateMonograph(Document metsFile, String profile, String defaulMessage, String cdmId){
    ValidationViolationsWrapper violationsWrapper = new ValidationViolationsWrapper();
    ValidationResult valResult = new ValidationResult(violationsWrapper, new HashMap<String, ValidationTemplate>());
    try {
      Validator validator = new Validator(metsFile, valResult, profile, cdmId);
      validator.setDefaultMainErrorMessage(defaulMessage);
      valResult = validator.validate();
    }
    catch (Exception e) {
      violationsWrapper.add(new ValidationViolation(defaulMessage, "Exception while validation: " + e.getMessage()));
    }
    return valResult;
  }
  
  public ValidationResult validatePeriodical(Document metsFile, String profile, String defaulMessage, String cdmId){
    ValidationViolationsWrapper violationsWrapper = new ValidationViolationsWrapper();
    ValidationResult valResult = new ValidationResult(violationsWrapper, new HashMap<String, ValidationTemplate>());
    try {
      Validator validator = new Validator(metsFile, valResult, profile, cdmId);
      validator.setDefaultMainErrorMessage(defaulMessage);
      valResult = validator.validate();
    }
    catch (Exception e) {
      violationsWrapper.add(new ValidationViolation(defaulMessage, "Exception while validation: " + e.getMessage()));
    }
    return valResult;
  }
  
}
