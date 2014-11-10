package com.logica.ndk.tm.jbpm.handler.transformation.mns;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.transformation.mns.MnsToMods;

public class MnsToModsSyncHandler extends AbstractSyncHandler {

  @Override
  public Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) {
    checkNotNull(workItem, "workItem must not be null");

    log.info("executeSyncWorkItem started");

    final Map<String, Object> results = new HashMap<String, Object>();

    final String inFilePath = (String) workItem.getParameter("inFilePath");
    checkNotNull(inFilePath, "inFilePath must not be null");
    log.debug("cdmId: " + inFilePath);

    final String outFilePath = (String) workItem.getParameter("outFilePath");
    checkNotNull(outFilePath, "outFilePath must not be null");
    log.debug("cdmId: " + outFilePath);

    final SyncCallInfo<MnsToMods> sci = new SyncCallInfo<MnsToMods>("MnsToModsEndpoint", MnsToMods.class, paramUtility);
    results.put("result", sci.getClient().executeSync(inFilePath, outFilePath));

    log.info("executeSyncWorkItem finished");
    return results;
  }

}
