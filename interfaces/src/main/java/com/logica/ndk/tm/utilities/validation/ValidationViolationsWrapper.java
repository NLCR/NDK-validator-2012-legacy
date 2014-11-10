/**
 * 
 */
package com.logica.ndk.tm.utilities.validation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;
import com.logica.ndk.tm.utilities.em.ValidationViolation;

/**
 * @author kovalcikm
 */
public class ValidationViolationsWrapper implements Serializable {
  private static final long serialVersionUID = 5275693158863626208L;
  
  List<ValidationViolation> violationsList;

  public ValidationViolationsWrapper() {
    violationsList = new ArrayList<ValidationViolation>();
  }

  public void add(ValidationViolation violation) {
    this.violationsList.add(violation);
  }

  public List<ValidationViolation> getViolationsList() {
    return violationsList;
  }

  public void setViolationsList(List<ValidationViolation> violationsList) {
    this.violationsList = violationsList;
  }
  
  public String printResult() {
    String joined = Joiner.on("\n").join(violationsList);
    return joined;
  }

}
