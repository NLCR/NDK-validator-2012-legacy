package com.logica.ndk.tm.utilities.file;

import java.util.HashMap;
import java.util.Map;

import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.HandlerTestHelper;
import com.logica.ndk.tm.utilities.TestWorkItemBase;
import com.logica.ndk.tm.utilities.TestWorkItemManagerBase;

public class RsyncAsyncHandlerIT extends HandlerTestHelper {
  
  @Ignore
  public void test() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("pathId", "1362667e-7d96-44ae-a450-aa7288f77829");
    parameters.put("localURNString", "aaa:100100100:1:2");
    final WorkItem wi = new TestWorkItemBase(parameters) {
      // just in the case I need to customize something
    };
    final WorkItemManager wim = new TestWorkItemManagerBase() {
      // just in the case I need to customize something
    };
    HandlerTestHelper.execute(new RsyncAsyncHandler(), wi, wim, 500000);
  }
}
