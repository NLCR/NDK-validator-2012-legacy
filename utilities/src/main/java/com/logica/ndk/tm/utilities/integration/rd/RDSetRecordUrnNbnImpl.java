package com.logica.ndk.tm.utilities.integration.rd;

import java.util.Date;
import java.util.List;

import com.logica.ndk.commons.utils.DateUtils;
import com.logica.ndk.tm.utilities.integration.rd.exception.DigitizationRecordSystemException;
import com.logica.ndk.tm.utilities.integration.rd.ws.client.DigitizationRegistryException_Exception;

public class RDSetRecordUrnNbnImpl extends RDBase {

  public boolean setRecordUrnNbn(Integer recordId, List<String> urnNbnList, Date date) {

    log.info("setRecordUrnNbn started for record id = " + recordId);
    if (recordId == null || recordId == 0) {
      return true;
    }
    try {
      initConnection();
      return registry.setRecordUrnNbn(recordId, DateUtils.toXmlDateTime(date), urnNbnList);
    }
    catch (DigitizationRegistryException_Exception e) {
      log.error("setUrnNbn for recordId={} failed", recordId, e);
      throw new DigitizationRecordSystemException(e);
    }

  }
}
