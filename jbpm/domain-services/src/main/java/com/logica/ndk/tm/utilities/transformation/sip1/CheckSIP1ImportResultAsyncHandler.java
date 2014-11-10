/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.sip1;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author kovalcikm
 *
 */
public class CheckSIP1ImportResultAsyncHandler extends AbstractAsyncHandler{
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    log.info("executeAsyncWorkItem started");

    final Map<String, Object> results = new HashMap<String, Object>();
    final String cdmId = (String) workItem.getParameter("cdmId");
    checkNotNull(cdmId, "cdmId must not be null");
    log.debug("cdmId: " + cdmId);
    final AsyncCallInfo<CheckSIP1ImportResult> aci = new AsyncCallInfo<CheckSIP1ImportResult>("checkSIP1ImportResultEndpoint", CheckSIP1ImportResult.class, paramUtility);
    aci.getClient().executeAsync(cdmId);
    log.info("executeAsyncWorkItem finished");
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("status", response);
    return results;
  }
}