package com.logica.ndk.jbpm.core.integration.impl;

import java.util.Date;

/**
 * @author brizat
 *
 */
public interface AsyncTimerService {
  
  void register(Long workItemId, Long processInstanceId, Integer secondsToWait) throws DAOException;
  void notifyNow()  throws DAOException;
  void notifyAtDateTime(Date dateTime)  throws DAOException;
  
}
