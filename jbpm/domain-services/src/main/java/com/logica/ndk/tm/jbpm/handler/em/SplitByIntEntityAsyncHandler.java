package com.logica.ndk.tm.jbpm.handler.em;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.transformation.em.SplitByIntEntity;

/**
 * @author majdaf
 */
public class SplitByIntEntityAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    log.info("Async splitting by int entity handler started");
    final String cdmId = (String) workItem.getParameter("cdmId");
    final AsyncCallInfo<SplitByIntEntity> aci = new AsyncCallInfo<SplitByIntEntity>("splitByIntEntityEndpoint", SplitByIntEntity.class, paramUtility);
    aci.getClient().executeAsync(cdmId);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    log.info("Async splitting by int entity handler finished");
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("newCdmIds", response);
    return results;
  }
}
