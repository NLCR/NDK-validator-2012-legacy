/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.sip2;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author kovalcikm
 */
public class StartKrameriusProcessAsyncHandler extends AbstractAsyncHandler {
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    log.info("executeSyncWorkItem started");

    final String uuid = (String) workItem.getParameter("uuid");
    log.debug("uuid: " + uuid);
    
    final String locality = (String) workItem.getParameter("locality");
    checkNotNull(locality, "locality must not be null");
    log.debug("locality: " + locality);
    
    final String cdmId = (String) workItem.getParameter("cdmId");
    checkNotNull(cdmId, "cdmId must not be null");
    log.debug("cdmId: " + cdmId);
    
    final AsyncCallInfo<StartKrameriusProcess> aci = new AsyncCallInfo<StartKrameriusProcess>("startKrameriusProcessEndpoint", StartKrameriusProcess.class, paramUtility);
    aci.getClient().executeAsync(uuid, locality.toLowerCase(), cdmId);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}
