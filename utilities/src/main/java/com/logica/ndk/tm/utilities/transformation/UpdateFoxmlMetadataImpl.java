/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.sip2.FedoraHelper;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author kovalcikm
 *         Utility updates Foxml parts defined in parameter and create new Foxmls in CDM which are ready to be sent to
 *         Fedora
 */
public class UpdateFoxmlMetadataImpl extends AbstractUtility {

  public static enum SupportedMetadataPart {
    mods, dc, policy
  };

  public String execute(String cdmId, List<String> metadataParts, String locality, String policyFilePath, Boolean processPages) {
    Preconditions.checkNotNull(cdmId);
    Preconditions.checkNotNull(metadataParts);

    log.info("Utility UpdateFoxmlMetadataImpl started for cdmId: " + cdmId);
    log.info("Going to update emtadata parts: " + metadataParts);
    log.info("Locality: " + locality);

    Map<String, Boolean> partsToUpdate = initializeMap();

    for (String part : metadataParts) {
      if (!supported(part)) {
        throw new SystemException("Updating metadata part in FOXML for part: " + part + "not supported"); //TODO error code
      }
      else {
        partsToUpdate.put(part, true);
      }
    }

    if (partsToUpdate.get(SupportedMetadataPart.policy.toString()) && !new File(policyFilePath).exists()) {
      throw new SystemException("No policy csv file for updating policy part. Policy file: " + policyFilePath);
    }
    
    FedoraHelper fedoraHelper = new FedoraHelper(locality,cdmId);
    fedoraHelper.updateMetadataForIE(cdmId, locality, partsToUpdate, policyFilePath, processPages);
    File updateK4 = new File(cdm.getWorkspaceDir(cdmId), "processUpdateK4");
    if(!updateK4.exists()){
        try {
            updateK4.createNewFile();
        } catch (IOException ex) {
            throw new SystemException("Could not create flag file", ex);
        }
    }
    log.info("Utility UpdateFoxmlMetadataImpl finished for cdmId: " + cdmId);

    return ResponseStatus.RESPONSE_OK;
  }

  private static boolean supported(String test) {

    for (SupportedMetadataPart c : SupportedMetadataPart.values()) {
      if (c.name().equals(test)) {
        return true;
      }
    }
    return false;
  }

  private Map<String, Boolean> initializeMap() {
    Map<String, Boolean> partsToUpdate = new HashMap<String, Boolean>();
    for (SupportedMetadataPart c : SupportedMetadataPart.values()) {
      partsToUpdate.put(c.toString(), false);
    }
    return partsToUpdate;
  }
}
