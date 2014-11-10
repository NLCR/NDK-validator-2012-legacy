package com.logica.ndk.tm.utilities.file;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author brizat
 *
 */
public class RemoveCDMByIdSyncHandler extends AbstractSyncHandler {

  @Override
  protected Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    String cdmId = (String) workItem.getParameter("id");
    checkNotNull(cdmId, "CDM id must not be null");

    SyncCallInfo<RemoveCDMById> sci = new SyncCallInfo<RemoveCDMById>
        ("removeCDMById", RemoveCDMById.class, paramUtility);

    Map<String, Object> result = new HashMap<String, Object>();
    result.put("result", sci.getClient().executeSync(cdmId));
        
    return result;
  }

}
