package com.logica.ndk.tm.utilities.integration.rd;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

public class RDAddRecordUrnNbnAsyncHandler extends AbstractAsyncHandler {
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");
    log.info("executeSyncWorkItem started");

    Integer recordId;
    if (workItem.getParameter("recordId") != null) {
      recordId = Integer.valueOf((String) workItem.getParameter("recordId"));
    }
    else {
      recordId = 0;
    }
    String urnNbn = (String) workItem.getParameter("urnNbn");

    List<String> urns = new ArrayList<String>();
    urns.add(urnNbn);
    final AsyncCallInfo<RDAddRecordUrnNbn> aci = new AsyncCallInfo<RDAddRecordUrnNbn>("rdAddRecordUrnNbnEndpoint", RDAddRecordUrnNbn.class, paramUtility);
    aci.getClient().addRecordUrnNbnAsync(recordId, urns, new Date());
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}
