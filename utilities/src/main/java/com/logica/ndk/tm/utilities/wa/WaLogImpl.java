package com.logica.ndk.tm.utilities.wa;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ResponseStatus;

public class WaLogImpl extends AbstractUtility {

  private WaLogDAO waLogDAO;

  public String execute(String cdmId) throws WAException {
    checkNotNull(cdmId, "cdmId must not be null");
    log.info("WaLog started.");

    // number of records in WA from CDM property
    String recordsInWAString = new CDM().getCdmProperties(cdmId).getProperty(WarcDumpImpl.CDM_PROPERTY_WA_TOTAL_RECORDS, null);
    Long recordsInWA = null;
    if (recordsInWAString != null) {
      try {
        recordsInWA = Long.valueOf(recordsInWAString);
      }
      catch (NumberFormatException e) {
        log.error("Incorrect format of CDM property " + WarcDumpImpl.CDM_PROPERTY_WA_TOTAL_RECORDS + " for cdmId: " + cdmId, e);
        throw new WAException("Incorrect format of CDM property " + WarcDumpImpl.CDM_PROPERTY_WA_TOTAL_RECORDS + " for cdmId: " + cdmId, e);
      }
    }
    else {
      log.error("Missing CDM property " + WarcDumpImpl.CDM_PROPERTY_WA_TOTAL_RECORDS + " for cdmId: " + cdmId);
      throw new WAException("Missing CDM property " + WarcDumpImpl.CDM_PROPERTY_WA_TOTAL_RECORDS + " for cdmId: " + cdmId);
    }
    WaLogEvent waLog = new WaLogEvent();
    waLog.setCdmId(cdmId);
    waLog.setFilesInWa(recordsInWA);
    waLog.setCreated(new Date());
    waLogDAO.insert(waLog);

    log.info("WaLog finished.");
    return ResponseStatus.RESPONSE_OK;
  }

  public WaLogDAO getWaLogDAO() {
    return waLogDAO;
  }

  public void setWaLogDAO(WaLogDAO waLogDAO) {
    this.waLogDAO = waLogDAO;
  }

}
