/**
 * 
 */
package com.logica.ndk.tm.utilities.integration.aleph.notification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author kovalcikm
 */
public class CheckAlephResponseAsyncHandler extends AbstractAsyncHandler {
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String cdmId = (String) workItem.getParameter("cdmId");
    final String locality = (String) workItem.getParameter("locality");
    
    final AsyncCallInfo<CheckAlephResponse> aci = new AsyncCallInfo<CheckAlephResponse>("checkAlephResponseEndpoint", CheckAlephResponse.class, paramUtility);
    aci.getClient().executeAsync(cdmId, locality);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}
