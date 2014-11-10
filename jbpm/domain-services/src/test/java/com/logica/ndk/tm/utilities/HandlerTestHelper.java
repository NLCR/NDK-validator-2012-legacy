package com.logica.ndk.tm.utilities;

import static com.google.common.base.Preconditions.checkNotNull;

import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.jbpm.handler.AbstractHandler;
import com.logica.ndk.tm.jbpm.handler.ping.PingAsyncHandler;
import com.logica.ndk.tm.jbpm.jms.ResponseMessageListener;

public class HandlerTestHelper {
  private static final Logger LOG = LoggerFactory.getLogger(HandlerTestHelper.class);

  @BeforeClass
  public static void beforeClass() {
    LOG.debug("before class was called");
    PingAsyncHandler.setTestRun(true);
    ResponseMessageListener.setTestRun(true);
  }

  public static void execute(AbstractHandler h, WorkItem wi, WorkItemManager wim, long waitTime) {
    checkNotNull(h);
    checkNotNull(wi);
    checkNotNull(wim);
    LOG.debug("Starting test for " + h.getClass().getSimpleName());
    LOG.debug("Executing: " + h.getClass().getSimpleName());
    h.executeWorkItem(wi, wim);
    LOG.debug("Waiting to receive message by ResponseMessageListener, waitTime=" + waitTime);
    try {
      Thread.sleep(waitTime);
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
    LOG.debug("Finished " + h.getClass().getSimpleName());
  }
}
