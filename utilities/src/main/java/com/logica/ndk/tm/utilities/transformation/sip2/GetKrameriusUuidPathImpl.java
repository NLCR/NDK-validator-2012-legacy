/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.sip2;

import com.google.common.base.Strings;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;

/**
 * This utility returns path to element in tree in Kramerius. It is used for example for deleting.
 * @author kovalcikm
 *  */
public class GetKrameriusUuidPathImpl extends AbstractUtility {

  public String execute(String cdmId) {
    log.info("Utility GetKrameriusUuidPath started. cdmId: " + cdmId);
    StringBuilder builder = new StringBuilder();
    CDMMetsHelper metsHelper = new CDMMetsHelper();
    try {
      String titleUuid = metsHelper.getIdentifierFromMods(cdm, cdmId, CDMMetsHelper.DMDSEC_ID_MODS_TITLE, "uuid");
      if (!Strings.isNullOrEmpty(titleUuid)) {
        builder.append("uuid:").append(titleUuid);
      }
      String volumeUuid = metsHelper.getIdentifierFromMods(cdm, cdmId, CDMMetsHelper.DMDSEC_ID_MODS_VOLUME, "uuid");
      if (!Strings.isNullOrEmpty(volumeUuid)) {
        if (builder.length() > 0) {
          builder.append("/");
        }
        builder.append("uuid:").append(volumeUuid);
      }
      if (!cdmId.equalsIgnoreCase(volumeUuid)) {
        if (builder.length() > 0) {
          builder.append("/");
        }
        builder.append("uuid:").append(cdmId);
      }
    }
    catch (Exception e) {
      throw new BusinessException("Could not build uuid path", e);
    }
    String path = builder.toString();
    log.info("Path in Kramerius: " + path);
    return path;
  }

  public static void main(String[] args) {
    new GetKrameriusUuidPathImpl().execute("0ff077d0-2462-11e4-8f04-00505682629d");
  }
}
