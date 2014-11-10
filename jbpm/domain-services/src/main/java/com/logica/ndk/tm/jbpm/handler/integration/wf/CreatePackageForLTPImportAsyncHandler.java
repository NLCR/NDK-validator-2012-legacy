package com.logica.ndk.tm.jbpm.handler.integration.wf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.integration.wf.CreatePackageForLTPImport;

/**
 * @author brizat
 */
public class CreatePackageForLTPImportAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String url = (String) workItem.getParameter("url");
    final Long taskId = Long.valueOf((String) workItem.getParameter("taskId"));
    final String cdmId =(String) workItem.getParameter("cdmId");
    final AsyncCallInfo<CreatePackageForLTPImport> aci = new AsyncCallInfo<CreatePackageForLTPImport>("createPackageForLTPImportEndpoint", CreatePackageForLTPImport.class, paramUtility);
    aci.getClient().executeAsync(url, taskId,cdmId);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}
