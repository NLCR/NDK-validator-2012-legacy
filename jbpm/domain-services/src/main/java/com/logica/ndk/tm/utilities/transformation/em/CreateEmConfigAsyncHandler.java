package com.logica.ndk.tm.utilities.transformation.em;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author londrusek
 */
public class CreateEmConfigAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    log.info("executeAsyncWorkItem started");

    final String cdmId = (String) workItem.getParameter("cdmId");
    checkNotNull(cdmId, "cdmId must not be null");
    
    final String ocr;  
    final Object ocrWorkItem = workItem.getParameter("ocr");  
    if (ocrWorkItem != null) {
        ocr = (String) ocrWorkItem;
    } else {
        ocr = "";
    }

    final AsyncCallInfo<CreateEmConfig> aci = new AsyncCallInfo<CreateEmConfig>("createEmConfigEndpoint", CreateEmConfig.class, paramUtility);
    aci.getClient().createAsync(cdmId, ocr);

    log.info("executeAsyncWorkItem finished");
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {

    log.info("processResponse started");

    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);

    log.info("processResponse finished");
    return results;
  }

}
