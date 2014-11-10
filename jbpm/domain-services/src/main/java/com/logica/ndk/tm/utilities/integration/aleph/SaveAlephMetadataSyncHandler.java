package com.logica.ndk.tm.utilities.integration.aleph;

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
public class SaveAlephMetadataSyncHandler extends AbstractSyncHandler {
  @Override
  public Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) {
    final Map<String, Object> results = new HashMap<String, Object>();
    final String cdmId = (String) workItem.getParameter("cdmId");
    Preconditions.checkNotNull(cdmId, "cdmId must not be null");
    final String alephMetadata = (String) workItem.getParameter("alephMetadata");
    Preconditions.checkNotNull(alephMetadata, "alephMetadata must not be null");
    final SyncCallInfo<SaveAlephMetadata> sci = new SyncCallInfo<SaveAlephMetadata>("saveAlephMetadataEndpoint", SaveAlephMetadata.class, paramUtility);
    results.put("result", sci.getClient().executeSync(cdmId, alephMetadata));
    return results;
  }
}
