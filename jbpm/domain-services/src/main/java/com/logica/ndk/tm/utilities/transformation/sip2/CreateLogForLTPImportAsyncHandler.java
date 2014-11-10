/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.sip2;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author mkorvas
 */
public class CreateLogForLTPImportAsyncHandler extends AbstractAsyncHandler {

  @Override
  protected String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    log.info("executeAsyncWorkItem started");

    final String cdmId = (String) workItem.getParameter("cdmId");
    checkNotNull(cdmId, "cdmId must not be null");
    log.debug("cdmId: " + cdmId);
    
    final String locality = (String) workItem.getParameter("locality");
    checkNotNull(locality, "locality must not be null");
    log.debug("locality: " + locality);
    
    final Integer taskId = Integer.parseInt((String) workItem.getParameter("taskId"));
    checkNotNull(taskId, "taskId must not be null");
    log.debug("taskId: " + taskId);
    
    final AsyncCallInfo<CreateLogForLTPImport> aci = new AsyncCallInfo<CreateLogForLTPImport>("createLogForLTPImportEndpoint", CreateLogForLTPImport.class, paramUtility);
    aci.getClient().executeAsync(cdmId, locality.toLowerCase(), taskId);

    log.info("executeAsyncWorkItem finished");
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {

    log.info("processResponse started");

    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);

    log.info("processResponse finished");
    return results;
  }

}
