package com.logica.ndk.tm.utilities.integration.aleph.notification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author Rudolf Daco
 *
 */
public class CreateAlephRecordSyncHandler extends AbstractSyncHandler {

  @Override
  public Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    final String cdmId = (String) workItem.getParameter("cdmId");
    final String docNumber = (String) workItem.getParameter("docNumber");
    final String locality = (String) workItem.getParameter("locality");
    final SyncCallInfo<CreateAlephRecord> sci = new SyncCallInfo<CreateAlephRecord>("createAlephRecordEndpoint", CreateAlephRecord.class, paramUtility);
    results.put("result", sci.getClient().executeSync(cdmId, docNumber, locality));
    return results;
  }

}
