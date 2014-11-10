package com.logica.ndk.tm.utilities.wa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author Rudolf Daco
 *
 */
public class ValidateWAAsyncHandler extends AbstractAsyncHandler {
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    //final String cdmId = (String) workItem.getParameter("cdmId");
    final String source = resolveParam((String) workItem.getParameter("source"), workItem.getParameters());
    final AsyncCallInfo<ValidateWA> aci = new AsyncCallInfo<ValidateWA>("validateWAEndpoint", ValidateWA.class, paramUtility);
    aci.getClient().executeAsync(source);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}
