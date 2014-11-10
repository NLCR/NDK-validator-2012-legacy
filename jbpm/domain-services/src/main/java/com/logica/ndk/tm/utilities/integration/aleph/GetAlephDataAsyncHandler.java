package com.logica.ndk.tm.utilities.integration.aleph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author rudi
 */
public class GetAlephDataAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String barCode = (String) workItem.getParameter("barCode");
    final String libraryId = (String) workItem.getParameter("libraryId");
    final String localBase = (String) workItem.getParameter("localBase");
    final AsyncCallInfo<GetAlephData> aci = new AsyncCallInfo<GetAlephData>("getAlephDataEndpoint", GetAlephData.class, paramUtility);
    aci.getClient().getBibliographicDataByBarCodeAsync(barCode, libraryId, localBase);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }

}
