package com.logica.ndk.tm.jbpm.handler.em;

import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author londrusek
 */
public class PurgeByMetsSyncHandler extends AbstractSyncHandler {

  @Override
  protected Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {

    log.info("executeSyncWorkItem started");

    log.info("executeSyncWorkItem finished");
    return null;
  }
}
