package com.logica.ndk.tm.utilities.transformation.scantailor;

import static com.google.common.base.Preconditions.checkNotNull;

import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ResponseStatus;

public class ImportScantailorResultImpl extends AbstractUtility {

  public String execute(String cdmId) {
    checkNotNull(cdmId);
    log.info("cdmId: " + cdmId);
    return ResponseStatus.RESPONSE_OK;
  }

}
