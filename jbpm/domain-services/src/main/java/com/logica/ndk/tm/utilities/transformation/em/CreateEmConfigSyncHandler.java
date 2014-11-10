package com.logica.ndk.tm.utilities.transformation.em;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author londrusek
 */
public class CreateEmConfigSyncHandler extends AbstractSyncHandler {

  @Override
  public Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) {
    checkNotNull(workItem, "workItem must not be null");

    log.info("executeSyncWorkItem started");

    final Map<String, Object> results = new HashMap<String, Object>();
    final String cdmId = (String) workItem.getParameter("cdmId");
    checkNotNull(cdmId, "cdmId must not be null");
    
    final String ocr;  
    final Object ocrWorkItem = workItem.getParameter("ocr");  
    if (ocrWorkItem != null) {
        ocr = (String) ocrWorkItem;
    } else {
        ocr = "";
    }

    final SyncCallInfo<CreateEmConfig> sci = new SyncCallInfo<CreateEmConfig>("createEmConfigEndpoint", CreateEmConfig.class, paramUtility);
    results.put("result", sci.getClient().createSync(cdmId, ocr));

    log.info("executeSyncWorkItem finished");
    return results;
  }

}
