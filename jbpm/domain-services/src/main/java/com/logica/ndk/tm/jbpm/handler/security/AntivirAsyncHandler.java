/**
 * 
 */
package com.logica.ndk.tm.jbpm.handler.security;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.security.Antivir;

/**
 * @author kovalcikm
 *
 */
public class AntivirAsyncHandler extends AbstractAsyncHandler {

 
  @Override
  protected String executeAsyncWorkItem(final WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    final String dir = resolveParam((String) workItem.getParameter("dir"), workItem.getParameters());

    final AsyncCallInfo<Antivir> aci = new AsyncCallInfo<Antivir>("antivirEndpoint", Antivir.class, paramUtility);
    aci.getClient().executeAsync(dir);

    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }

}
