package com.logica.ndk.tm.utilities.transformation;

import java.util.HashMap;
import java.util.Map;

import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.HandlerTestHelper;
import com.logica.ndk.tm.utilities.TestWorkItemBase;
import com.logica.ndk.tm.utilities.TestWorkItemManagerBase;

public class UpdateMetsFilesAsyncHandlerIT extends HandlerTestHelper {

  @Ignore
  public void test() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("cdmId", "29aba5d0-cdc2-11e1-91a2-00505682629d-master");
    final WorkItem wi = new TestWorkItemBase(params) {
      // just in the case I need to customize something
    };
    final WorkItemManager wim = new TestWorkItemManagerBase() {
      // just in the case I need to customize something
    };
    execute(new UpdateMetsFilesAsyncHandler(), wi, wim, 10000);
  }

}