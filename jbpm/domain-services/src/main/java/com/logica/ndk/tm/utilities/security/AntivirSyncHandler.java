/**
 * 
 */
package com.logica.ndk.tm.utilities.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author kovalcikm
 */
public class AntivirSyncHandler extends AbstractSyncHandler {

  @Override
  protected Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    //String dir = (String) workItem.getParameter("dir");
    final String dir = resolveParam((String) workItem.getParameter("dir"), workItem.getParameters());
    log.info("Parameter dir resolved to : {}",dir);
    
    final SyncCallInfo<Antivir> sci = new SyncCallInfo<Antivir>("antivirEndpoint", Antivir.class, paramUtility);
    results.put("result", sci.getClient().executeSync(dir));
    return results;
  }

}
