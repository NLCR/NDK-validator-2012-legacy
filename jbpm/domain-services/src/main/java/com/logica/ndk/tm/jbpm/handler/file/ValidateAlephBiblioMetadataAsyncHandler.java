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
import com.logica.ndk.tm.utilities.ProcessParams;
import com.logica.ndk.tm.utilities.validation.ValidateAlephBiblioMetadata;
import com.logica.ndk.tm.utilities.validation.ValidationViolationsWrapper;

/**
 * @author kovalcikm
 */
public class ValidateAlephBiblioMetadataAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String cdmId = (String) workItem.getParameter("cdmId");
    Boolean throwException = Boolean.parseBoolean((String) workItem.getParameter("throwException"));
    if ((throwException == null)) {
      throwException = false;
    }

    final AsyncCallInfo<ValidateAlephBiblioMetadata> aci = new AsyncCallInfo<ValidateAlephBiblioMetadata>("validateAlephBiblioMetadataOutputEndpoint", ValidateAlephBiblioMetadata.class, paramUtility);
    aci.getClient().executeAsync(cdmId, throwException);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();

    ValidationViolationsWrapper validationsWrapper = (ValidationViolationsWrapper) response;
    if (validationsWrapper.getViolationsList().size() != 0) {
      results.put(ProcessParams.PARAM_NAME_ERROR, true);
    }
    else {
      results.put(ProcessParams.PARAM_NAME_ERROR, false);
    }

    results.put("result", response);
    return results;
  }
}
