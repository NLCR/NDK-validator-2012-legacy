package com.logica.ndk.jbpm.core.integration.impl;

import java.util.List;

import org.drools.process.instance.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.commons.shutdown.ShutdownAttribute;
import com.logica.ndk.commons.shutdown.ShutdownException;
import com.logica.ndk.jbpm.core.CommandDelegate;
import com.logica.ndk.jbpm.core.SessionFactory;
import com.logica.ndk.jbpm.core.integration.ManagementFactory;

/**
 * @author Rudolf Daco
 */
public class MaintainanceServiceImpl implements MaintainanceService {
  private static final Logger LOG = LoggerFactory.getLogger(MaintainanceServiceImpl.class);

  private CommandDelegate delegate;

  public MaintainanceServiceImpl() {
    delegate = new CommandDelegate();
  }

  @Override
  public void resumeProcesses() throws ServiceException {
    checkShutdownAttribute();
    WorkItemInfoService workItemInfoService = new ManagementFactory().createWorkItemInfoService();
    LOG.info("Deleting garbage from WorkItemInfo DB table - started.");
    workItemInfoService.deleteGarbage();
    LOG.info("Deleting garbage from WorkItemInfo DB table - finished.");
    List<WorkItem> activeWIList = workItemInfoService.findAllWorkItem();
    for (WorkItem workItem : activeWIList) {
      LOG.info("Executing start for WI: id: " + workItem.getId() + " name: " + workItem.getName() + " processInstanceId:" + workItem.getProcessInstanceId());
      WorkItemHandler workItemHandlerClass = delegate.getWorkItemHandlerClass(workItem.getName());
      if (workItemHandlerClass != null) {
        try {
          workItemHandlerClass.executeWorkItem(workItem, SessionFactory.getSession().getWorkItemManager());
          LOG.info("Executing OK for WI: id: " + workItem.getId() + " name: " + workItem.getName() + " processInstanceId:" + workItem.getProcessInstanceId());
        }
        finally {
          SessionFactory.returnSession();
        }
      }
      else {
        LOG.error("Can't find workItemHandlerClass from list of existing definitions. Name of WI to find: " + workItem.getName());
        throw new ServiceException("Can't find workItemHandlerClass from list of existing definitions. Name of WI to find: " + workItem.getName());
      }
    }
  }

  @Override
  public void resumeProcess(long processInstanceId) throws ServiceException {
    checkShutdownAttribute();
    WorkItemInfoService workItemInfoService = new ManagementFactory().createWorkItemInfoService();
    LOG.info("Deleting garbage from WorkItemInfo DB table - started.");
    workItemInfoService.deleteGarbage();
    LOG.info("Deleting garbage from WorkItemInfo DB table - finished.");
    List<WorkItem> activeWIList = workItemInfoService.findWorkItem(processInstanceId);
    if (activeWIList == null || activeWIList.size() == 0) {
      throw new ServiceException("No active work item for process with id: " + processInstanceId + " Not possible to continue in this process. Check used instanceId or stop this process if really exists.");
    }
    for (WorkItem workItem : activeWIList) {
      LOG.info("Executing start for WI: id: " + workItem.getId() + " name: " + workItem.getName() + " processInstanceId:" + workItem.getProcessInstanceId());
      WorkItemHandler workItemHandlerClass = delegate.getWorkItemHandlerClass(workItem.getName());

      if (workItemHandlerClass != null) {
        try {
          workItemHandlerClass.executeWorkItem(workItem, SessionFactory.getSession().getWorkItemManager());
          LOG.info("Executing OK for WI: id: " + workItem.getId() + " name: " + workItem.getName() + " processInstanceId:" + workItem.getProcessInstanceId());
        }
        finally {
          SessionFactory.returnSession();
        }
      }
      else {
        LOG.error("Can't find workItemHandlerClass from list of existing definitions. Name of WI to find: " + workItem.getName());
        throw new ServiceException("Can't find workItemHandlerClass from list of existing definitions. Name of WI to find: " + workItem.getName());
      }
    }

  }

  private void checkShutdownAttribute() throws ShutdownException {
    ShutdownAttribute.checkShutdownAttribute();
  }
}
