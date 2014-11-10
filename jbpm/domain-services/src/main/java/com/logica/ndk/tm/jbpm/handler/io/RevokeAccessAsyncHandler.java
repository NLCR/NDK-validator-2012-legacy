package com.logica.ndk.tm.jbpm.handler.io;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.io.RevokeAccess;

/**
 * @author ondrusekl
 */
public class RevokeAccessAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(final WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    log.info("executeAsyncWorkItem started");

    final String cdmId = (String) workItem.getParameter("cdmId");
    checkNotNull(cdmId, "cdmId must not be null");

    final AsyncCallInfo<RevokeAccess> aci = new AsyncCallInfo<RevokeAccess>("revokeAccessEndpoint", RevokeAccess.class, paramUtility);
    aci.getClient().revokeAsync(cdmId);

    log.info("executeAsyncWorkItem finished");
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(final Object response) throws Exception {

    log.info("processResponse started");

    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);

    log.info("processResponse finished");
    return results;
  }

}
