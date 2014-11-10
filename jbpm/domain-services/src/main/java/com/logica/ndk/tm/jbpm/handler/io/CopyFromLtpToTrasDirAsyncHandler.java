package com.logica.ndk.tm.jbpm.handler.io;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.io.CopyFromLtpToTrasDir;

/**
 * @author brizat
 */
public class CopyFromLtpToTrasDirAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    String ulr = resolveParam((String) workItem.getParameter("url"), workItem.getParameters());
    String note = resolveParam((String) workItem.getParameter("note"), workItem.getParameters()); 
    String taskId = Long.toString(workItem.getProcessInstanceId());
    Preconditions.checkNotNull(ulr);
    Preconditions.checkNotNull(taskId);
    if(note == null){
      note = "";
    }
    
    
    final AsyncCallInfo<CopyFromLtpToTrasDir> aci = new AsyncCallInfo<CopyFromLtpToTrasDir>("copyFromLtpToTrasDirEndpoint", CopyFromLtpToTrasDir.class, paramUtility);
    aci.getClient().executeAsync(ulr, note, taskId);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }

}
