/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.sip2;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author korvasm
 */
public class CheckKrameriusProcessResultSyncHandler extends AbstractSyncHandler {

  @Override
  protected Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    log.info("executeSyncWorkItem started");

    final Map<String, Object> results = new HashMap<String, Object>();
    final String processId = (String) workItem.getParameter("processId");
    checkNotNull(processId, "processId must not be null");
    log.debug("processId : " + processId);
    
    final String locality = (String) workItem.getParameter("location");
    checkNotNull(locality, "locality must not be null");
    log.debug("locality: " + locality);
    
    final String cdmId = (String) workItem.getParameter("cdmId");
    checkNotNull(cdmId, "cdmId must not be null");
    log.debug("cdmId: " + cdmId);

    final SyncCallInfo<CheckKrameriusProcessResult> sci = new SyncCallInfo<CheckKrameriusProcessResult>("checkKrameriusProcessResultEndpoint", CheckKrameriusProcessResult.class, paramUtility);
    String result = sci.getClient().executeSync(processId, locality.toLowerCase(), cdmId);
    results.put("result", result);

    log.info("executeSyncWorkItem finished");
    return results;
  }

}
