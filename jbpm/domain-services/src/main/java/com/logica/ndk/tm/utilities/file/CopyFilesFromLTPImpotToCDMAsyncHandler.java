/**
 * 
 */
package com.logica.ndk.tm.utilities.file;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.ProcessParams;

/**
 * @author brizat
 *
 */
public class CopyFilesFromLTPImpotToCDMAsyncHandler extends AbstractAsyncHandler{
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {    
    String cdmId = (String) workItem.getParameter("cdmId");
    String sourcePath = (String) workItem.getParameter("sourcePath");

    final AsyncCallInfo<CopyFilesFromLTPImpotToCDM> aci = new AsyncCallInfo<CopyFilesFromLTPImpotToCDM>("copyFilesFromLTPImpotToCDMEndPoint", CopyFilesFromLTPImpotToCDM.class, paramUtility);
    aci.getClient().executeAsync(cdmId, sourcePath);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put(ProcessParams.PARAM_NAME_SCAN_COUNT, response);
    results.put(ProcessParams.PARAM_NAME_PAGE_COUNT, response);
    return results;
  }
}