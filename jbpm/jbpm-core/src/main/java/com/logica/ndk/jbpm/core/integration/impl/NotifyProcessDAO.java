package com.logica.ndk.jbpm.core.integration.impl;

import java.util.Date;
import java.util.List;

import com.logica.ndk.jbpm.core.integration.api.NotifyProcess;

/**
 * @author brizat
 *
 */
public interface NotifyProcessDAO {

  NotifyProcess add(NotifyProcess notifyProcess) throws DAOException;
  
  void remove(NotifyProcess notifyProcess) throws DAOException;

  void modify(NotifyProcess notifyProcess) throws DAOException;   
  
  List<NotifyProcess> findByNotifyAt(Date date) throws DAOException;
}
