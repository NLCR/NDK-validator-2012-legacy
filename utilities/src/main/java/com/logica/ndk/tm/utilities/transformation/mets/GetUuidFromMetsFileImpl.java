package com.logica.ndk.tm.utilities.transformation.mets;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;

/**
 * 
 * 
 * @author brizat
 *
 */
public class GetUuidFromMetsFileImpl extends AbstractUtility{
  
  private static String UUID_TYPE = "uuid";
  
  public String execute(String cdmId){
    log.info("Execute GetUuidFromMetsFileImpl started");
    log.info("CdmId: " + cdmId);
    CDMMetsHelper metsHelper = new CDMMetsHelper();
    try {
      return metsHelper.getIdentifierFromMods(new CDM(), cdmId, UUID_TYPE);
    }
    catch (Exception e) {
      log.error("Error while getting uuid from mets file: "  , e);
      throw new BusinessException("Error while getting uuid from mets file: " + e.getMessage(), ErrorCodes.IMPORT_LTP_GETING_UUID_ERROR);
    }
  }

}
