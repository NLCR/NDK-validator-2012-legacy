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
 */
public class ValidateCdmBasicAsyncHandler extends AbstractAsyncHandler {
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String cdmId = (String) workItem.getParameter("cdmId");
    Boolean throwException = Boolean.parseBoolean((String) workItem.getParameter("throwException"));
    if ((throwException == null)) {
      throwException = false;
    }

    final AsyncCallInfo<ValidateCdmBasic> aci = new AsyncCallInfo<ValidateCdmBasic>("validateCdmBasicEndpoint", ValidateCdmBasic.class, paramUtility);
    aci.getClient().validateAsync(cdmId, throwException);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}
