package com.logica.ndk.jbpm.core.integration.impl;

import java.util.List;

import org.drools.persistence.info.WorkItemInfo;

/**
 * DAO pre pracu s WorkItemInfo.
 * 
 * @author Rudolf Daco
 */
public interface WorkItemInfoDAO {
  /**
   * Finds all WorkItemInfo - this represents all WI handlers which was not finished. If WI is finished it is removed
   * from WorkItemInfo DB table.
   * 
   * @return
   * @throws DAOException
   */
  public List<WorkItemInfo> findAll() throws DAOException;

  /**
   * Zmaze z DB vsetky zaznamy WorkItemInfo pre dany zoznam id.
   * 
   * @param workItemIdList
   * @throws DAOException
   */
  public void delete(List<Long> workItemIdList) throws DAOException;

  /**
   * Finds by processInstanceId.
   * 
   * @param processInstanceId
   * @return
   * @throws DAOException
   */
  public List<WorkItemInfo> find(long processInstanceId) throws DAOException;
}
