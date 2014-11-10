package com.logica.ndk.tm.jbpm.handler.io;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.io.FailIfDirExists;

/**
 * 
 */

/**
 * @author kovalcikm
 */
public class FailIfDirExistsAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {

    String path = resolveParam((String) workItem.getParameter("path"), workItem.getParameters());
    Preconditions.checkNotNull(path, "path must not be null");

    final AsyncCallInfo<FailIfDirExists> aci = new AsyncCallInfo<FailIfDirExists>("failIfDirExistsEndpoint", FailIfDirExists.class, paramUtility);
    aci.getClient().executeAsync(path);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }

}
