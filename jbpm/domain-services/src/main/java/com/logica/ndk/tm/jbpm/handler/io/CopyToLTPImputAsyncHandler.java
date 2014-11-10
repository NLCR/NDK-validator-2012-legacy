
package com.logica.ndk.tm.jbpm.handler.io;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.io.CopyToLTPImput;

/**
 * @author ondrusekl
 */
public class CopyToLTPImputAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String uuid = resolveParam((String) workItem.getParameter("uuid"), workItem.getParameters());
    
    final AsyncCallInfo<CopyToLTPImput> aci = new AsyncCallInfo<CopyToLTPImput>("copyToLTPImputEndpoint", CopyToLTPImput.class, paramUtility);
    aci.getClient().executeAsync(uuid);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }

}
