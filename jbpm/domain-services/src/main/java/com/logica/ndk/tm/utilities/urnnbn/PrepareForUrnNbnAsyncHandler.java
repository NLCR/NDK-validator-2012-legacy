/**
 * 
 */
package com.logica.ndk.tm.utilities.urnnbn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author kovalcikm
 */
public class PrepareForUrnNbnAsyncHandler extends AbstractAsyncHandler {
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    log.info("PrepareForUrnNbnAsyncHandler started.");
    String cdmId = (String) workItem.getParameter("cdmId");
    final String registrarCode = (String) workItem.getParameter("sigla");
    final Integer pageCount = Integer.parseInt((String) workItem.getParameter("pageCount"));
    log.info("Registar code: "+registrarCode);
    final AsyncCallInfo<PrepareForUrnNbn> aci = new AsyncCallInfo<PrepareForUrnNbn>("prepareForUrnNbnEndpoint", PrepareForUrnNbn.class, paramUtility);
    aci.getClient().executeAsync(cdmId, registrarCode, pageCount);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}
