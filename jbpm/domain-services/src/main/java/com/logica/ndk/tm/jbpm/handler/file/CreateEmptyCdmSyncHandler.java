package com.logica.ndk.tm.jbpm.handler.file;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.file.CreateEmptyCdm;

/**
 * @author rudi
 */
public class CreateEmptyCdmSyncHandler extends AbstractSyncHandler {

  @Override
  public Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    final String barCode = (String) workItem.getParameter("barCode");
    final String taskId = (String) workItem.getParameter("taskId");
    Preconditions.checkNotNull(barCode, "barCode must not be null");
    final SyncCallInfo<CreateEmptyCdm> sci = new SyncCallInfo<CreateEmptyCdm>("createEmptyCdmEndpoint", CreateEmptyCdm.class, paramUtility);
    results.put("result", sci.getClient().executeSync(barCode, taskId));
    return results;
  }
}
