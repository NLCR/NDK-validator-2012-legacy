package com.logica.ndk.tm.utilities.wa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.ProcessParams;
import com.logica.ndk.tm.utilities.transformation.em.WAIdentifierWrapper;

/**
 * @author rudi
 */
public class Arc2WarcAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    //final String cdmId = (String) workItem.getParameter("cdmId");
    final String source = resolveParam((String) workItem.getParameter("source"), workItem.getParameters());
    final String target = resolveParam((String) workItem.getParameter("target"), workItem.getParameters());
    final String cdmId = resolveParam((String) workItem.getParameter("cdmId"), workItem.getParameters());
    final AsyncCallInfo<Arc2Warc> aci = new AsyncCallInfo<Arc2Warc>("arc2WarcEndpoint", Arc2Warc.class, paramUtility);
    aci.getClient().executeAsync(source, target, cdmId);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    WAIdentifierWrapper data = (WAIdentifierWrapper) response;
    results.put(ProcessParams.PARAM_NAME_TITLE_UUID, data.getTitleUuid());
    results.put(ProcessParams.PARAM_NAME_RECORD_IDENTIFIER, data.getTmHash());
    return results;
  }
}
