package com.logica.ndk.tm.jbpm.handler.sample;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.sample.OtherSample;

/**
 * @author rudi
 */
public class OtherSampleSyncHandler extends AbstractSyncHandler {

  @Override
  public Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    final String arg1 = (String) workItem.getParameter("arg1");
    final SyncCallInfo<OtherSample> sci = new SyncCallInfo<OtherSample>("otherSampleEndpoint", OtherSample.class, paramUtility);
    results.put("result", sci.getClient().executeSync(arg1));
    return results;
  }

}
