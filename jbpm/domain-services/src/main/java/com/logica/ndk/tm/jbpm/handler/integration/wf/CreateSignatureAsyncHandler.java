/**
 * 
 */
package com.logica.ndk.tm.jbpm.handler.integration.wf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.integration.wf.CreateSignature;

/**
 * @author kovalcikm
 *
 */
public class CreateSignatureAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final Long packageId = Long.valueOf((String)workItem.getParameter("packageId"));
    final String signatureType = (String)workItem.getParameter("signatureType");
    
    Preconditions.checkNotNull(packageId, "packageId must not be null");
    Preconditions.checkNotNull(signatureType, "signatureType must not be null");
    
    final AsyncCallInfo<CreateSignature> aci = new AsyncCallInfo<CreateSignature>("createSignatureEndpoint", CreateSignature.class, paramUtility);
    aci.getClient().executeAsync(packageId, signatureType);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}