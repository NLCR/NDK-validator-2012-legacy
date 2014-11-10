/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.em;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author kovalcikm
 */
public class ExtendEmCsvAsyncHandler extends AbstractAsyncHandler {
  @Override
  protected String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String cdmId = (String) workItem.getParameter("cdmId");
    final String ocr = (String) workItem.getParameter("ocr");
    final String taskId = (String) workItem.getParameter("taskId");
    Preconditions.checkNotNull(cdmId, "cdmId must not be null");
    Preconditions.checkNotNull(ocr, "ocr must not be null");
    Preconditions.checkNotNull(taskId, "taskId must not be null");

    final AsyncCallInfo<ExtendEmCsv> aci = new AsyncCallInfo<ExtendEmCsv>("extendEmCsvEndpoint", ExtendEmCsv.class, paramUtility);
    aci.getClient().executeAsync(cdmId, ocr, taskId);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("result", response);
    return result;
  }
}
