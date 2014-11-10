/**
 * 
 */
package com.logica.ndk.tm.utilities.file;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author londrusek
 */
public class RsyncAsyncHandler extends AbstractAsyncHandler {

  @Override
  protected String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    log.info("executeAsyncWorkItem started");

    final String pathId = (String) workItem.getParameter("pathId");
    log.debug("PathId: " + pathId);

    // TODO should be loaded directly from params provided by WF API
    /*
    String computerName = "virtual";
    String barCode = (String) workItem.getParameter("barCode");
    String taskId = (String) workItem.getParameter("taskId");
    String scanSerialNo = "1";
    final String localURNString = computerName + ":" + barCode + ":" + taskId + ":" + scanSerialNo;
    */
    final String localURNString = (String) workItem.getParameter("localURNString");
    log.debug("Local URN String: " + localURNString);

    final AsyncCallInfo<Rsync> aci = new AsyncCallInfo<Rsync>("rsyncEndpoint", Rsync.class, paramUtility);
    aci.getClient().executeAsync(pathId, localURNString);

    log.info("executeAsyncWorkItem finished");
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {

    log.info("processResponse started");

    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);

    log.info("processResponse finished");
    return results;
  }

}
