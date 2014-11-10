/**
 * 
 */
package com.logica.ndk.tm.jbpm.handler.transformation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.transformation.ConvertToTiff;


/**
 * @author kovalcikm
 *
 */
public class ConvertToTiffAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    String cdmId = (String) workItem.getParameter("cdmId");
    final String profile = (String) workItem.getParameter("profile");
    String sourceExt = (String) workItem.getParameter("sourceExt");
    final String source = resolveParam((String) workItem.getParameter("source"), workItem.getParameters());
    final String target = resolveParam((String) workItem.getParameter("target"), workItem.getParameters());
    if(cdmId == null) {
      cdmId = "";
    }
    if(sourceExt == null) {
      sourceExt = "";
    }
    final AsyncCallInfo<ConvertToTiff> aci = new AsyncCallInfo<ConvertToTiff>("convertToTiffEndpoint", ConvertToTiff.class, paramUtility);
    aci.getClient().executeAsync(cdmId, source, target, profile, sourceExt);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}