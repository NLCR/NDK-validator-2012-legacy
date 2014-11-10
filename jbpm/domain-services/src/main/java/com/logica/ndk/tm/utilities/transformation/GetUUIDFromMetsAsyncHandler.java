/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author kovalcikm
 *
 */
public class GetUUIDFromMetsAsyncHandler extends AbstractAsyncHandler{
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String metsFilePath = (String) workItem.getParameter("metsFilePath");
    Preconditions.checkNotNull(metsFilePath, "metsFilePath must not be null");

    final AsyncCallInfo<GetUUIDFromMets> aci = new AsyncCallInfo<GetUUIDFromMets>("getUUIDFromMetsEndpoint", GetUUIDFromMets.class, paramUtility);
    aci.getClient().executeAsync(metsFilePath);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}