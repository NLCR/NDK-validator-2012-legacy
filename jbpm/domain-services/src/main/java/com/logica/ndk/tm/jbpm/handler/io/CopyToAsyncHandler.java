package com.logica.ndk.tm.jbpm.handler.io;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.io.CopyTo;

/**
 * @author majdaf
 */
public class CopyToAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String source = resolveParam((String) workItem.getParameter("source"), workItem.getParameters());
    final String target = resolveParam((String) workItem.getParameter("target"), workItem.getParameters());
    final String wildcard = resolveParam((String) workItem.getParameter("wildcard"), workItem.getParameters());
    
    log.info("Source token: {}",workItem.getParameter("source"));
    log.info("Target token: {}",workItem.getParameter("target"));
    
    log.info("Source token resolved to : {}",source);
    log.info("Target token resolved to : {}",target);
    
    log.info("cdmID: {}",workItem.getParameter("cdmId"));
    
    Preconditions.checkNotNull(source, "source must not be null");
    Preconditions.checkNotNull(target, "target must not be null");
    
    final AsyncCallInfo<CopyTo> aci = new AsyncCallInfo<CopyTo>("copyToEndpoint", CopyTo.class, paramUtility);
    aci.getClient().copyAsync(source, target, wildcard);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }

}
