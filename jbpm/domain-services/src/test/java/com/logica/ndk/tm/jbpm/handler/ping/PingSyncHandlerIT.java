package com.logica.ndk.tm.jbpm.handler.ping;

import java.util.HashMap;
import java.util.Map;

import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.process.util.ParamUtilityPriority;
import com.logica.ndk.tm.utilities.HandlerTestHelper;
import com.logica.ndk.tm.utilities.TestWorkItemBase;
import com.logica.ndk.tm.utilities.TestWorkItemManagerBase;

public class PingSyncHandlerIT extends HandlerTestHelper {

  @Ignore
  public void test() {
    Map<String, Object> params = new HashMap<String, Object>();
    // test pre strplaceholder
    params.put("error", "${value_for_error}");
    params.put("value_for_error", "false");
    params.put("test_param_from_tm_config", "${cygwinHome}");
    params.put("cdmId", "38039c03-778e-479d-a2df-237941b061ef");
    params.put("test_param_from_CDM", "${MC_DIR}");
    params.put(ParamUtilityPriority.PARAMETER_NAME_DEFAULT, 2);
    final WorkItem wi = new TestWorkItemBase(params) {
      // just in the case I need to customize something
    };
    final WorkItemManager wim = new TestWorkItemManagerBase() {
      // just in the case I need to customize something
    };
    PingSyncHandler pingSyncHandler = new PingSyncHandler();
    pingSyncHandler.executeWorkItem(wi, wim);
  }

}
