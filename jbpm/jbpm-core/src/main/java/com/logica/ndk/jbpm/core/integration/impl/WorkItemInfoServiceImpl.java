package com.logica.ndk.jbpm.core.integration.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.drools.persistence.info.WorkItemInfo;
import org.drools.process.instance.WorkItem;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.jbpm.core.SessionFactory;
import com.logica.ndk.jbpm.core.integration.api.ProcessInstanceEndLog;
import com.logica.ndk.jbpm.core.integration.api.ProcessInstanceEndLogFilter;

public class WorkItemInfoServiceImpl implements WorkItemInfoService {
  private static final Logger LOG = LoggerFactory.getLogger(WorkItemInfoServiceImpl.class);

  private EntityManagerFactory entityManagerFactory;

  private EntityManagerFactory getEntityManagerFactory() {
    if (entityManagerFactory == null) {
      try {
        Environment env = SessionFactory.getSession().getEnvironment();
        entityManagerFactory = (EntityManagerFactory) env.get(EnvironmentName.ENTITY_MANAGER_FACTORY);
      }
      finally {
        SessionFactory.returnSession();
      }
    }
    return entityManagerFactory;
  }

  @Override
  public List<WorkItemInfo> findAll() throws ServiceException {
    EntityManager em = null;
    try {
      em = getEntityManagerFactory().createEntityManager();
      return new WorkItemInfoDAOImpl(em).findAll();
    }
    catch (Exception e) {
      LOG.error("Error at findAll", e);
      throw new ServiceException("Error at findAll", e);
    }
    finally {
      if (em != null) {
        em.clear();
        em.close();
        SessionFactory.returnSession();
      }
    }
  }

  public List<WorkItem> findAllWorkItem() throws ServiceException {
    List<WorkItemInfo> list = findAll();
    List<WorkItem> result = new ArrayList<WorkItem>();
    try {
      if (list != null) {
        Environment env = SessionFactory.getSession().getEnvironment();
        for (WorkItemInfo workItemInfo : list) {
          WorkItem workItem = workItemInfo.getWorkItem(env);
          result.add(workItem);
        }
      }
    }
    finally {
      SessionFactory.returnSession();
    }
    return result;
  }

  @Override
  public List<WorkItemInfo> find(long processInstanceId) throws ServiceException {
    EntityManager em = null;
    try {
      em = getEntityManagerFactory().createEntityManager();
      return new WorkItemInfoDAOImpl(em).find(processInstanceId);
    }
    catch (Exception e) {
      LOG.error("Error at find", e);
      throw new ServiceException("Error at find", e);
    }
    finally {
      if (em != null) {
        em.clear();
        em.close();
        SessionFactory.returnSession();
      }
    }
  }

  @Override
  public List<WorkItem> findWorkItem(long processInstanceId) throws ServiceException {
    List<WorkItemInfo> list = find(processInstanceId);
    List<WorkItem> result = new ArrayList<WorkItem>();
    try {
      if (list != null) {
        Environment env = SessionFactory.getSession().getEnvironment();
        for (WorkItemInfo workItemInfo : list) {
          WorkItem workItem = workItemInfo.getWorkItem(env);
          result.add(workItem);
        }
      }
    }
    finally {
      SessionFactory.returnSession();
    }
    return result;
  }

  public void deleteGarbage() throws ServiceException {
    EntityManager em = null;
    List<WorkItemInfo> wiList = null;
    List<Long> listToRemove = new ArrayList<Long>();
    try {
      em = getEntityManagerFactory().createEntityManager();
      WorkItemInfoDAOImpl dao = new WorkItemInfoDAOImpl(em);
      wiList = dao.findAll();
      if (wiList != null) {
        for (WorkItemInfo workItemInfo : wiList) {
          ProcessInstanceEndLogFilter filter = new ProcessInstanceEndLogFilter();
          filter.setProcessInstanceId(workItemInfo.getProcessInstanceId());
          // process instance pre tento work item je uz ukoncena takze tento WI uz nie je aktivny - odstranime ho
          // taketo wi mozu vznikat ak synch handler hodi exception ale nestihne sa zaperzistovat ze tento wi skoncil
          // tato metoda sluzi zaroven na vycistenie tychto wi
          try {
            List<ProcessInstanceEndLog> processInstanceEndLog = new JPAProcessInstanceDbLogExtend(SessionFactory.getSession().getEnvironment()).findProcessInstanceEndLog(filter);

            if (processInstanceEndLog != null && processInstanceEndLog.size() > 0) {
              listToRemove.add(workItemInfo.getId());
            }
          }
          finally {
            SessionFactory.returnSession();
          }
        }
        dao.delete(listToRemove);
      }
    }
    catch (Exception e) {
      LOG.error("Error at findAll", e);
      throw new ServiceException("Error at findAll", e);
    }
    finally {
      if (em != null) {
        em.clear();
        em.close();
      }
    }
  }
}
