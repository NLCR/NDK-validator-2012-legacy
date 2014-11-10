/**
 * 
 */
package com.logica.ndk.tm.jbpm.handler.integration.wf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.integration.wf.DeactivateInWF;

/**
 * @author kovalcikm
 *
 */
public class DeactivateInWFAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String cdmId = (String)workItem.getParameter("cdmId");
    final String recordIdentifier = (String)workItem.getParameter("recordIdentifier");
    
    Preconditions.checkNotNull(cdmId, "cdmId must not be null");
    Preconditions.checkNotNull(recordIdentifier, "recordIdentifier must not be null");
    
    final AsyncCallInfo<DeactivateInWF> aci = new AsyncCallInfo<DeactivateInWF>("deactivateInWFEndpoint", DeactivateInWF.class, paramUtility);
    aci.getClient().executeAsync(cdmId, recordIdentifier);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}