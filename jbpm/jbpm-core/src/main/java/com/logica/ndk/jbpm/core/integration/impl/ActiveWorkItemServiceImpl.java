package com.logica.ndk.jbpm.core.integration.impl;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.transaction.TransactionManager;

import org.drools.persistence.jta.JtaTransactionManager;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.jbpm.core.SessionFactory;
import com.logica.ndk.jbpm.core.integration.api.ActiveWorkItem;

/**
 * @author Rudolf Daco
 */
public class ActiveWorkItemServiceImpl implements ActiveWorkItemService {
  private static final Logger LOG = LoggerFactory.getLogger(ActiveWorkItemServiceImpl.class);
  private static EntityManagerFactory entityManagerFactory = null;
  private static JtaTransactionManager jtaTransactionManager = null;

  private EntityManagerFactory getEntityManagerFactory() {
    if (entityManagerFactory == null) {
      try {
        entityManagerFactory = SessionFactory.getEntityManagerFactory();
      }
      finally {
        //SessionFactory.returnSession();
      }
    }
    return entityManagerFactory;

  }

  private JtaTransactionManager getTransactionManagerFactory() {
    if (jtaTransactionManager == null) {
      try {
        LOG.info("getTransactionManagerFactory started");
        Environment env = SessionFactory.getSession().getEnvironment();
        LOG.info("getTransactionManagerFactory have enviroment");
        jtaTransactionManager = (JtaTransactionManager) env.get(EnvironmentName.TRANSACTION_MANAGER);        
        LOG.info("getTransactionManagerFactory ended");
      }
      finally {
        SessionFactory.returnSession();
      }
    }
    return jtaTransactionManager;
  }

  public void add(ActiveWorkItem activeWorkItem) throws ServiceException {
    EntityManager em = null;
    try {
      em = getEntityManagerFactory().createEntityManager();
      new ActiveWorkItemDAOImpl(em, getTransactionManagerFactory()).add(activeWorkItem);
    }
    catch (Exception e) {
      LOG.error("Error at add activeWorkItem", e);
      throw new ServiceException("Error at add activeWorkItem", e);
    }    
  }

  public void delete(String correlationId) throws ServiceException {
    EntityManager em = null;
    try {
      em = getEntityManagerFactory().createEntityManager();
      new ActiveWorkItemDAOImpl(em, getTransactionManagerFactory()).delete(correlationId);
    }
    catch (Exception e) {
      LOG.error("Error at delete activeWorkItem", e);
      throw new ServiceException("Error at delete activeWorkItem", e);
    }    
  }

  public ActiveWorkItem get(String correlationId) throws ServiceException {
    LOG.info("get method started");
    ActiveWorkItem activeWorkItem = null;
    EntityManager em = null;
    try {
      LOG.info("start getting entity manager");
      em = getEntityManagerFactory().createEntityManager();
      LOG.info("start transacion manager factory");
      JtaTransactionManager transactionManagerFactory = getTransactionManagerFactory();
      LOG.info("creating of ActiveWorkItemDAOImpl ");
      activeWorkItem = new ActiveWorkItemDAOImpl(em, transactionManagerFactory).get(correlationId);
    }
    catch (Exception e) {
      LOG.error("Error at get activeWorkItem", e);
      throw new ServiceException("Error at get activeWorkItem", e);
    }    
    LOG.info("get method ended");
    return activeWorkItem;
  }
}
