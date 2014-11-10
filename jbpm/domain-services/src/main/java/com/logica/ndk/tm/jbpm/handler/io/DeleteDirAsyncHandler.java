/**
 * 
 */
package com.logica.ndk.tm.jbpm.handler.io;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.io.Delete;
import com.logica.ndk.tm.utilities.io.DeleteDir;

/**
 * @author kovalcikm
 */
public class DeleteDirAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {

    String cdmId = (String) workItem.getParameter("cdmId");
    String dirName = (String) workItem.getParameter("dirName");
    String throwNotFoudExObj = (String) workItem.getParameter("notThrowNotFoudEx");
    log.info("Parameter throwNotFoudEx: " + throwNotFoudExObj);

    boolean throwNotFoudEx = false;
    if (Boolean.parseBoolean(throwNotFoudExObj)) {
      throwNotFoudEx = true;
    }

    Preconditions.checkNotNull(cdmId, "cdmId must not be null");
    Preconditions.checkNotNull(dirName, "dirName must not be null");

    final AsyncCallInfo<DeleteDir> aci = new AsyncCallInfo<DeleteDir>("deleteDirEndpoint", DeleteDir.class, paramUtility);
    aci.getClient().executeAsync(cdmId, dirName, throwNotFoudEx);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }

}
