package com.logica.ndk.tm.jbpm.handler.transformation;

import java.util.HashMap;
import java.util.Map;

import org.drools.runtime.process.WorkItem;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.jbpm.handler.file.CreateEmptyCdmSyncHandler;
import com.logica.ndk.tm.utilities.HandlerTestHelper;
import com.logica.ndk.tm.utilities.TestWorkItemBase;
import com.logica.ndk.tm.utilities.TestWorkItemManagerBase;
import com.logica.ndk.tm.utilities.integration.aleph.GetAlephDataAsyncHandler;
import com.logica.ndk.tm.utilities.integration.aleph.SaveAlephMetadataSyncHandler;
import com.logica.ndk.tm.utilities.transformation.CreateMetsSyncHandler;

public class CreateMetsSyncHandlerIT extends HandlerTestHelper {

  @Ignore
  public void test() {
    Map<String, Object> params = new HashMap<String, Object>();
    final WorkItem wi = new TestWorkItemBase(params) {
      // just in the case I need to customize something
    };
    final TestWorkItemManagerBase wim = new TestWorkItemManagerBase() {
      // just in the case I need to customize something
    };
    params.put("barCode", "12123122342345");
    HandlerTestHelper.execute(new CreateEmptyCdmSyncHandler(), wi, wim, 5000);

    params.put("cdmId", wim.getResult(wi.getId(), "result"));
    params.put("barCode", "1000957006");
    params.put("libraryId", "NKCR");
    params.put("localBase", "NKC");
    HandlerTestHelper.execute(new GetAlephDataAsyncHandler(), wi, wim, 5000);
    params.put("alephMetadata", wim.getResult(wi.getId(), "result"));
    HandlerTestHelper.execute(new SaveAlephMetadataSyncHandler(), wi, wim, 5000);
    HandlerTestHelper.execute(new CreateMetsSyncHandler(), wi, wim, 5000);
  }

}
