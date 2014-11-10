/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author brizat
 *
 */
public class UpdateModsAndDcAsyncHandler extends AbstractAsyncHandler{
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    String cdmId = (String) workItem.getParameter("cdmId");

    final AsyncCallInfo<UpdateModsAndDc> aci = new AsyncCallInfo<UpdateModsAndDc>("updateModsAndDcEndpoint", UpdateModsAndDc.class, paramUtility);
    aci.getClient().executeAsync(cdmId);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}


