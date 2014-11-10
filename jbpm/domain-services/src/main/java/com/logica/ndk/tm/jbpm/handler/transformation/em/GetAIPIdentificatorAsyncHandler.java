/**
 * 
 */
package com.logica.ndk.tm.jbpm.handler.transformation.em;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.ProcessParams;
import com.logica.ndk.tm.utilities.transformation.em.GetAIPIdentificator;

/**
 * @author kovalcikm
 */
public class GetAIPIdentificatorAsyncHandler extends AbstractAsyncHandler {

  @Override
  protected String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    log.info("GetAIPIdentificatorAsyncHandler started");

    final String cdmId = (String) workItem.getParameter("cdmId");
    final AsyncCallInfo<GetAIPIdentificator> aci = new AsyncCallInfo<GetAIPIdentificator>("getAIPIdentificatorEndpoint", GetAIPIdentificator.class, paramUtility);

    aci.getClient().executeAsync(cdmId);

    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();

    String id = (String) response;
    results.put(ProcessParams.PARAM_NAME_ID_AIP, id);

    return results;
  }

}
