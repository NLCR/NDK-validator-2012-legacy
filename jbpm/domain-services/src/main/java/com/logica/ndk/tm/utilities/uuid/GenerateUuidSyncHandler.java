package com.logica.ndk.tm.utilities.uuid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author rudi
 */
public class GenerateUuidSyncHandler extends AbstractSyncHandler {
  @Override
  public Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) {
    final Map<String, Object> results = new HashMap<String, Object>();
    final SyncCallInfo<GenerateUuid> sci = new SyncCallInfo<GenerateUuid>("generateUuidEndpoint", GenerateUuid.class, paramUtility);
    results.put("result", sci.getClient().executeSync());
    return results;
  }
}
