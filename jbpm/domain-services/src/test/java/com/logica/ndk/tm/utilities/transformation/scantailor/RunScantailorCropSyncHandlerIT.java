package com.logica.ndk.tm.utilities.transformation.scantailor;

import java.util.HashMap;
import java.util.Map;

import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.HandlerTestHelper;
import com.logica.ndk.tm.utilities.TestWorkItemBase;
import com.logica.ndk.tm.utilities.TestWorkItemManagerBase;

/**
 * @author rudi
 */
public class RunScantailorCropSyncHandlerIT extends HandlerTestHelper {
  private Map<String, Object> parameters;

  @Before
  public void before() {
    parameters = new HashMap<String, Object>();
    parameters.put("cdmId", "ANL000002");
  }

  @Ignore
  public void test() {
    final WorkItem wi = new TestWorkItemBase(parameters) {
      // just in the case I need to customize something
    };
    final WorkItemManager wim = new TestWorkItemManagerBase() {
      // just in the case I need to customize something
    };
    HandlerTestHelper.execute(new RunScantailorCropSyncHandler(), wi, wim, 5000);
  }

}