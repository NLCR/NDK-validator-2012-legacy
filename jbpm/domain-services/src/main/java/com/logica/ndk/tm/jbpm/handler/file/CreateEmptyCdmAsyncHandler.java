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
import com.logica.ndk.tm.utilities.file.CreateEmptyCdm;

/**
 * @author kovalcikm
 */
public class CreateEmptyCdmAsyncHandler extends AbstractAsyncHandler {
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    final String barCode = (String) workItem.getParameter("barCode");
    final String taskId = (String) workItem.getParameter("taskId");
    Preconditions.checkNotNull(barCode, "barCode must not be null");
    Preconditions.checkNotNull(taskId, "taskId must not be null");

    final AsyncCallInfo<CreateEmptyCdm> aci = new AsyncCallInfo<CreateEmptyCdm>("createEmptyCdmEndpoint", CreateEmptyCdm.class, paramUtility);
    aci.getClient().executeAsync(barCode, taskId);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}
