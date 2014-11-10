package com.logica.ndk.jbpm.core.integration.impl;

/**
 * @author Rudolf Daco
 */
public interface MaintainanceService {
  /**
   * Resume all processes. Find all not finished WI handlers and execute them.
   * 
   * @throws ServiceException
   */
  public void resumeProcesses() throws ServiceException;

  /**
   * Resume process with this id.
   * 
   * @param processInstanceId
   * @throws ServiceException
   */
  public void resumeProcess(long processInstanceId) throws ServiceException;
}
