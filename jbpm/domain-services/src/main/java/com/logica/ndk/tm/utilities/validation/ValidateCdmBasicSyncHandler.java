package com.logica.ndk.tm.utilities.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author rudi
 */
public class ValidateCdmBasicSyncHandler extends AbstractSyncHandler {

  @Override
  public Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    final String cdmId = (String) workItem.getParameter("cdmId");
    Preconditions.checkNotNull(cdmId, "cdmId must not be null");
    log.info("Parameter throwException retrieved as: "+workItem.getParameter("throwException"));
    Boolean throwException = Boolean.parseBoolean((String)workItem.getParameter("throwException"));

    if ((throwException == null)) {
      throwException = false;
    }
    final SyncCallInfo<ValidateCdmBasic> sci = new SyncCallInfo<ValidateCdmBasic>("validateCdmBasicEndpoint", ValidateCdmBasic.class, paramUtility);
    results.put("result", sci.getClient().validateSync(cdmId, throwException));
    return results;
  }

}
