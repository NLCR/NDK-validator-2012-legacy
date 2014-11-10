package com.logica.ndk.tm.utilities.transformation.sip2;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

public class CheckExistenceOfUuidAsyncHandler extends AbstractAsyncHandler{
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    log.info("executeAsyncWorkItem started");    
    final String uuid = (String) workItem.getParameter("uuid");
    checkNotNull(uuid, "uuid must not be null");
    
    final String locality = (String) workItem.getParameter("locality");
    checkNotNull(locality, "locality must not be null");
    
    final String throwExceptionOnExist = (String) workItem.getParameter("throwExceptionOnExist");
    checkNotNull(throwExceptionOnExist, "throwExceptionOnExist must not be null");
    final boolean throwExceptionOnExistBool = Boolean.parseBoolean(throwExceptionOnExist);
    log.debug("throwExceptionOnExist: " + throwExceptionOnExistBool);
    
    final AsyncCallInfo<CheckExistenceOfUuid> aci = new AsyncCallInfo<CheckExistenceOfUuid>("checkExistenceOfUuidEndpoint", CheckExistenceOfUuid.class, paramUtility);
    aci.getClient().executeAsync(uuid, locality, throwExceptionOnExistBool);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}