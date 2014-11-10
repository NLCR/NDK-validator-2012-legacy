/**
 * 
 */
package com.logica.ndk.tm.jbpm.handler.urnnbn;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.urnnbn.DeactivateUrnNbn;

/**
 * @author kovalcikm
 */
public class DeactivateurnNbnAsyncHandler extends AbstractAsyncHandler {

  @Override
  protected String executeAsyncWorkItem(final WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    final String urnnbn = (String) workItem.getParameter("urnnbn");
    final String note = (String) workItem.getParameter("note");

    final AsyncCallInfo<DeactivateUrnNbn> aci = new AsyncCallInfo<DeactivateUrnNbn>("deactivateUrnNbnEndpoint", DeactivateUrnNbn.class, paramUtility);
    aci.getClient().executeAsync(urnnbn, note);

    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(final Object response) throws Exception {

    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;

  }

}
