package com.logica.ndk.tm.jbpm.handler.io;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.io.Delete;

/**
 * @author ondrusekl
 */
public class DeleteSyncHandler extends AbstractSyncHandler {

  @Override
  protected Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {

    log.trace("executeNDKWorkItem started");

    final Map<String, Object> results = new HashMap<String, Object>();
    final String path = (String) workItem.getParameter("path");
    String throwNotFoudExObj = (String)workItem.getParameter("throwNotFoudEx");
    boolean notThrowNotFoudEx = false;
    if(Boolean.parseBoolean(throwNotFoudExObj)){
      notThrowNotFoudEx = true;
    }
    checkNotNull(path, "path must not be null");
    final SyncCallInfo<Delete> sci = new SyncCallInfo<Delete>("deleteEndpoint", Delete.class, paramUtility);
    results.put("result", sci.getClient().deleteSync(path, notThrowNotFoudEx));

    log.trace("executeNDKWorkItem finished");
    return results;
  }

}
