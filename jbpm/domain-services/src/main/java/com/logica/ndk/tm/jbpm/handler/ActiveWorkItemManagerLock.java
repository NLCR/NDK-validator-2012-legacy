package com.logica.ndk.tm.jbpm.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author Rudolf Daco
 *
 */
public class ActiveWorkItemManagerLock {
  protected final Logger log = LoggerFactory.getLogger(ActiveWorkItemManagerLock.class);

  private static ActiveWorkItemManagerLock instance;
  private static final int DEFAULT_WAIT_TIMEOUT = 5000;
  private Integer count = new Integer(0);

  private ActiveWorkItemManagerLock() {
  }

  public static synchronized ActiveWorkItemManagerLock getInstance() {
    if (instance == null) {
      instance = new ActiveWorkItemManagerLock();
    }
    return instance;
  }

  public synchronized void inc() {
    count++;
  }

  /**
   * Descrement counter and wake up all threads which are waiting for 0 in waitForLock method.
   */
  public synchronized void dec() {
    count--;
    if (count.intValue() == 0) {
      notifyAll();
    }
  }

  /**
   * Wait if counter is not 0. Do not use while cycle here because we need some timeout which ends this check method
   * finally.
   */
  public synchronized void waitForLock() {
    if (count.intValue() > 0) {
      try {
        log.info("ActiveWorkItemServiceLock waiting: " + DEFAULT_WAIT_TIMEOUT + " ms");
        wait(DEFAULT_WAIT_TIMEOUT);
      }
      catch (InterruptedException e) {
        log.error("Error at ActiveWorkItemServiceLock.check()");
        throw new SystemException("Error at ActiveWorkItemServiceLock.check()",ErrorCodes.WAITING_FOR_LOCK_FAILED);
      }
    }
  }
}
