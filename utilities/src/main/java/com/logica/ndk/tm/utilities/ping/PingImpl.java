package com.logica.ndk.tm.utilities.ping;

import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Utility for ping - test if system is up and working.
 * 
 * @author rudi
 */
public class PingImpl extends AbstractUtility {
  public String execute(String error) throws PingException, BusinessException, SystemException {
    log.info("Test param: " + TmConfig.instance().getString("utility.ping.test"));
    log.info("error parameter: " + error);
    if (error != null && error.toLowerCase().equals("true")) {
      throw new PingException("Ping utility is throwing exception because this was requested by parameter 'error'.", ErrorCodes.PING_TEST);
    }
    return ResponseStatus.RESPONSE_OK;
  }
}
