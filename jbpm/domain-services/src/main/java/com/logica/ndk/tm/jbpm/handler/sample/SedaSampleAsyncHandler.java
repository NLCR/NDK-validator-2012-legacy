package com.logica.ndk.tm.jbpm.handler.sample;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.sample.SedaSample;

/**
 * @author Rudolf Daco
 *
 */
public class SedaSampleAsyncHandler extends AbstractAsyncHandler {
  @Override
  protected String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String param = (String) workItem.getParameter("param");
    final String timeInMillis = (String) workItem.getParameter("timeInMillis");
    final AsyncCallInfo<SedaSample> aci = new AsyncCallInfo<SedaSample>("sedaSampleEndpoint", SedaSample.class, paramUtility);
    aci.getClient().executeAsync(param, Long.parseLong(timeInMillis));
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("result", response);
    return result;
  }
}
