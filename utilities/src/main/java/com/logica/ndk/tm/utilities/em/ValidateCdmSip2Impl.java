package com.logica.ndk.tm.utilities.em;

import java.util.ArrayList;
import java.util.List;

import com.logica.ndk.tm.utilities.AbstractUtility;

public class ValidateCdmSip2Impl extends AbstractUtility {

  public List<ValidationViolation> validate(String cdmId) {
    log.info("validate(" + cdmId + ")");
    // TODO not implemented yet!
    final List<ValidationViolation> result = new ArrayList<ValidationViolation>();
    log.info("validate " + result);
    return result;
  }

}
