package com.logica.ndk.tm.utilities.file;

import static com.google.common.base.Preconditions.checkNotNull;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ResponseStatus;

/**
 * @author brizat
 */
public class RemoveCDMByIdImpl extends AbstractUtility {

  public String execute(String id) throws CDMException {
    log.info("Utility RemoveCDMById started.");
    checkNotNull(id);
    if (id.isEmpty() || id.equals("")) {
      log.warn("CDM id is empty, skiping");
      return ResponseStatus.RESPONSE_OK;
    }
    log.info("Start removing CDM id: " + id);
    String cdmId = id;
    CDM cdm = new CDM();
    
    //Removing JPG-TIFF images
    try {
    	cdm.deleteJpgTiffImages(cdmId);
    } catch (CDMException ex) {
    	log.error(ex.getMessage());
    	throw ex;
    }
    
    //Removing referenced CDM
    try {
      String[] referencedCDM = cdm.getReferencedCdmList(cdmId);
      for (String refCMDId : referencedCDM) {
        log.info("Removing referenced CDM by id: " + refCMDId);
        cdm.deleteCdm(refCMDId);
      }
    }
    catch (CDMException ex) {
      log.info("No referenced CDM's found");
    }
    
    //Removing root CDM
    try {
      log.info("Removing CDM id: " + id);
      cdm.deleteCdm(cdmId);
    }
    catch (CDMException ex) {
      log.error("Error at removing CDM for id " + cdmId);
      throw ex;
    }
    return ResponseStatus.RESPONSE_OK;
  }

}
