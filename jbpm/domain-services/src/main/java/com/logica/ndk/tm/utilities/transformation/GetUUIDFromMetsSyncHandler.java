package com.logica.ndk.tm.utilities.transformation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author kovalm
 */
public class GetUUIDFromMetsSyncHandler extends AbstractSyncHandler {

  @Override
  public Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    final String metsFilePath = (String) workItem.getParameter("metsFilePath");
    Preconditions.checkNotNull(metsFilePath, "metsFilePath must not be null");
    final SyncCallInfo<GetUUIDFromMets> sci = new SyncCallInfo<GetUUIDFromMets>("getUUIDFromMetsEndpoint", GetUUIDFromMets.class, paramUtility);
    results.put("result", sci.getClient().executeSync(metsFilePath));
    return results;
  }
}
