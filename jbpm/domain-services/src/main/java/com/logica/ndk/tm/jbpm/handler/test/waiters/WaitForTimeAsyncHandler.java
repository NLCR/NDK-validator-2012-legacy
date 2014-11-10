/**
 * 
 */
package com.logica.ndk.tm.jbpm.handler.test.waiters;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.test.waiters.WaitForTime;

/**
 * @author brizat
 *
 */
public class WaitForTimeAsyncHandler extends AbstractAsyncHandler {

 
  @Override
  protected String executeAsyncWorkItem(final WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");
    final String timeString = (String) workItem.getParameter("time");
    log.info("Waiting time: " + timeString);
    
    final Long time = Long.parseLong(timeString);

    final AsyncCallInfo<WaitForTime> aci = new AsyncCallInfo<WaitForTime>("waitForTimeEndpoint", WaitForTime.class, paramUtility);
    aci.getClient().executeAsync(time);

    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    //results.put("result", response);
    return results;
  }

}
