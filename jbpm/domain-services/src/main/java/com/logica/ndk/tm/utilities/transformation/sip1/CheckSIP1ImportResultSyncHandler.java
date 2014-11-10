/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.sip1;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author londrusek
 */
public class CheckSIP1ImportResultSyncHandler extends AbstractSyncHandler {

  @Override
  protected Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    log.info("executeSyncWorkItem started");

    final Map<String, Object> results = new HashMap<String, Object>();
    final String cdmId = (String) workItem.getParameter("cdmId");
    checkNotNull(cdmId, "cdmId must not be null");
    log.debug("cdmId: " + cdmId);

    final SyncCallInfo<CheckSIP1ImportResult> sci = new SyncCallInfo<CheckSIP1ImportResult>("checkSIP1ImportResultEndpoint", CheckSIP1ImportResult.class, paramUtility);
    String status = sci.getClient().executeSync(cdmId);
    results.put("status", status);

    log.info("executeSyncWorkItem finished");
    return results;
  }

}
