package com.logica.ndk.jbpm.core.integration.impl;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TemporalType;

import org.drools.persistence.jta.JtaTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.jbpm.core.integration.api.NotifyProcess;

/**
 * @author brizat
 */
public class NotifyProcessDAOImpl implements NotifyProcessDAO {

  private static final Logger LOG = LoggerFactory.getLogger(NotifyProcessDAOImpl.class);
  private EntityManager entityManager;
  private JtaTransactionManager transactionManager;

  public NotifyProcessDAOImpl(EntityManager entityManager, JtaTransactionManager transactionManager) {
    this.entityManager = entityManager;
    this.transactionManager = transactionManager;
  }

  @Override
  public NotifyProcess add(NotifyProcess notifyProcess) throws DAOException {
    boolean begin = false;
    try {
      begin = transactionManager.begin();
      entityManager.joinTransaction();

      notifyProcess = entityManager.merge(notifyProcess);
      entityManager.flush();

      transactionManager.commit(begin);
    }
    catch (Exception ex) {
      transactionManager.rollback(begin);
      LOG.error("Error at add notifyProcess. " + ex);
      throw new DAOException("Error at add notifyProcess. ", ex);
    }
    finally{
      entityManager.clear();
      entityManager.close();
    }
    return notifyProcess;
  }

  @Override
  public void remove(NotifyProcess notifyProcess) throws DAOException {
    boolean begin = false;
    try {
      begin = transactionManager.begin();
      entityManager.joinTransaction();
      
      NotifyProcess find = entityManager.find(NotifyProcess.class, notifyProcess.getId());
      
      entityManager.remove(find);
      entityManager.flush();

      transactionManager.commit(begin);
    }
    catch (Exception ex) {
      transactionManager.rollback(begin);
      LOG.error("Error at remove notifyProcess. " + ex);
      throw new DAOException("Error at add notifyProcess. ", ex);
    }
    finally{
      entityManager.clear();
      entityManager.close();
    }
  }

  @Override
  public void modify(NotifyProcess notifyProcess) throws DAOException {
    boolean begin = false;
    try {
      begin = transactionManager.begin();
      entityManager.joinTransaction();
      
      NotifyProcess find = entityManager.find(NotifyProcess.class, notifyProcess.getId());
      
      find.setNotifyAt(notifyProcess.getNotifyAt());
      find.setState(notifyProcess.getState());
      
      entityManager.persist(find);
      entityManager.flush();

      transactionManager.commit(begin);
    }
    catch (Exception ex) {
      transactionManager.rollback(begin);
      LOG.error("Error at modify notifyProcess. " + ex);
      throw new DAOException("Error at add notifyProcess. ", ex);
    }
    finally{
      entityManager.clear();
      entityManager.close();
    }
  }

  public List<NotifyProcess> findByNotifyAt(Date date) throws DAOException{
    boolean begin = false;
    try {
      begin = transactionManager.begin();
      entityManager.joinTransaction();
      
      List<NotifyProcess> result = entityManager.createQuery("FROM NotifyProcess a where a.state = :state and a.notifyAt <= :date").
          setParameter("state", "waiting").setParameter("date", date, TemporalType.TIMESTAMP).getResultList();
      LOG.info("Result size: " + result.size());
      
      transactionManager.commit(begin);
      return result;
    }
    catch (Exception ex) {
      transactionManager.rollback(begin);
      LOG.error("Error at findByNotifyAt notifyProcess. " + ex);
      throw new DAOException("Error at add notifyProcess. ", ex);
    }
    finally{
      entityManager.clear();
      entityManager.close();
    }
  }

}
