/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.mets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.ProcessParams;

/**
 * @author brizat
 *
 */
public class GetUuidFromMetsFileAsyncHandler extends AbstractAsyncHandler{
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String cdmId = (String) workItem.getParameter("cdmId");
    Preconditions.checkNotNull(cdmId, "CdmId must not be null");

    final AsyncCallInfo<GetUuidFromMetsFile> aci = new AsyncCallInfo<GetUuidFromMetsFile>("getUuidFromMetsFileEndpoint", GetUuidFromMetsFile.class, paramUtility);
    aci.getClient().executeAsync(cdmId);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put(ProcessParams.PARAM_NAME_UUID, response);
    return results;
  }
}