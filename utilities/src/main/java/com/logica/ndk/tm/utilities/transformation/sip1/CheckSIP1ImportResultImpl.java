package com.logica.ndk.tm.utilities.transformation.sip1;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.logica.ndk.tm.cdm.CDMMetsWAHelper;
import com.logica.ndk.tm.cdm.ImportFromLTPHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.transformation.sip1.exception.SIP1ImportFailedException;

public class CheckSIP1ImportResultImpl extends AbstractUtility {

  private static final String CDM_PROP_FILE_NAME = "cdmProperties.xml";

  public String execute(String cdmId) throws SIP1ImportFailedException {
    log.info("CheckSIP1ImportResult for CDM ID: " + cdmId);

    String status = getStatus(cdmId);
    log.info("Status: " + status);
    if (SIP1ImportConsts.SIP_STATUS_ERROR.equals(status)) {
      log.error("SIP1 import for CDM ID " + cdmId + " failed.");
      throw new SIP1ImportFailedException("CDM ID " + cdmId + " import failed, status: " + status, ErrorCodes.CHECK_SIP1_IMPORT_RESULT_FAILED);
    }
    else if (SIP1ImportConsts.SIP_STATUS_MISSING.equals(status)) {
      try {
        log.debug("Missing status return waiting");
        Thread.sleep(10000L);
      }
      catch (InterruptedException e) {
      }
      log.debug("Waiting is over checking status again");
      status = getStatus(cdmId);
      if (SIP1ImportConsts.SIP_STATUS_MISSING.equals(status)) {
        log.error("SIP1 import for CDM ID " + cdmId + " failed.");
        throw new SIP1ImportFailedException("CDM ID " + cdmId + " import failed, status: " + status, ErrorCodes.CHECK_SIP1_IMPORT_RESULT_FAILED);
      }
    }
    else {
      return status; 
    }
    return status;
  }

  private String getStatus(String cdmId) {
    String importDir = SIP1ImportConsts.SIP_IMPORT_DIR;
    File cdmPropertiesFile = new File(cdm.getCdmDir(cdmId), CDM_PROP_FILE_NAME);
    if (cdmPropertiesFile.exists()) {
      String documentType = cdm.getCdmProperties(cdmId).getProperty("documentType");
      if (documentType != null && (documentType.equals(CDMMetsWAHelper.DOCUMENT_TYPE_WEB_ARCHIVE) || documentType.equals(CDMMetsWAHelper.DOCUMENT_TYPE_WEB_ARCHIVE_HARVEST))) {
        importDir = SIP1ImportConsts.SIP_IMPORT_DIR_WA;
      }
      if (documentType != null && documentType.equals(CDMMetsWAHelper.DOCUMENT_TYPE_WEB_ARCHIVE)) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss", Locale.ENGLISH);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        String time = df.format(cal.getTime());
        cdm.updateProperty(cdmId, "ltpImportDate", time);
        log.debug("UpdatedltpImportTime for cdmId: " + cdmId + " to: " + time);
      }
    }

    if (ImportFromLTPHelper.isFromLTPFlagExist(cdmId)) {
      return SIP1ImportConsts.SIP_STATUS_DONE;
    }
    File importFolder = new File(importDir);

    for (String fName : importFolder.list()) {
      File f = new File(importFolder, fName);
      if (!f.isDirectory() || !f.getName().contains("_") || f.getName().split("_").length < 3) {
        continue;
      }

      String fCdmId = f.getName().substring(f.getName().indexOf("_") + 1);
      log.debug(fCdmId);
      if ((SIP1ImportConsts.SIP_CDM_PREFIX + cdmId).equals(fCdmId)) {
        log.debug("Found candidate " + f.getName());
        String status = f.getName().split("_")[0];
        log.debug("Status: " + status);
        return status;
      }
    }

    return SIP1ImportConsts.SIP_STATUS_MISSING;
  }

}
