package com.logica.ndk.tm.jbpm.handler.transformation;

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
import com.logica.ndk.tm.utilities.transformation.CreateCdmFromSkenSyncHandler;

/**
 * @author rudi
 */
public class CreateCdmFromSkenSyncHandlerIT extends HandlerTestHelper {
  private Map<String, Object> parameters;

  @Before
  public void before() {
    parameters = new HashMap<String, Object>();
    parameters.put("cdmId", "ANL000001");
  }

  @Ignore
  public void test() {
    final WorkItem wi = new TestWorkItemBase(parameters) {
      // just in the case I need to customize something
    };
    final WorkItemManager wim = new TestWorkItemManagerBase() {
      // just in the case I need to customize something
    };
    HandlerTestHelper.execute(new CreateCdmFromSkenSyncHandler(), wi, wim, 5000);
  }

}
