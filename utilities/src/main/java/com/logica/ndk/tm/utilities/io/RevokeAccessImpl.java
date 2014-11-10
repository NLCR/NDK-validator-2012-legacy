package com.logica.ndk.tm.utilities.io;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ResponseStatus;

/**
 * Implementation of {@link RevokeAccess} WS interface.
 * 
 * @author ondrusekl
 */
public class RevokeAccessImpl extends AbstractUtility {

  public String revoke(String cdmId) {
    checkNotNull(cdmId, "cdmId must not be null");
    checkArgument(!cdmId.isEmpty(), "cdmId must not be empty");

    log.info("revoke started");

    CDM cdm = new CDM();

    String path = cdm.getCdmDir(cdmId).getAbsolutePath();

    checkDirectory(path);

    PermissionsHelper.reset(path, false);

    log.info("revoke finished");
    return ResponseStatus.RESPONSE_OK;
  }
}
