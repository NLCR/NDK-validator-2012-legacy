package com.logica.ndk.tm.jbpm.handler.ping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.jbpm.handler.StrPlaceholder;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.ping.Ping;

/**
 * @author rudi
 */
public class PingSyncHandler extends AbstractSyncHandler {

  @Override
  public Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    final String error = resolveParam((String) workItem.getParameter("error"), workItem.getParameters());
    Preconditions.checkNotNull(error, "error must not be null");
    // test start
    log.info("Igore this. This is only output from test of StrPlaceholder: Start");
    log.info("error param after StrPlaceholder: " + error);
    final String test_param_from_tm_config = StrPlaceholder.resolveParam((String) workItem.getParameter("test_param_from_tm_config"), workItem.getParameters());
    log.info("test_param_from_tm_config after StrPlaceholder: " + test_param_from_tm_config);
    final String test_param_from_CDM = StrPlaceholder.resolveParam((String) workItem.getParameter("test_param_from_CDM"), workItem.getParameters());
    log.info("test_param_from_CDM after StrPlaceholder: " + test_param_from_CDM);
    log.info("Igore this. This is only output from test of StrPlaceholder: End");
    // test end
    final SyncCallInfo<Ping> sci = new SyncCallInfo<Ping>("pingEndpoint", Ping.class, paramUtility);
    results.put("result", sci.getClient().executeSync(error));
    return results;
  }

}
