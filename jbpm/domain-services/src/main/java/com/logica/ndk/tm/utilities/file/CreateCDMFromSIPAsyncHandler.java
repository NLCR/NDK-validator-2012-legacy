package com.logica.ndk.tm.utilities.file;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

public class CreateCDMFromSIPAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    String path = (String) workItem.getParameter("path");
    String processType = (String) workItem.getParameter("processType");
    if (processType == null) {
      processType = "";
    }

    final AsyncCallInfo<CreateCDMFromSIP> aci = new AsyncCallInfo<CreateCDMFromSIP>("createCDMFromSIPEndpoint", CreateCDMFromSIP.class, paramUtility);
    aci.getClient().executeAsync(path, processType);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }

}
