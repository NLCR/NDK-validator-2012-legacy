/**
 * 
 */
package com.logica.ndk.tm.jbpm.handler.em;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.transformation.em.SplitByIntEntity;

/**
 * @author londrusek
 */
public class SplitByIntEntitySyncHandler extends AbstractSyncHandler {

  @Override
  protected Map<String, Object> executeSyncWorkItem(final WorkItem workItem, final List<ParamUtility> paramUtility) throws Exception {

    log.info("executeSyncWorkItem started");

    final Map<String, Object> results = new HashMap<String, Object>();
    final String cdmId = (String) workItem.getParameter("cdmId");
    final SyncCallInfo<SplitByIntEntity> sci = new SyncCallInfo<SplitByIntEntity>("splitByIntEntityEndpoint", SplitByIntEntity.class, paramUtility);
    results.put("newCdmIds", sci.getClient().executeSync(cdmId));

    log.info("executeSyncWorkItem finished");
    return results;
  }

}
