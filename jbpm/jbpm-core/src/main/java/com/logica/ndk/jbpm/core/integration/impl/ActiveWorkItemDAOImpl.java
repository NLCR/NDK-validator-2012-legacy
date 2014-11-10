package com.logica.ndk.jbpm.core.integration.impl;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.TransactionManager;

import org.drools.persistence.jta.JtaTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.jbpm.core.integration.api.ActiveWorkItem;

/**
 * @author Rudolf Daco
 */
public class ActiveWorkItemDAOImpl implements ActiveWorkItemDAO {

    private static final Logger LOG = LoggerFactory.getLogger(ActiveWorkItemDAOImpl.class);
    private EntityManager entityManager;
    private JtaTransactionManager transactionManager;

    public ActiveWorkItemDAOImpl(EntityManager entityManager, JtaTransactionManager transactionManager) {
        this.entityManager = entityManager;
        this.transactionManager = transactionManager;
    }

//  private TransactionManager getTransactionManager() {
//    return (TransactionManager) SessionFactory.getSession().getEnvironment().get(EnvironmentName.TRANSACTION_MANAGER);
//  }
    @Override
    public void add(ActiveWorkItem activeWorkItem) throws DAOException {
      LOG.info("add method started!");
      boolean begin = false;
        try {
            begin = transactionManager.begin();
            entityManager.joinTransaction();
            entityManager.persist(activeWorkItem);
            entityManager.flush();
            transactionManager.commit(begin);
        } catch (Exception e) {
            LOG.error("Exception while adding activeWorkItem.", e);
            try {
                transactionManager.rollback(begin);
            } catch (Exception e1) {
                LOG.error("Error at rollback transaction.", e);
            }
            throw new DAOException("Error at add", e);
        }
        finally{
          entityManager.clear();
          entityManager.close();
        }
        LOG.info("add method e!");
    }

    @Override
    public void delete(String correlationId) throws DAOException {
      LOG.info("delete method started!");  
      boolean begin = false;
        try {
            begin = transactionManager.begin();
            entityManager.joinTransaction();
            ActiveWorkItem activeWorkItem = (ActiveWorkItem) entityManager.createQuery("FROM ActiveWorkItem a where a.correlationId = :correlationId").setParameter("correlationId", correlationId).getSingleResult();
            if (activeWorkItem != null) {
                entityManager.remove(activeWorkItem);
                entityManager.flush();
            }
            transactionManager.commit(begin);
        } catch (Exception e) {
            LOG.error("Exception while removing activeWorkItem for correlationId: " + correlationId, e);
            try {
                transactionManager.rollback(begin);
            } catch (Exception e1) {
                LOG.error("Error at rollback transaction.", e);
            }
            throw new DAOException("Error at delete", e);
        }
        finally{
          entityManager.clear();
          entityManager.close();
        }
        LOG.info("delete method started!");
    }

    @Override
    public ActiveWorkItem get(String correlationId) throws DAOException {
        LOG.info("Get method started!");
        ActiveWorkItem result = null;
        boolean begin = false;
        try {
            begin = transactionManager.begin();
            entityManager.joinTransaction();
            try {
                result = (ActiveWorkItem) entityManager.createQuery("FROM ActiveWorkItem a where a.correlationId = :correlationId").setParameter("correlationId", correlationId).getSingleResult();
            } catch (NoResultException e) {
                result = null;
            }
            
            transactionManager.commit(begin);
        } catch (Exception e) {
            LOG.error("Exception while retriving activeWorkItem for correlationId: " + correlationId, e);
            try {
                transactionManager.rollback(begin);
            } catch (Exception e1) {
                LOG.error("Error at rollback transaction.", e);
            }
        }
        finally{
          entityManager.clear();
          entityManager.close();
        }
        LOG.info("Get method ended!");
        return result;
    }
}
