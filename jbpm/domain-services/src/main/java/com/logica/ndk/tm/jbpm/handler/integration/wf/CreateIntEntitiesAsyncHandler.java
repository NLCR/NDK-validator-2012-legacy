package com.logica.ndk.tm.jbpm.handler.integration.wf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.integration.wf.CreateIntEntities;

/**
 * @author majdaf
 */
public class CreateIntEntitiesAsyncHandler extends AbstractAsyncHandler {

  @SuppressWarnings("unchecked")
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final Long taskId = Long.valueOf((String)workItem.getParameter("taskId"));
    final String cdmId = (String)workItem.getParameter("cdmId");
    final List<String> childCdmIds = (List<String>)workItem.getParameter("childCdmIds");
    
    Preconditions.checkNotNull(taskId, "taskId must not be null");
    Preconditions.checkNotNull(cdmId, "cdmId must not be null");
    Preconditions.checkNotNull(childCdmIds, "childCdmIds must not be null");
    
    final AsyncCallInfo<CreateIntEntities> aci = new AsyncCallInfo<CreateIntEntities>("createIntEntitiesEndpoint", CreateIntEntities.class, paramUtility);
    aci.getClient().executeAsync(taskId, cdmId, childCdmIds);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }

}

