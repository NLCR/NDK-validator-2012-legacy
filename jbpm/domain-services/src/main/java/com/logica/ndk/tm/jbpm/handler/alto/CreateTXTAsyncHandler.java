package com.logica.ndk.tm.jbpm.handler.alto;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.alto.CreateTXT;

/**
 * @author majdaf
 */
public class CreateTXTAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    log.info("Create TXT async handler started");

    checkNotNull(workItem, "workItem must not be null");
    final String cdmId = (String) workItem.getParameter("cdmId");
    final String abstractionDir = (String) workItem.getParameter("abstractionDir");

    Preconditions.checkNotNull(cdmId, "cdmId must not be null");
    Preconditions.checkNotNull(abstractionDir, "abstractionDir must not be null");

    final AsyncCallInfo<CreateTXT> aci = new AsyncCallInfo<CreateTXT>("createTXTEndpoint", CreateTXT.class, paramUtility);
    aci.getClient().executeAsync(cdmId, abstractionDir);
    log.info("Create TXT async handler started utility");
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    log.info("Create TXT async handler finished");
    return results;
  }
}
