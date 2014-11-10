package com.logica.ndk.tm.utilities.shutdown;

import com.logica.ndk.commons.shutdown.ShutdownAttribute;
import com.logica.ndk.tm.utilities.AbstractUtility;

/**
 * Utility for testing TM state
 * 
 * @author brizat
 *
 */
public class ShutDownServiceImpl extends AbstractUtility{
  
  /**
   * Return true if TM is shutting down
   * 
   * @return
   */  
  public Boolean isShuttingDown(){
    log.info("isShutting down utility started.");
    
    return ShutdownAttribute.isSet();    
  }
  
}
