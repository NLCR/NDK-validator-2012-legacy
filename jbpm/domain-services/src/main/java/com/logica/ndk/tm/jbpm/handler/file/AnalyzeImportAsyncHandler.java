package com.logica.ndk.tm.jbpm.handler.file;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.file.AnalyzeImport;

public class AnalyzeImportAsyncHandler extends AbstractAsyncHandler {
  
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String cdmId = (String) workItem.getParameter("cdmId");
    final AsyncCallInfo<AnalyzeImport> aii = new AsyncCallInfo<AnalyzeImport>("analyzeImportEndpoint", AnalyzeImport.class, paramUtility);
    aii.getClient().executeAsync(cdmId);
    return aii.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }

}
