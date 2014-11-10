package com.logica.ndk.tm.jbpm.handler.file;

import java.util.HashMap;
import java.util.Map;

import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.HandlerTestHelper;
import com.logica.ndk.tm.utilities.TestWorkItemBase;
import com.logica.ndk.tm.utilities.TestWorkItemManagerBase;

public class FileCharacterizationAsyncHandlerIT extends HandlerTestHelper {

  @Ignore
  public void test() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("cdmId", "ba99fe0b-24b8-4737-aa61-81b63a0bd776");
    parameters.put("fileCharType", "UC");
    final WorkItem wi = new TestWorkItemBase(parameters) {
      // just in the case I need to customize something
    };
    final WorkItemManager wim = new TestWorkItemManagerBase() {
      // just in the case I need to customize something
    };
    HandlerTestHelper.execute(new FileCharacterizationAsyncHandler(), wi, wim, 50000);
  }

}
