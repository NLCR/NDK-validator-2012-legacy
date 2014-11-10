/**
 * 
 */
package com.logica.ndk.tm.jbpm.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.premis.GeneratePremisManualOCR;

/**
 * @author kovalcikm
 *
 */
public class GeneratePremisManualOCRAsyncHandler extends AbstractAsyncHandler{
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String cdmId = resolveParam((String) workItem.getParameter("cdmId"), workItem.getParameters());
    final String agent = resolveParam((String) workItem.getParameter("agent"), workItem.getParameters());
    log.info("'agent' parameter retrieved: "+agent);
    
    Preconditions.checkNotNull(cdmId, "cdmId must not be null");
    Preconditions.checkNotNull(agent, "agent must not be null");
    
    final AsyncCallInfo<GeneratePremisManualOCR> aci = new AsyncCallInfo<GeneratePremisManualOCR>("generatePremisManualOCREndpoint", GeneratePremisManualOCR.class, paramUtility);
    aci.getClient().executeAsync(cdmId, agent);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }

}
