package com.logica.ndk.tm.jbpm.asyncTimer;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.jbpm.core.integration.impl.AsyncTimerServiceImpl;
import com.logica.ndk.jbpm.core.integration.impl.DAOException;

public class FinishRegistedWorkItemsJob implements Runnable{
  
  protected final Logger log = LoggerFactory.getLogger(getClass());
  
  private static FinishRegistedWorkItemsJob instance;
  
  public static FinishRegistedWorkItemsJob getInstance(){
    if(instance == null){
      instance = new FinishRegistedWorkItemsJob();
      Thread thread = new Thread(instance);
      thread.start();
    }
    return instance;
  }
  
  protected void execute(){
    log.info("FinishRegistedWorkItemsJob started");
    try {
      new AsyncTimerServiceImpl().notifyAtDateTime(new Date());
    }
    catch (DAOException e) {
      log.error("Error: " , e);
    }
  }

  @Override
  public void run() {
    while (true) {
      //log.info("FinishRegistedWorkItemsJob running");
      try {
        Thread.sleep(10000l);
      }
      catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      execute();
    }    
  }
  
  
  
}
