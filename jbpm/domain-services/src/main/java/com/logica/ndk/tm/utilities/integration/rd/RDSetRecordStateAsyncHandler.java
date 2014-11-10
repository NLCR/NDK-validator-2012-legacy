package com.logica.ndk.tm.utilities.integration.rd;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

public class RDSetRecordStateAsyncHandler extends AbstractAsyncHandler{
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    log.info("executeSyncWorkItem started");

//      final String cdmId = (String) workItem.getParameter("cdmId");
//      checkNotNull(cdmId, "cdmId must not be null");
//      log.debug("cdmId: " + cdmId);
        
      
      Integer recordId;
      if (workItem.getParameter("recordId") != null) {
        recordId = Integer.valueOf((String)workItem.getParameter("recordId"));
      } else {
        recordId = 0;
      }
      String newState = (String) workItem.getParameter("newState");
      String oldState = (String) workItem.getParameter("oldState");
      String user = (String) workItem.getParameter("user");  
    final AsyncCallInfo<RDSetRecordState> aci = new AsyncCallInfo<RDSetRecordState>("rdSetRecordStateEndpoint", RDSetRecordState.class, paramUtility);
    aci.getClient().setRecordStateAsync(recordId, newState, oldState, user, new Date());
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}