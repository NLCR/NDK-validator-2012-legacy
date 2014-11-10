package com.logica.ndk.jbpm.core.integration.impl;

import com.logica.ndk.jbpm.core.integration.api.ActiveWorkItem;

/**
 * @author Rudolf Daco
 *
 */
public interface ActiveWorkItemDAO {

  public void add(ActiveWorkItem activeWorkItem) throws DAOException;

  public void delete(String correlationId) throws DAOException;

  public ActiveWorkItem get(String correlationId) throws DAOException;

}
