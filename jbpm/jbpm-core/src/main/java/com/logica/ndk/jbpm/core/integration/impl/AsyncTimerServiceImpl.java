/**
 * 
 */
package com.logica.ndk.jbpm.core.integration.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.drools.persistence.jta.JtaTransactionManager;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.springframework.scheduling.annotation.Scheduled;

import com.logica.ndk.jbpm.core.CommandDelegate;
import com.logica.ndk.jbpm.core.SessionFactory;
import com.logica.ndk.jbpm.core.integration.api.NotifyProcess;

/**
 * @author brizat
 *
 */
public class AsyncTimerServiceImpl implements AsyncTimerService {
  
  public static String WAITING_STATUS = "waiting";
  public static String NOTIFIED_STATUS = "notified";
  
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

  private JtaTransactionManager getTransactionManager() {
    if (jtaTransactionManager == null) {
      try {
        Environment env = SessionFactory.getSession().getEnvironment();
        jtaTransactionManager = (JtaTransactionManager) env.get(EnvironmentName.TRANSACTION_MANAGER);        
        
      }
      finally {
        SessionFactory.returnSession();
      }
    }
    return jtaTransactionManager;
  }
  
  @Override
  public void register(Long workItemId, Long processInstanceId, Integer secondsToWait) throws DAOException {
    Date now = new Date();
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.SECOND, secondsToWait);
    Date notifyAt = calendar.getTime();
    
    NotifyProcess notifyProcess = new NotifyProcess(processInstanceId, workItemId, now , notifyAt, WAITING_STATUS);
    new NotifyProcessDAOImpl(getEntityManagerFactory().createEntityManager(), getTransactionManager()).add(notifyProcess);
  }

  @Override
  @Scheduled(cron="*/10 * * * *")
  public void notifyNow() throws DAOException {
    notifyAtDateTime(new Date());
  }

  @Override
  public void notifyAtDateTime(Date dateTime) throws DAOException{
    List<NotifyProcess> utilityToFinish = new NotifyProcessDAOImpl(getEntityManagerFactory().createEntityManager(), getTransactionManager()).findByNotifyAt(dateTime);
    
    CommandDelegate commandDelegate = new CommandDelegate();
    
    for (NotifyProcess notifyProcess : utilityToFinish) {
      commandDelegate.completeWorkItem(notifyProcess.getWorkItemId(), new HashMap<String, Object>());
    }
    
    
    for (NotifyProcess notifyProcess : utilityToFinish) {
      notifyProcess.setState(NOTIFIED_STATUS);
      new NotifyProcessDAOImpl(getEntityManagerFactory().createEntityManager(), getTransactionManager()).modify(notifyProcess);
    }
  }

}
