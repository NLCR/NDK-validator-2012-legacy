package com.logica.ndk.tm.jbpm.handler.transformation.jpeg2000;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.transformation.jpeg2000.ConvertToJpeg2000;
import com.logica.ndk.tm.utilities.transformation.jpeg2000.ConvertToJpeg2000LTP;

/**
 * @author rudi
 */
public class ConvertToJpeg2000LTPAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    String cdmId = (String) workItem.getParameter("cdmId");
    final String profile = (String) workItem.getParameter("profile");
    final String source = resolveParam((String) workItem.getParameter("source"), workItem.getParameters());
    final String target = resolveParam((String) workItem.getParameter("target"), workItem.getParameters());
    if(cdmId == null) {
      cdmId = "";
    }
    
    final AsyncCallInfo<ConvertToJpeg2000LTP> aci = new AsyncCallInfo<ConvertToJpeg2000LTP>("convertToJpeg2000LTPEndpoint", ConvertToJpeg2000LTP.class, paramUtility);
    aci.getClient().executeAsync(cdmId, source, target, profile);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}
