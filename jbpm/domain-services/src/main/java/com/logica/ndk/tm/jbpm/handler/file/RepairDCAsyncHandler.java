/**
 * 
 */
package com.logica.ndk.tm.jbpm.handler.file;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.transformation.RepairDC;

/**
 * @author kovalcikm
 *
 */
public class RepairDCAsyncHandler extends AbstractAsyncHandler {
  @Override
  protected String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String cdmId = (String) workItem.getParameter("cdmId");
    Preconditions.checkNotNull(cdmId, "cdmId must not be null");
    final AsyncCallInfo<RepairDC> aci = new AsyncCallInfo<RepairDC>("repairDCEndpoint", RepairDC.class, paramUtility);
    aci.getClient().executeAsync(cdmId);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("result", response);
    return result;
  }
}