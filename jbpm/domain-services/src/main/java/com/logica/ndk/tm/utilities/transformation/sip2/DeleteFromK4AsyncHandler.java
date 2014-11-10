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
 * @author kovalcikm
 */
public class DeleteFromK4AsyncHandler extends AbstractAsyncHandler {
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    log.info("DeleteFromK4AsyncHandler, executeAsyncWorkItem started");

    final String cdmId = (String) workItem.getParameter("cdmId");
    final String locality = (String) workItem.getParameter("locality");
    final String deleteEmptyParents = (String) workItem.getParameter("deleteEmptyParents");
    String uuidKrameriusPath = (String) workItem.getParameter("uuidKrameriusPath");
    if (uuidKrameriusPath == null) {
      uuidKrameriusPath = "";
    }

    checkNotNull(cdmId, "cdmId must not be null");
    checkNotNull(locality, "locality must not be null");
    checkNotNull(deleteEmptyParents, "deleteEmptyParents must not be null");

    log.debug("cdmId: " + cdmId);
    final AsyncCallInfo<DeleteFromK4> aci = new AsyncCallInfo<DeleteFromK4>("deleteFromK4Endpoint", DeleteFromK4.class, paramUtility);
    aci.getClient().executeAsync(cdmId, locality, deleteEmptyParents, uuidKrameriusPath);
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
