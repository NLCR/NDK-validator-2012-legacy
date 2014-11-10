/**
 * 
 */
package com.logica.ndk.tm.jbpm.handler.file;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.validation.ValidatePPOutput;


/**
 * @author kovalcikm
 */
public class ValidatePPOutputAsyncHandler extends AbstractAsyncHandler{
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String cdmId = (String) workItem.getParameter("cdmId");
    Boolean throwException = Boolean.parseBoolean((String) workItem.getParameter("throwException"));
    if ((throwException == null)) {
      throwException = false;
    }

    final AsyncCallInfo<ValidatePPOutput> aci = new AsyncCallInfo<ValidatePPOutput>("validatePPOutputEndpoint", ValidatePPOutput.class, paramUtility);
    aci.getClient().executeAsync(cdmId, throwException);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}
