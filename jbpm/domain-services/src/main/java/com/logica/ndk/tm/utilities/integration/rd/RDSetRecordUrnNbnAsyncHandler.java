package com.logica.ndk.tm.utilities.integration.rd;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

public class RDSetRecordUrnNbnAsyncHandler extends AbstractAsyncHandler{
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    log.info("executeSyncWorkItem started");

      final Map<String, Object> results = new HashMap<String, Object>();
              
      Integer recordId;
      if (workItem.getParameter("recordId") != null) {
        recordId = Integer.valueOf((String)workItem.getParameter("recordId"));
      } else {
        recordId = 0;
      }
      
      List<String> urns = (List<String>) workItem.getParameter("urnNbn"); 

    final AsyncCallInfo<RDSetRecordUrnNbn> aci = new AsyncCallInfo<RDSetRecordUrnNbn>("rdSetRecordUrnNbnEndpoint", RDSetRecordUrnNbn.class, paramUtility);
    aci.getClient().setRecordUrnNbnAsync(recordId, urns, new Date());
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}