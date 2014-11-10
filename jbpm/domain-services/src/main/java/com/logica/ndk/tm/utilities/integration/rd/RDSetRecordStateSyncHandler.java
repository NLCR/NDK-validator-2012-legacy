package com.logica.ndk.tm.utilities.integration.rd;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

public class RDSetRecordStateSyncHandler extends AbstractSyncHandler{

	@Override
	protected Map<String, Object> executeSyncWorkItem(WorkItem workItem,
			List<ParamUtility> paramUtility) throws Exception {
		checkNotNull(workItem, "workItem must not be null");

		log.info("executeSyncWorkItem started");

	    final Map<String, Object> results = new HashMap<String, Object>();

//	    final String cdmId = (String) workItem.getParameter("cdmId");
//	    checkNotNull(cdmId, "cdmId must not be null");
//	    log.debug("cdmId: " + cdmId);
	   	  
	    
      Integer recordId;
      if (workItem.getParameter("recordId") != null) {
        recordId = Integer.valueOf((String)workItem.getParameter("recordId"));
      } else {
        recordId = 0;
      }
	    String newState = (String) workItem.getParameter("newState");
	    String oldState = (String) workItem.getParameter("oldState");
	    String user = (String) workItem.getParameter("user");	    	    
	    
	    final SyncCallInfo<RDSetRecordState> sci = new SyncCallInfo<RDSetRecordState>("rdSetRecordStateEndpoint", RDSetRecordState.class, paramUtility);
	    boolean result = sci.getClient().setRecordStateSync(recordId, newState, oldState, user, new Date());
	    results.put("result", result);

	    log.info("executeSyncWorkItem finished");
	    return results;	    
	}


}
