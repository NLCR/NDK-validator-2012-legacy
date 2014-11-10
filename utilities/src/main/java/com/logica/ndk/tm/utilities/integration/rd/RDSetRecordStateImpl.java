package com.logica.ndk.tm.utilities.integration.rd;

import java.util.Date;

import com.logica.ndk.commons.utils.DateUtils;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.integration.rd.exception.DigitizationRecordSystemException;
import com.logica.ndk.tm.utilities.integration.rd.ws.client.DigitizationRegistry;
import com.logica.ndk.tm.utilities.integration.rd.ws.client.DigitizationRegistryException;
import com.logica.ndk.tm.utilities.integration.rd.ws.client.DigitizationRegistryException_Exception;

/**
 * Implementation of {@link RDSetRecordState} WS interface.
 * 
 * @author ondrusekl
 */
public class RDSetRecordStateImpl extends RDBase {

  public RDSetRecordStateImpl() {
  }

  // only for tests
  @SuppressWarnings("unused")
  private RDSetRecordStateImpl(DigitizationRegistry registry) {
    this.registry = registry;
  }

  public boolean setRecordState(Integer recordId,
      String newState,
      String oldState,
      String user,
      Date date) {

    log.trace("setRecordState started");

    boolean checkState = TmConfig.instance().getBoolean("rd.checkState", true);
    
    if (checkState) {
      initConnection();
      try {
        log.trace("setRecordState finished");
  
        return registry.setRecordState(recordId, toDigitizationState(newState), toDigitizationState(oldState), user, DateUtils.toXmlDateTime(date));
      }
      catch (DigitizationRegistryException_Exception e) {
        log.error("setRecordState for recordId={} failed", recordId);
        throw new DigitizationRecordSystemException(e);
      }
    } else {
      return true;
    }
  }

}
