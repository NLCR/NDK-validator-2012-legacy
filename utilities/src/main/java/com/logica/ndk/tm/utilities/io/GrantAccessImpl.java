package com.logica.ndk.tm.utilities.io;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ResponseStatus;

/**
 * Implementation of {@link GrantAccess} WS interface.
 * 
 * @author ondrusekl
 */
public class GrantAccessImpl extends AbstractUtility {

  public String grant(String user, String cdmId) {
    checkNotNull(user, "user must not be null");
    checkArgument(!user.isEmpty(), "user must not be empty");
    checkNotNull(cdmId, "cdmId must not be null");
    checkArgument(!cdmId.isEmpty(), "cdmId must not be empty");

    String permission = TmConfig.instance().getString("utility.io.defaultGrantPermission");

    log.info("grant started for user: " + user + ", CDM ID: " + cdmId);
    
    CDM cdm = new CDM();

    String path = cdm.getCdmDir(cdmId).getAbsolutePath();
    checkDirectory(path);

    PermissionsHelper.grant(user, path, true, false, new String[] { permission });

    log.info("grant finished");
    return ResponseStatus.RESPONSE_OK;
  }

}
