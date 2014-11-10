package com.logica.ndk.tm.utilities.file;

import static com.google.common.base.Preconditions.checkNotNull;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.drools.runtime.process.WorkItem;

/**
 * 
 * @author brizat
 */
public class RemoveCDMByIdAsyncHandler extends AbstractAsyncHandler {

  @Override
  protected String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    String cdmId = (String) workItem.getParameter("id");
    checkNotNull(cdmId, "CDM id must not be null");

    AsyncCallInfo<RemoveCDMById> aci = new AsyncCallInfo<RemoveCDMById>
        ("removeCDMByIdEndpoint", RemoveCDMById.class, paramUtility);
    aci.getClient().executeAsync(cdmId);
    
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }

}
