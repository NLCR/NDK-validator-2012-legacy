package com.logica.ndk.tm.utilities.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author Rudolf Daco
 */
public class ValidateImagesForPostprocAsyncHandler extends AbstractAsyncHandler {
  @Override
  protected String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String cdmId = (String) workItem.getParameter("cdmId");
    Boolean throwException = Boolean.parseBoolean((String) workItem.getParameter("throwException"));
    Preconditions.checkNotNull(cdmId, "cdmId must not be null");
    if ((throwException == null)) {
      throwException = false;
    }
    final AsyncCallInfo<ValidateImagesForPostproc> aci = new AsyncCallInfo<ValidateImagesForPostproc>("validateImagesForPostprocEndpoint", ValidateImagesForPostproc.class, paramUtility);
    aci.getClient().validateAsync(cdmId, throwException);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("result", response);
    return result;
  }
}
