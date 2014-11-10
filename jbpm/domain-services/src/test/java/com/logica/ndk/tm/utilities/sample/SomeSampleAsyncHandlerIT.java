package com.logica.ndk.tm.utilities.sample;

import java.util.HashMap;
import java.util.Map;

import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.jbpm.handler.sample.SomeSampleAsyncHandler;
import com.logica.ndk.tm.utilities.HandlerTestHelper;
import com.logica.ndk.tm.utilities.TestWorkItemBase;
import com.logica.ndk.tm.utilities.TestWorkItemManagerBase;

public class SomeSampleAsyncHandlerIT extends HandlerTestHelper {
  @Ignore
  public void test() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    //parameters.put("arg1", "ok");
    parameters.put("arg1", "ok");
    final WorkItem wi = new TestWorkItemBase(parameters) {
      // just in the case I need to customize something
    };
    final WorkItemManager wim = new TestWorkItemManagerBase() {
      // just in the case I need to customize something
    };
    HandlerTestHelper.execute(new SomeSampleAsyncHandler(), wi, wim, 5000);
  }
}
