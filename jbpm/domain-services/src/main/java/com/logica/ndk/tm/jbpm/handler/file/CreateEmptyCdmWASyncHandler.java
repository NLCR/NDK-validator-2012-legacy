package com.logica.ndk.tm.jbpm.handler.file;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.file.CreateEmptyCdmWA;

/**
 * @author Rudolf Daco
 *
 */
public class CreateEmptyCdmWASyncHandler extends AbstractSyncHandler {

  @Override
  public Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    final SyncCallInfo<CreateEmptyCdmWA> sci = new SyncCallInfo<CreateEmptyCdmWA>("createEmptyCdmWAEndpoint", CreateEmptyCdmWA.class, paramUtility);
    results.put("result", sci.getClient().executeSync());
    return results;
  }
}
