/**
 * 
 */
package com.logica.ndk.tm.jbpm.ws;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.integration.jbpm.StartJBPMProcess;
import com.logica.ndk.tm.utilities.integration.jbpm.StartJBPMProcessElsewhere;
import com.logica.ndk.tm.utilities.io.CopyTo;

/**
 * @author kovalcikm
 */
public class StartJBPMProcessElsewhereAsyncHandler extends AbstractAsyncHandler {
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String processName = (String) workItem.getParameter("processName");
    final String params = (String) workItem.getParameter("parameters");

    Preconditions.checkNotNull(processName, "processName can not be null");
    log.info("processName: " + processName);
    log.info("parameters: " + params);

    final AsyncCallInfo<StartJBPMProcessElsewhere> aci = new AsyncCallInfo<StartJBPMProcessElsewhere>("startJBPMProcessElsewhereEndpoint", StartJBPMProcessElsewhere.class, paramUtility);
    aci.getClient().executeAsync(processName, params);

    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}
