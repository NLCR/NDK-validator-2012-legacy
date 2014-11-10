package com.logica.ndk.tm.jbpm.handler;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActiveWorkItemManagerLockIT {
  protected final Logger log = LoggerFactory.getLogger(ActiveWorkItemManagerLockIT.class);

  @Ignore
  public void test() throws InterruptedException {
    new Thread1().start();
    Thread.sleep(100);
    new Thread1().start();
    Thread.sleep(100);
    new Thread2().start();
    Thread.sleep(100);
    new Thread2().start();
    Thread.sleep(50000);
  }

  class Thread1 extends Thread {
    public void run() {
      log.info("th1 start");
      ActiveWorkItemManagerLock.getInstance().inc();
      try {
        sleep(3000);
      }
      catch (InterruptedException e) {
        e.printStackTrace();
      }
      log.info("th1 body");
      ActiveWorkItemManagerLock.getInstance().dec();
      try {
        sleep(3000);
      }
      catch (InterruptedException e) {
        e.printStackTrace();
      }
      log.info("th1 end");
    }
  }

  class Thread2 extends Thread {
    public void run() {
      log.info("th2 start");
      ActiveWorkItemManagerLock.getInstance().waitForLock();
      log.info("th2 end");
    }
  }
}
