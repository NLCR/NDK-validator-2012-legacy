package com.logica.ndk.tm.jbpm.handler.io;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.io.GrantAccess;

/**
 * @author ondrusekl
 */
public class GrantAccessAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    log.info("executeAsyncWorkItem started");

    final String user = (String) workItem.getParameter("user");
    final String cdmId = (String) workItem.getParameter("cdmId");
    checkNotNull(user, "user must not be null");
    checkNotNull(cdmId, "cdmId must not be null");

    final AsyncCallInfo<GrantAccess> aci = new AsyncCallInfo<GrantAccess>("grantAccessEndpoint", GrantAccess.class, paramUtility);
    aci.getClient().grantAsync(user, new CDM().getCdmDir(cdmId).getAbsolutePath());

    log.info("executeAsyncWorkItem finished");
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }

}
