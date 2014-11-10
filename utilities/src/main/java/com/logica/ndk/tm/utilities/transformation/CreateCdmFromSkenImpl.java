package com.logica.ndk.tm.utilities.transformation;

import static com.google.common.base.Preconditions.checkNotNull;

import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ResponseStatus;

public class CreateCdmFromSkenImpl extends AbstractUtility {

  public String execute(String cdmId) {
    checkNotNull(cdmId);
    log.info("cdmId: " + cdmId);
    // do nothing; CDM is already complete 
    return ResponseStatus.RESPONSE_OK;
  }

}
