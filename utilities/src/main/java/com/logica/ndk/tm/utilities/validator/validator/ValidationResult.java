/**
 * 
 */
package com.logica.ndk.tm.utilities.validator.validator;

import java.util.HashMap;
import java.util.Map;

import com.logica.ndk.tm.utilities.validation.ValidationViolationsWrapper;
import com.logica.ndk.tm.utilities.validator.structures.ValidationTemplate;

/**
 * @author brizat
 */
public class ValidationResult {

  ValidationViolationsWrapper errors;
  Map<String, ValidationTemplate> validations;

  public ValidationResult(ValidationViolationsWrapper errors, Map<String, ValidationTemplate> validations) {
    this.errors = errors;
    this.validations = validations;
  }

  public ValidationResult() {
    errors = new ValidationViolationsWrapper();
    validations = new HashMap<String, ValidationTemplate>();
  }

  public ValidationViolationsWrapper getErrors() {
    return errors;
  }

  public void setErrors(ValidationViolationsWrapper errors) {
    this.errors = errors;
  }

  public Map<String, ValidationTemplate> getValidations() {
    return validations;
  }

  public void setValidations(Map<String, ValidationTemplate> validations) {
    this.validations = validations;
  }

}
