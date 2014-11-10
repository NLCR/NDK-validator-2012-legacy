/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.xml.ws.Response;

import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ResponseStatus;

/**
 * @author kovalcikm
 *         Checks if mets exists. If not it calls CreateMetsImpl
 */
public class CheckIfMetsExistsImpl extends AbstractUtility {

  public String execute(String cdmId) {
    log.info("CheckIfMetsExists stated. cdmId: " + cdmId);
    checkNotNull(cdmId);

    if ((cdm.getMetsFile(cdmId) == null) || (!cdm.getMetsFile(cdmId).exists())) {
      log.info("Mets file does not exists. Calling CreateMetsImpl.");
      new CreateMetsImpl().execute(cdmId);
    }
    else {
      log.info("Mets file exists.");
    }

    return ResponseStatus.RESPONSE_OK;
  }
}
