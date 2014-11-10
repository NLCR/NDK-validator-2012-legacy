package com.logica.ndk.tm.jbpm.handler.transformation.jpeg2000;

import java.util.HashMap;
import java.util.Map;

import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.HandlerTestHelper;
import com.logica.ndk.tm.utilities.TestWorkItemBase;
import com.logica.ndk.tm.utilities.TestWorkItemManagerBase;

public class ConvertToJpeg2000AsyncHandlerIT extends HandlerTestHelper {

  @Ignore
  public void test() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("cdmId", "ANL000003");
    parameters.put("source", "${POSTPROCESSING_DATA_DIR}");
    parameters.put("target", "${MC_DIR}");
    parameters.put("profile", "utility.convertToJpeg2000.profile.masterCopy");
    final WorkItem wi = new TestWorkItemBase(parameters) {
      // just in the case I need to customize something
    };
    final WorkItemManager wim = new TestWorkItemManagerBase() {
      // just in the case I need to customize something
    };
    HandlerTestHelper.execute(new ConvertToJpeg2000AsyncHandler(), wi, wim, 5000);
  }

}
