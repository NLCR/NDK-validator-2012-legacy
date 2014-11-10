package com.logica.ndk.tm.utilities.em;

import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ResponseStatus;

public class ImportFromExternalFileImpl extends AbstractUtility {

  public String importFromExternalFile(String cdmId, String fileUrl) {
    log.info("importFromExternalFile(" + cdmId + "," + fileUrl + ")");
    // TODO not implemented yet!
    final String result = ResponseStatus.RESPONSE_OK;
    log.info("importFromExternalFile " + result);
    return result;
  }

}
