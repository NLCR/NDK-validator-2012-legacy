package com.logica.ndk.jbpm.core.integration.impl;

import com.logica.ndk.jbpm.core.integration.api.ActiveWorkItem;

/**
 * @author Rudolf Daco
 *
 */
public interface ActiveWorkItemService {
  public void add(ActiveWorkItem activeWorkItem) throws ServiceException;

  public void delete(String correlationId) throws ServiceException;

  public ActiveWorkItem get(String correlationId) throws ServiceException;
}
