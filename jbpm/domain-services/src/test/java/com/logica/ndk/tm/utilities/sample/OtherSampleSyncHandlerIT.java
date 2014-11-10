package com.logica.ndk.tm.utilities.sample;

import java.util.HashMap;
import java.util.Map;

import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.jbpm.handler.sample.OtherSampleSyncHandler;
import com.logica.ndk.tm.utilities.HandlerTestHelper;
import com.logica.ndk.tm.utilities.TestWorkItemBase;
import com.logica.ndk.tm.utilities.TestWorkItemManagerBase;

public class OtherSampleSyncHandlerIT extends HandlerTestHelper {

  @Ignore
  public void test() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("arg1", "other-sample-sync-test-arg1");
    final WorkItem wi = new TestWorkItemBase(parameters) {
      // just in the case I need to customize something
    };
    final WorkItemManager wim = new TestWorkItemManagerBase() {
      // just in the case I need to customize something
    };
    HandlerTestHelper.execute(new OtherSampleSyncHandler(), wi, wim, 5000);
  }
}
