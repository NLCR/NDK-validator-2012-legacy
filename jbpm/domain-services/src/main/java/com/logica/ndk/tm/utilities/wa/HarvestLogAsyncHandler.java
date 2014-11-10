package com.logica.ndk.tm.utilities.wa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.ProcessParams;

/**
 * @author Petr Palous
 */

public class HarvestLogAsyncHandler extends AbstractAsyncHandler {
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String cdmId = (String) workItem.getParameter("cdmId");
    final AsyncCallInfo<HarvestLog> aci = new AsyncCallInfo<HarvestLog>("harvestLogEndpoint", HarvestLog.class, paramUtility);
    aci.getClient().executeAsync(cdmId);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    String cdmId = (String) response;
    log.info("HarvestLogAsyncHandler. Setting recordIdentifier and titleUUID to " + cdmId);
    results.put(ProcessParams.PARAM_NAME_TITLE_UUID, cdmId);
    results.put(ProcessParams.PARAM_NAME_RECORD_IDENTIFIER, cdmId);
    return results;
  }
}
