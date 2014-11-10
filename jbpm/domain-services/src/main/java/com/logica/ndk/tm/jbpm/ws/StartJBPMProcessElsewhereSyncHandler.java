/**
 * 
 */
package com.logica.ndk.tm.jbpm.ws;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Strings;
import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.ParamMap;
import com.logica.ndk.tm.process.ParamMapItem;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.integration.jbpm.StartJBPMProcess;
import com.logica.ndk.tm.utilities.integration.jbpm.StartJBPMProcessElsewhere;

/**
 * @author kovalcikm
 */
public class StartJBPMProcessElsewhereSyncHandler extends AbstractSyncHandler {

  @Override
  protected Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    final String processName = (String) workItem.getParameter("processName");
    final String paramsAsString = (String) workItem.getParameter("parameters");

    final SyncCallInfo<StartJBPMProcessElsewhere> sci = new SyncCallInfo<StartJBPMProcessElsewhere>("startJBPMProcessElsewhereEndpoint", StartJBPMProcessElsewhere.class, paramUtility);
    results.put("result", sci.getClient().executeSync(processName, paramsAsString));
    return results;
  }
}
