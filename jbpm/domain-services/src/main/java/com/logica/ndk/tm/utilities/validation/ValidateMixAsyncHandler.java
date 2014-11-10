/**
 * 
 */
package com.logica.ndk.tm.utilities.validation;

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
public class ValidateMixAsyncHandler extends AbstractAsyncHandler {
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    log.info("ValidateMixAsyncHandler started.");
    final String cdmId = (String) workItem.getParameter("cdmId");
    final String dirname = (String) workItem.getParameter("dirName");
    final String profile = (String) workItem.getParameter("profile");
    Boolean throwException = Boolean.parseBoolean((String) workItem.getParameter("throwException"));
    if ((throwException == null)) {
      throwException = false;
    }

    final AsyncCallInfo<ValidateMix> aci = new AsyncCallInfo<ValidateMix>("validateMixEndpoint", ValidateMix.class, paramUtility);
    aci.getClient().executeAsync(cdmId, dirname, profile, throwException);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}
