package com.logica.ndk.tm.utilities.integration.aleph;

import java.util.HashMap;
import java.util.Map;

import org.drools.runtime.process.WorkItem;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.jbpm.handler.file.CreateEmptyCdmSyncHandler;
import com.logica.ndk.tm.utilities.HandlerTestHelper;
import com.logica.ndk.tm.utilities.TestWorkItemBase;
import com.logica.ndk.tm.utilities.TestWorkItemManagerBase;

public class SaveAlephMetadataSyncHandlerIT extends HandlerTestHelper {

  @Ignore
  public void test() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("barCode", "111-222-333-4444-5555");
    parameters.put("alephMetadata", "aaa");
    final WorkItem wi = new TestWorkItemBase(parameters) {
      // just in the case I need to customize something
    };
    final TestWorkItemManagerBase wim = new TestWorkItemManagerBase() {
      // just in the case I need to customize something
    };
    HandlerTestHelper.execute(new CreateEmptyCdmSyncHandler(), wi, wim, 5000);
    parameters.put("cdmId", wim.getResult(wi.getId(), "result"));
    HandlerTestHelper.execute(new SaveAlephMetadataSyncHandler(), wi, wim, 5000);
  }

}
