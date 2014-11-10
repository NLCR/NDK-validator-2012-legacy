package com.logica.ndk.tm.utilities.em;

import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ResponseStatus;

public class ImportFromCdmImpl extends AbstractUtility {

  public String importFromCdm(String cdmId, String sourceCdmId, String fileName) {
    log.info("importFromCdm(" + cdmId + "," + sourceCdmId + "," + fileName + ")");
    // TODO not implemented yet!
    final String result = ResponseStatus.RESPONSE_OK;
    log.info("importFromCdm " + result);
    return result;
  }

}
