package com.logica.ndk.tm.utilities.integration.aleph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;

import com.logica.ndk.tm.process.util.ParamUtility;

public class CreateAlephEnrichedDataAsyncHandler extends AbstractAsyncHandler {

	@Override
	public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
	  final String barCode = (String) workItem.getParameter("barCode");
	  final String recordIdentifier = (String) workItem.getParameter("recordIdentifier");
	  final String libraryId = (String) workItem.getParameter("libraryId");
	  final String localBase = (String) workItem.getParameter("localBase");
	  final String cdmId = (String) workItem.getParameter("cdmId");
	  Boolean throwException = Boolean.parseBoolean((String) workItem.getParameter("throwException"));
	  
    Preconditions.checkNotNull(barCode, "barCode must not be null");
    Preconditions.checkNotNull(libraryId, "libraryId must not be null");
    Preconditions.checkNotNull(cdmId, "cdmId must not be null");
    Preconditions.checkNotNull(recordIdentifier, "recordIdentifier must not be null");

    if ((throwException == null)) { 
      throwException = false;
    }

	  final AsyncCallInfo<CreateAlephEnrichedData> aci = new AsyncCallInfo<CreateAlephEnrichedData>("createAlephEnrichedDataEndpoint", CreateAlephEnrichedData.class, paramUtility);
	    aci.getClient().createBibliographicEnrichedDataByBarCodeAsync(barCode, recordIdentifier, libraryId, localBase, cdmId, throwException);
	    return aci.getCorrelationId();
	  }

	  @Override
	  public Map<String, Object> processResponse(Object response) throws Exception {
	    final Map<String, Object> results = new HashMap<String, Object>();
	    results.put("result", response);
	    return results;
	  }

}
