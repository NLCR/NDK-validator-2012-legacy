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
import com.logica.ndk.tm.utilities.integration.jbpm.GetJBPMProcessState;
import com.logica.ndk.tm.utilities.integration.jbpm.StartJBPMProcessElsewhere;

/**
 * @author kovalcikm
 *
 */
public class GetJBPMProcessStateAsyncHandler extends AbstractAsyncHandler {
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String processName = (String) workItem.getParameter("processName");
    final Integer processId = (Integer) workItem.getParameter("processId");

    Preconditions.checkNotNull(processName, "processName can not be null");
    Preconditions.checkNotNull(processId, "processId can not be null");
    log.info("processName: " + processName);
    log.info("parameters: " + processId);

    final AsyncCallInfo<GetJBPMProcessState> aci = new AsyncCallInfo<GetJBPMProcessState>("getJBPMProcessStateEndpoint", GetJBPMProcessState.class, paramUtility);
    aci.getClient().executeAsync(processName, processId);

    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}
