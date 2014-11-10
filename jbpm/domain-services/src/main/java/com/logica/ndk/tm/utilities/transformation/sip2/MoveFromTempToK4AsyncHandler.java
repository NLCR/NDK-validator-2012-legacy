package com.logica.ndk.tm.utilities.transformation.sip2;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

public class MoveFromTempToK4AsyncHandler extends AbstractAsyncHandler{

  @Override
  protected String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    log.info("execute MoveFromTempToK4 AsyncWorkItem started");

    final String cdmId = (String) workItem.getParameter("cdmId");
    checkNotNull(cdmId, "cdmId must not be null");
    log.debug("cdmId: " + cdmId);
    
    final String locality = (String) workItem.getParameter("locality");
    checkNotNull(locality, "locality must not be null");
    log.debug("locality: " + locality);

    final AsyncCallInfo<MoveFromTempToK4> aci = new AsyncCallInfo<MoveFromTempToK4>("moveFromTempToK4Endpoint", MoveFromTempToK4.class, paramUtility);
    aci.getClient().executeAsync(cdmId, locality.toLowerCase());

    log.info("execute MoveFromTempToK4 AsyncWorkItem finished");
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {

    log.info("processResponse MoveFromTempToK4  started");

    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);

    log.info("processResponse MoveFromTempToK4 finished");
    return results;
  }

}
