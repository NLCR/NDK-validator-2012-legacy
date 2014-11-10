/**
 * 
 */
package com.logica.ndk.tm.jbpm.handler.em;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.validation.ValidateCdmMetadata;

/**
 * @author londrusek
 */
public class ValidateCdmMetadataSyncHandler extends AbstractSyncHandler {

  @Override
  protected Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) {
    checkNotNull(workItem, "workItem must not be null");

    log.info("executeSyncWorkItem started");

    final Map<String, Object> results = new HashMap<String, Object>();
    final String cdmId = (String) workItem.getParameter("cdmId");
    Preconditions.checkNotNull(cdmId, "cdmId must not be null");
    Boolean throwException = Boolean.parseBoolean((String)workItem.getParameter("throwException"));

    if ((throwException == null)) {
      throwException = false;
    }

    final SyncCallInfo<ValidateCdmMetadata> sci = new SyncCallInfo<ValidateCdmMetadata>("validateCdmMetadataEndpoint", ValidateCdmMetadata.class, paramUtility);
    results.put("result", sci.getClient().validateSync(cdmId, throwException));

    log.info("executeSyncWorkItem finished");
    return results;
  }

}
