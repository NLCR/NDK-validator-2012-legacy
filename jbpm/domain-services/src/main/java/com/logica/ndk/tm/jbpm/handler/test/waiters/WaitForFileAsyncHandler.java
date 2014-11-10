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
import com.logica.ndk.tm.utilities.test.waiters.WaitForFile;

/**
 * @author brizat
 *
 */
public class WaitForFileAsyncHandler extends AbstractAsyncHandler {

 
  @Override
  protected String executeAsyncWorkItem(final WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    final String dir = resolveParam((String) workItem.getParameter("path"), workItem.getParameters());
    final Long processIntanceId = workItem.getProcessInstanceId();

    final AsyncCallInfo<WaitForFile> aci = new AsyncCallInfo<WaitForFile>("waitForFileEndpoint", WaitForFile.class, paramUtility);
    aci.getClient().executeAsync(dir, processIntanceId);

    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }

}
