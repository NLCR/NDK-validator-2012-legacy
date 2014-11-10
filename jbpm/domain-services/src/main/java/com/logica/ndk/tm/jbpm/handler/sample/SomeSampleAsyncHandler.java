package com.logica.ndk.tm.jbpm.handler.sample;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.sample.SampleParam;
import com.logica.ndk.tm.utilities.sample.SomeSample;

/**
 * @author rudi
 */
public class SomeSampleAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String arg1 = (String) workItem.getParameter("arg1");
    final AsyncCallInfo<SomeSample> aci = new AsyncCallInfo<SomeSample>("someSampleEndpoint", SomeSample.class, paramUtility);
    aci.getClient().executeAsynchronous(new SampleParam("s5", "s6"), arg1);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    log.info("Response object: " + "\n" + response.toString());
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}
