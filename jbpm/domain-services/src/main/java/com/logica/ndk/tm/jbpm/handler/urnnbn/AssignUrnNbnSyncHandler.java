package com.logica.ndk.tm.jbpm.handler.urnnbn;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.urnnbn.AssignUrnNbn;
import com.logica.ndk.tm.utilities.urnnbn.Import;

/**
 * @author ondrusekl
 */
public class AssignUrnNbnSyncHandler extends AbstractSyncHandler {

  @Override
  protected Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    checkNotNull(workItem, "workItem must not be null");

    final String cdmId = (String) workItem.getParameter("cdmId");
    final String registrarCode = (String) workItem.getParameter("sigla");

    final SyncCallInfo<AssignUrnNbn> sci = new SyncCallInfo<AssignUrnNbn>("assignUrnNbnEndpoint", AssignUrnNbn.class, paramUtility);
    results.put("result", sci.getClient().assignSync(registrarCode, cdmId));
    
    return results;
  }

}
