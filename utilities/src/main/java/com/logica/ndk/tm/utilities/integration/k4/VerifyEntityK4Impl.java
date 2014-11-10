package com.logica.ndk.tm.utilities.integration.k4;

import static com.google.common.base.Preconditions.checkNotNull;

import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ResponseStatus;

/**
 * @author ondrusekl
 */
public class VerifyEntityK4Impl extends AbstractUtility {

  public String verify(String cdmId) {
    checkNotNull(cdmId);

    log.trace("create started");

    log.debug("Procesing cdmId={}", cdmId);

    log.trace("create finished");

    return ResponseStatus.RESPONSE_OK;
  }

}
