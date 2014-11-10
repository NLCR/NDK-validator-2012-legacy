package com.logica.ndk.tm.jbpm.handler.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;

public class CheckCounterSyncHandler extends AbstractSyncHandler {

  @Override
  public Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    final int limit = Integer.valueOf((String) workItem.getParameter("limit"));
    final int counter = (Integer) workItem.getParameter("counter");

    if (counter <= limit)
    {
      results.put("result", "OK");
    }
    else
    {
      throw new SystemException("Limit counter expired, Limit:" + limit + " count: " + counter, ErrorCodes.COUNTER_LIMIT_EXCEEDED);
    }
    return results;
  }

}
