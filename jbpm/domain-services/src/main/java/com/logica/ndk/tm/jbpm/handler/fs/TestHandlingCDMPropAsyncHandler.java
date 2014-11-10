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
import com.logica.ndk.tm.utilities.fs.FSOperationsGenerator;
import com.logica.ndk.tm.utilities.fs.TestHandlingCDMProp;

/**
 * @author kovalcikm
 *
 */
public class TestHandlingCDMPropAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String processDir = resolveParam((String) workItem.getParameter("processDir"), workItem.getParameters());
    final String sourceFolderName = resolveParam((String) workItem.getParameter("sourceFolderName"), workItem.getParameters());
           
    Preconditions.checkNotNull(processDir, "source must not be null");
    Preconditions.checkNotNull(sourceFolderName, "target must not be null");
    
    final AsyncCallInfo<TestHandlingCDMProp> aci = new AsyncCallInfo<TestHandlingCDMProp>("testHandlingCDMPropEndpoint", TestHandlingCDMProp.class, paramUtility);
    aci.getClient().executeAsync(processDir, sourceFolderName);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}