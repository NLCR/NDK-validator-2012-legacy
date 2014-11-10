package com.logica.ndk.tm.utilities.integration.rd;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

public class RDSetRecordUrnNbnSyncHandler extends AbstractSyncHandler {

	@Override
	protected Map<String, Object> executeSyncWorkItem(WorkItem workItem,
			List<ParamUtility> paramUtility) throws Exception {
		checkNotNull(workItem, "workItem must not be null");

		log.info("executeSyncWorkItem started");

	    final Map<String, Object> results = new HashMap<String, Object>();
	   	  	    
      Integer recordId;
      if (workItem.getParameter("recordId") != null) {
        recordId = Integer.valueOf((String)workItem.getParameter("recordId"));
      } else {
        recordId = 0;
      }
	    
	    List<String> urns = (List<String>) workItem.getParameter("urnNbn");	    
	    
	    
	    final SyncCallInfo<RDSetRecordUrnNbn> sci = new SyncCallInfo<RDSetRecordUrnNbn>("rdSetRecordUrnNbnEndpoint", RDSetRecordUrnNbn.class, paramUtility);
	    boolean result = sci.getClient().setRecordUrnNbnSync(recordId, urns, new Date());
	    results.put("result", result);

	    log.info("executeSyncWorkItem finished");
	    return results;	    
	}
}
