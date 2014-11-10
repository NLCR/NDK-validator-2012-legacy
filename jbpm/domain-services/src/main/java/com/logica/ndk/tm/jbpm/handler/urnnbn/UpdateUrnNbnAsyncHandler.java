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
import com.logica.ndk.tm.utilities.urnnbn.UpdateUrnNbn;

/**
 * @author ondrusekl
 */
public class UpdateUrnNbnAsyncHandler extends AbstractAsyncHandler {

  @Override
  protected String executeAsyncWorkItem(final WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    final String cdmId = (String) workItem.getParameter("cdmId");
    final String registrarCode = (String) workItem.getParameter("sigla");

    final AsyncCallInfo<UpdateUrnNbn> aci = new AsyncCallInfo<UpdateUrnNbn>("updateUrnNbnEndpoint", UpdateUrnNbn.class, paramUtility);
    aci.getClient().assignAsync(registrarCode, cdmId);

    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(final Object response) throws Exception {

    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", "OK");

    return results;
  }

}
