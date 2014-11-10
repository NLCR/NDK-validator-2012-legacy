/**
 * 
 */
package com.logica.ndk.tm.utilities.integration.aleph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author kovalcikm
 *
 */
public class SaveAlephMetadataAsyncHandler extends AbstractAsyncHandler{
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String cdmId = (String) workItem.getParameter("cdmId");
    Preconditions.checkNotNull(cdmId, "cdmId must not be null");
    final String alephMetadata = (String) workItem.getParameter("alephMetadata");
    Preconditions.checkNotNull(alephMetadata, "alephMetadata must not be null");

    final AsyncCallInfo<SaveAlephMetadata> aci = new AsyncCallInfo<SaveAlephMetadata>("saveAlephMetadataEndpoint", SaveAlephMetadata.class, paramUtility);
    aci.getClient().executeAsync(cdmId, alephMetadata);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}
