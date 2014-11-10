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
import com.logica.ndk.tm.utilities.io.CleanData;
import com.logica.ndk.tm.utilities.io.DeleteDir;

/**
 * @author kovalcikm
 */
public class CleanDataAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {

    String cdmId = (String) workItem.getParameter("cdmId");
    String isEntityString = (String) workItem.getParameter("isEntity");

    boolean isEntity = false;
    if (isEntityString != null && Boolean.parseBoolean(isEntityString)) {
      isEntity = true;
    }

    String throwNotFoudExObj = (String) workItem.getParameter("notThrowNotFoudEx");
    log.info("Parameter throwNotFoudEx: " + throwNotFoudExObj);

    boolean throwNotFoudEx = false;
    if (throwNotFoudExObj != null && Boolean.parseBoolean(throwNotFoudExObj)) {
      throwNotFoudEx = true;
    }

    Preconditions.checkNotNull(cdmId, "cdmId must not be null");

    final AsyncCallInfo<CleanData> aci = new AsyncCallInfo<CleanData>("cleanDataEndpoint", CleanData.class, paramUtility);
    aci.getClient().executeAsync(cdmId, isEntity, throwNotFoudEx);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }

}
