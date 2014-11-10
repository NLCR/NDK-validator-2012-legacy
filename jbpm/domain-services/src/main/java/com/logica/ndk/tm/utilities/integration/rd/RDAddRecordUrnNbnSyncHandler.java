package com.logica.ndk.tm.utilities.integration.rd;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

public class RDAddRecordUrnNbnSyncHandler extends AbstractSyncHandler {

  @Override
  protected Map<String, Object> executeSyncWorkItem(WorkItem workItem,
      List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    log.info("executeSyncWorkItem started");

    final Map<String, Object> results = new HashMap<String, Object>();

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

    final SyncCallInfo<RDAddRecordUrnNbn> sci = new SyncCallInfo<RDAddRecordUrnNbn>("rdAddRecordUrnNbnEndpoint", RDAddRecordUrnNbn.class, paramUtility);
    boolean result = sci.getClient().addRecordUrnNbnSync(recordId, urns, new Date());
    results.put("result", result);

    log.info("executeSyncWorkItem finished");
    return results;
  }
}
