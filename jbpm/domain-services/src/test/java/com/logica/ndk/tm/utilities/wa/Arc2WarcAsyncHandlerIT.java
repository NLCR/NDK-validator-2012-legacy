package com.logica.ndk.tm.utilities.wa;

import java.util.HashMap;
import java.util.Map;

import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.HandlerTestHelper;
import com.logica.ndk.tm.utilities.TestWorkItemBase;
import com.logica.ndk.tm.utilities.TestWorkItemManagerBase;

public class Arc2WarcAsyncHandlerIT extends HandlerTestHelper {
  @Ignore
  public void test() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("source", "c:\\NDK\\data_test\\_wa\\arc\\");
    parameters.put("target", "c:\\NDK\\data_test\\_wa\\arc\\");
    final WorkItem wi = new TestWorkItemBase(parameters) {
      // just in the case I need to customize something
    };
    final WorkItemManager wim = new TestWorkItemManagerBase() {
      // just in the case I need to customize something
    };
    execute(new Arc2WarcAsyncHandler(), wi, wim, 20000);
  }
}
