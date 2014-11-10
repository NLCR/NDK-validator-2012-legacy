package com.logica.ndk.tm.utilities.integration.aleph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

public class GetAlephDataSyncHandler extends AbstractSyncHandler {

  @Override
  public Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    String barCode = (String) workItem.getParameter("barCode");
    String libraryId = (String) workItem.getParameter("libraryId");
    String localBase = (String) workItem.getParameter("localBase");
    final SyncCallInfo<GetAlephData> sci = new SyncCallInfo<GetAlephData>("getAlephDataEndpoint", GetAlephData.class, paramUtility);
    results.put("result", sci.getClient().getBibliographicDataByBarCodeSync(barCode, libraryId, localBase));
    return results;
  }
}
