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
public class RemovePrefixAsyncHandler extends AbstractAsyncHandler {
  @Override
  protected String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String dirPath = resolveParam((String) workItem.getParameter("dirPath"), workItem.getParameters());
    final String prefix = (String) workItem.getParameter("prefix");
    log.info("Parameter dirPath resolved to: "+dirPath);
    log.info("Parameter prefix resolved to: "+prefix);
    
    Preconditions.checkNotNull(dirPath, "cdmId must not be null");
    final AsyncCallInfo<RemovePrefix> aci = new AsyncCallInfo<RemovePrefix>("removePrefixEndpoint", RemovePrefix.class, paramUtility);
    aci.getClient().executeAsync(dirPath, prefix);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("result", response);
    return result;
  }
}