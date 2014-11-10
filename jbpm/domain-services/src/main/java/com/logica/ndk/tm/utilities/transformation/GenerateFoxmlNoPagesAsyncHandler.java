/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author kovalcikm
 *
 */
public class GenerateFoxmlNoPagesAsyncHandler extends AbstractAsyncHandler {

  @Override
  protected String executeAsyncWorkItem(final WorkItem workItem, final List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    final String cdmId = (String) workItem.getParameter("cdmId");
    final String locality = (String) workItem.getParameter("locality");

    final AsyncCallInfo<GenerateFoxmlNoPages> aci = new AsyncCallInfo<GenerateFoxmlNoPages>("generateFoxmlNoPagesEndpoint", GenerateFoxmlNoPages.class, paramUtility);
    aci.getClient().executeAsync(cdmId, locality);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(final Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }

}
