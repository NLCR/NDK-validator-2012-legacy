package com.logica.ndk.tm.utilities.transformation.scantailor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author rudi
 */
public class CreateScantailorConfigSyncHandler extends AbstractSyncHandler {
  
  @Override
  public Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    final String cdmId = (String) workItem.getParameter("cdmId");
    final SyncCallInfo<CreateScantailorConfig> sci = new SyncCallInfo<CreateScantailorConfig>("createScantailorConfigEndpoint", CreateScantailorConfig.class, paramUtility);
    results.put("result", sci.getClient().executeSync(cdmId));
    return results;
  }
}
