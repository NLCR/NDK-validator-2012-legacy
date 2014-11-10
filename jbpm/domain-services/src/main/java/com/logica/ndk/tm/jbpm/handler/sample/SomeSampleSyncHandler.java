package com.logica.ndk.tm.jbpm.handler.sample;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.sample.SampleParam;
import com.logica.ndk.tm.utilities.sample.SampleResponse;
import com.logica.ndk.tm.utilities.sample.SomeSample;

/**
 * @author rudi
 */
public class SomeSampleSyncHandler extends AbstractSyncHandler {

  @Override
  public Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    final String arg1 = (String) workItem.getParameter("arg1");
    final SyncCallInfo<SomeSample> sci = new SyncCallInfo<SomeSample>("someSampleEndpoint", SomeSample.class, paramUtility);
    SampleResponse result = sci.getClient().execute(new SampleParam("s1", "s2"), arg1);
    log.info("Response object: " + "\n" + result.toString());
    results.put("result", result);
    return results;
  }

}
