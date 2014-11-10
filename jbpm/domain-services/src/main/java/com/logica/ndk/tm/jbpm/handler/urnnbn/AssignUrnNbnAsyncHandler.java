package com.logica.ndk.tm.jbpm.handler.urnnbn;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.AssignUrnNbnResponse;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.urnnbn.AssignUrnNbn;
import com.logica.ndk.tm.utilities.urnnbn.Import;

/**
 * @author ondrusekl
 */
public class AssignUrnNbnAsyncHandler extends AbstractAsyncHandler {

  @Override
  protected String executeAsyncWorkItem(final WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    final String cdmId = (String) workItem.getParameter("cdmId");
    final String registrarCode = (String) workItem.getParameter("sigla");

    final AsyncCallInfo<AssignUrnNbn> aci = new AsyncCallInfo<AssignUrnNbn>("assignUrnNbnEndpoint", AssignUrnNbn.class, paramUtility);
    aci.getClient().assignAsync(registrarCode, cdmId);

    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(final Object response) throws Exception {

    final AssignUrnNbnResponse urnNbnResponse = (AssignUrnNbnResponse) response;
    
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("urnNbn", urnNbnResponse.getUrnNbn());
    results.put("urnNbnSource", urnNbnResponse.getUrnNbnSource());

    return results;
  }

}
