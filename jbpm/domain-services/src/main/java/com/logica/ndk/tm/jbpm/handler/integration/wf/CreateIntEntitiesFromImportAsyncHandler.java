package com.logica.ndk.tm.jbpm.handler.integration.wf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.integration.wf.CreateIntEntitiesFromImport;

public class CreateIntEntitiesFromImportAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final Long taskId = Long.valueOf((String)workItem.getParameter("taskId"));
    final String url = (String)workItem.getParameter("url");
    
    Preconditions.checkNotNull(taskId, "taskId must not be null");
    
    final AsyncCallInfo<CreateIntEntitiesFromImport> aci = new AsyncCallInfo<CreateIntEntitiesFromImport>("createIntEntitiesFromImportEndpoint", CreateIntEntitiesFromImport.class, paramUtility);
    aci.getClient().executeAsync(taskId, url);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}
