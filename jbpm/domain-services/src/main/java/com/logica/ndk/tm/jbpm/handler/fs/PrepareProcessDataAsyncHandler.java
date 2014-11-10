/**
 * 
 */
package com.logica.ndk.tm.jbpm.handler.fs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.fs.PrepareProcessData;

/**
 * @author kovalcikm
 */
public class PrepareProcessDataAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String dataPath = resolveParam((String) workItem.getParameter("dataPath"), workItem.getParameters());

    Preconditions.checkNotNull(dataPath, "source must not be null");

    final AsyncCallInfo<PrepareProcessData> aci = new AsyncCallInfo<PrepareProcessData>("prepareProcessDataEndpoint", PrepareProcessData.class, paramUtility);
    aci.getClient().executeAsync(dataPath);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }

}
