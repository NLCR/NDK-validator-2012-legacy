package com.logica.ndk.tm.utilities.transformation.em;

import java.util.HashMap;
import java.util.Map;

import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.HandlerTestHelper;
import com.logica.ndk.tm.utilities.TestWorkItemBase;
import com.logica.ndk.tm.utilities.TestWorkItemManagerBase;

public class DeleteByEmAsyncHandlerIT extends HandlerTestHelper {

  @Ignore
  public void test() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("cdmId", "e3b799c0-bea4-11e1-a2f9-001018b5eb5c");
    final WorkItem wi = new TestWorkItemBase(params) {
      // just in the case I need to customize something
    };
    final WorkItemManager wim = new TestWorkItemManagerBase() {
      // just in the case I need to customize something
    };
    execute(new DeleteByEmAsyncHandler(), wi, wim, 10000);
  }
}
