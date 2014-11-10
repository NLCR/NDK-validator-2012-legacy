package com.logica.ndk.tm.jbpm.handler.sample;

import java.util.HashMap;
import java.util.Map;

import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.HandlerTestHelper;
import com.logica.ndk.tm.utilities.TestWorkItemBase;
import com.logica.ndk.tm.utilities.TestWorkItemManagerBase;

/**
 * @author Rudolf Daco
 *
 */
public class SedaSampleSyncHandlerIT extends HandlerTestHelper {

  @Ignore
  public void test() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("param", "1");
    params.put("timeInMillis", "40000");
    final WorkItem wi = new TestWorkItemBase(params) {
      // just in the case I need to customize something
    };
    final WorkItemManager wim = new TestWorkItemManagerBase() {
      // just in the case I need to customize something
    };
    SedaSampleSyncHandler sedaSampleSyncHandler = new SedaSampleSyncHandler();
    sedaSampleSyncHandler.executeWorkItem(wi, wim);
  }

}
