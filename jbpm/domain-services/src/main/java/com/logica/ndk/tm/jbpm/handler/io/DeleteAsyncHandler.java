package com.logica.ndk.tm.jbpm.handler.io;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.io.Delete;

/**
 * @author ondrusekl
 */
public class DeleteAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String path = resolveParam((String) workItem.getParameter("path"), workItem.getParameters());
    String throwNotFoudExObj = (String)workItem.getParameter("notThrowNotFoudEx");
    log.info("Parameter throwNotFoudEx: "+throwNotFoudExObj);
    
    boolean throwNotFoudEx = false;
    if(Boolean.parseBoolean(throwNotFoudExObj)){
      throwNotFoudEx = true;
    }

    Preconditions.checkNotNull(path, "path must not be null");
    final AsyncCallInfo<Delete> aci = new AsyncCallInfo<Delete>("deleteEndpoint", Delete.class, paramUtility);
    aci.getClient().deleteAsync(path, throwNotFoudEx);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }

}