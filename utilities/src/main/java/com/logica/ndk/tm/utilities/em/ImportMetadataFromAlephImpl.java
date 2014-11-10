package com.logica.ndk.tm.utilities.em;

import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.integration.aleph.exception.AlephUnaccessibleException;

public class ImportMetadataFromAlephImpl extends AbstractUtility {

  public String importMetadataFromAleph(String cdmId) throws AlephUnaccessibleException {
    log.info("importMetadataFromAleph(" + cdmId + ")");
    // TODO not implemented yet!
    final String result = ResponseStatus.RESPONSE_OK;
    log.info("importMetadataFromAleph " + result);
    return result;
  }

}
