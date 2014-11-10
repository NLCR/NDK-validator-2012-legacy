package com.logica.ndk.tm.utilities.transformation.djvu;

import java.util.HashMap;
import java.util.Map;

import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.HandlerTestHelper;
import com.logica.ndk.tm.utilities.TestWorkItemBase;
import com.logica.ndk.tm.utilities.TestWorkItemManagerBase;

public class ConvertDjVuToTiffAsyncHandlerIT extends HandlerTestHelper {

  @Ignore
  public void test() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("cdmId", "1eb2d310-baad-11e1-95c4-02004c4f4f50");
    parameters.put("source", "${RAW_DATA_DIR}\\img\\");
    parameters.put("target", "${WORKSPACE_DIR}\\MC_TIFF\\");
    final WorkItem wi = new TestWorkItemBase(parameters) {
      // just in the case I need to customize something
    };
    final WorkItemManager wim = new TestWorkItemManagerBase() {
      // just in the case I need to customize something
    };
    execute(new ConvertDjVuToTiffAsyncHandler(), wi, wim, 60000);
  }

}
