package com.logica.ndk.jbpm.core.integration.impl;

import java.util.List;

import javax.persistence.EntityManager;

import org.drools.persistence.TransactionManager;
import org.drools.persistence.info.WorkItemInfo;
import org.drools.runtime.EnvironmentName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.jbpm.core.SessionFactory;

/**
 * DAO pre pracu s WorkItemInfo.
 * 
 * @author Rudolf Daco
 */
//TODO [rda]: mozno by bolo vhodne to prerobit podobne ako JPAProcessInstanceDbLogExtend
public class WorkItemInfoDAOImpl implements WorkItemInfoDAO {
  private static final Logger LOG = LoggerFactory.getLogger(WorkItemInfoDAOImpl.class);

  private EntityManager entityManager;
  private static TransactionManager transactionManager;

  public WorkItemInfoDAOImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  private TransactionManager getTransactionManager() {

    if (transactionManager == null) {
      try {
        transactionManager = (TransactionManager) SessionFactory.getSession().getEnvironment().get(EnvironmentName.TRANSACTION_MANAGER);
      }
      finally {
        SessionFactory.returnSession();
      }
    }
    return transactionManager;

  }

  @SuppressWarnings("unchecked")
  @Override
  public List<WorkItemInfo> findAll() throws DAOException {
    List<WorkItemInfo> result = null;
    TransactionManager tmx = null;
    boolean txOwner = false;
    try {
      tmx = getTransactionManager();
      txOwner = tmx.begin();
      entityManager.joinTransaction();
      result = (List<WorkItemInfo>) entityManager.createQuery("FROM WorkItemInfo wii").getResultList();
      tmx.commit(txOwner);
    }
    catch (Exception e) {
      if (tmx != null) {
        try {
          tmx.rollback(txOwner);
        }
        catch (Exception e1) {
          LOG.error("Error at findAll.", e1);
          throw new DAOException("Error at findAll", e1);
        }
      }
      LOG.error("Error at findAll.", e);
      throw new DAOException("Error at findAll", e);
    }
    finally {
      SessionFactory.returnSession();
    }
    return result;
  }

  @Override
  public void delete(List<Long> idList) throws DAOException {
    if (idList == null || idList.size() == 0) {
      return;
    }
    TransactionManager tmx = null;
    boolean txOwner = false;
    try {
      tmx = getTransactionManager();
      txOwner = tmx.begin();
      entityManager.joinTransaction();
      for (Long workItemId : idList) {
        WorkItemInfo workItemInfo = (WorkItemInfo) entityManager.createQuery("FROM WorkItemInfo wii where wii.workItemId = :workItemId").setParameter("workItemId", workItemId).getSingleResult();
        if (workItemInfo != null) {
          LOG.debug("Deleting WorkItemInfo from DB. workItemId: " + workItemId);
          entityManager.remove(workItemInfo);
        }
      }
      tmx.commit(txOwner);
    }
    catch (Exception e) {
      if (tmx != null) {
        try {
          tmx.rollback(txOwner);
        }
        catch (Exception e1) {
          LOG.error("Error at delete.", e1);
          throw new DAOException("Error at delete", e1);
        }
      }
      LOG.error("Error at delete.", e);
      throw new DAOException("Error at delete", e);
    }
    finally {
      SessionFactory.returnSession();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<WorkItemInfo> find(long processInstanceId) throws DAOException {
    List<WorkItemInfo> result = null;
    TransactionManager tmx = null;
    boolean txOwner = false;
    try {
      tmx = getTransactionManager();
      txOwner = tmx.begin();
      entityManager.joinTransaction();
      result = (List<WorkItemInfo>) entityManager.createQuery("FROM WorkItemInfo wii where processInstanceId = :processInstanceId").setParameter("processInstanceId", processInstanceId).getResultList();
      tmx.commit(txOwner);
    }
    catch (Exception e) {
      if (tmx != null) {
        try {
          tmx.rollback(txOwner);
        }
        catch (Exception e1) {
          LOG.error("Error at find.", e1);
          throw new DAOException("Error at find", e1);
        }
      }
      LOG.error("Error at find.", e);
      throw new DAOException("Error at find", e);
    }
    finally {
      SessionFactory.returnSession();
    }
    return result;
  }
}
