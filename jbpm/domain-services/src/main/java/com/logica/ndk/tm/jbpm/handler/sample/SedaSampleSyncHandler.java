package com.logica.ndk.tm.jbpm.handler.sample;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.sample.SedaSample;


/**
 * @author Rudolf Daco
 *
 */
public class SedaSampleSyncHandler extends AbstractSyncHandler {

  @Override
  public Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    final String param = (String) workItem.getParameter("param");
    final String timeInMillisString = (String) workItem.getParameter("timeInMillis");
    Long timeInMillis = Long.parseLong(timeInMillisString);
    final SyncCallInfo<SedaSample> sci = new SyncCallInfo<SedaSample>("sedaSampleEndpoint", SedaSample.class, paramUtility);
    String execute = sci.getClient().execute(param, timeInMillis);
    results.put("result", execute);
    return results;
  }

}
