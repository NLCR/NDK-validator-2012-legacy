package com.logica.ndk.tm.utilities.file;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

public class CreateRdcCsvFileAsyncHandler extends AbstractAsyncHandler{
  
  @Override
  protected String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    String cdmId = (String) workItem.getParameter("cdmId");
    checkNotNull(cdmId, "CDM id must not be null");

    AsyncCallInfo<CreateRdcCsvFile> aci = new AsyncCallInfo<CreateRdcCsvFile>
        ("createRdcCsvFileEndpoint", CreateRdcCsvFile.class, paramUtility);
    aci.getClient().executeAsync(cdmId);
    
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }

}
