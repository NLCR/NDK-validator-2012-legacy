package com.logica.ndk.tm.jbpm.handler.sample;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.sample.OtherSample;

/**
 * @author rudi
 */
public class OtherSampleAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String arg1 = (String) workItem.getParameter("arg1");
    final AsyncCallInfo<OtherSample> aci = new AsyncCallInfo<OtherSample>("otherSampleEndpoint", OtherSample.class, paramUtility);
    aci.getClient().executeAsync(arg1);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }

}
