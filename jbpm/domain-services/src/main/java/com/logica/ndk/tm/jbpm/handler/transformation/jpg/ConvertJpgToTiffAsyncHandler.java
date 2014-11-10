package com.logica.ndk.tm.jbpm.handler.transformation.jpg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.transformation.jpg.ConvertJpgToTiff;

public class ConvertJpgToTiffAsyncHandler extends AbstractAsyncHandler {

  @Override
  protected String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    String cdmId = (String) workItem.getParameter("cdmId");
    String sourceExt = (String) workItem.getParameter("sourceExt");
    final String source = resolveParam((String) workItem.getParameter("source"), workItem.getParameters());
    final String target = resolveParam((String) workItem.getParameter("target"), workItem.getParameters());
    if (cdmId == null) {
      cdmId = "";
    }
    if (sourceExt == null) {
      sourceExt = "";
    }
    final AsyncCallInfo<ConvertJpgToTiff> aci = new AsyncCallInfo<ConvertJpgToTiff>("convertJpgToTiffEndpoint", ConvertJpgToTiff.class, paramUtility);
    aci.getClient().executeAsync(cdmId, source, target, sourceExt);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("result", response);
    return result;
  }
  
}
