package com.logica.ndk.tm.jbpm.handler.ping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.ping.Ping;

/**
 * @author rudi
 */
public class PingAsyncHandler extends AbstractAsyncHandler {
  @Override
  protected String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String error = (String) workItem.getParameter("error");
    Preconditions.checkNotNull(error, "error must not be null");
    final AsyncCallInfo<Ping> aci = new AsyncCallInfo<Ping>("pingEndpoint", Ping.class, paramUtility);
    aci.getClient().executeAsync(error);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("result", response);
    return result;
  }
}
