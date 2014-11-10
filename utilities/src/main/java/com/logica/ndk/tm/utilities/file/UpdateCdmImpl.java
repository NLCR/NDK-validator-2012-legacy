package com.logica.ndk.tm.utilities.file;

import static com.google.common.base.Preconditions.checkNotNull;

import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.transformation.CreateMD5FileImpl;

/**
 * @author Rudolf Daco
 */
public class UpdateCdmImpl extends AbstractUtility {

  public String execute(String cdmId) {
    checkNotNull(cdmId);
    log.info("cdmId: " + cdmId);
    try {
      cdm.updateCdm(cdmId);
    }
    catch (CDMException e) {
      log.error("Error at updating CDM " + cdmId);
      throw e;
    }
//    new CreateMD5FileImpl().execute(cdmId);
    return ResponseStatus.RESPONSE_OK;
  }
}
