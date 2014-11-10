/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author kovalcikm
 */
public class UpdateMetsSyncHandler extends AbstractSyncHandler {

  @Override
  protected Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    String cdmId = (String) workItem.getParameter("cdmId");

    final SyncCallInfo<UpdateMets> sci = new SyncCallInfo<UpdateMets>("updateMetsEndpoint", UpdateMets.class, paramUtility);
    results.put("result", sci.getClient().executeSync(cdmId));
    return results;
  }
}
