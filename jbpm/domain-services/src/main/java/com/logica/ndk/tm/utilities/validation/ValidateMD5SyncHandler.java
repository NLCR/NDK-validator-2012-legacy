/**
 * 
 */
package com.logica.ndk.tm.utilities.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author kovalcikm
 *
 */
public class ValidateMD5SyncHandler extends AbstractSyncHandler{
  
  @Override
  protected Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    String cdmId = (String) workItem.getParameter("cdmId");
    Boolean throwException = Boolean.parseBoolean((String)workItem.getParameter("throwException"));
    
    if ((throwException == null)) {
      throwException = false;
    }

    final SyncCallInfo<ValidateMD5> sci = new SyncCallInfo<ValidateMD5>("validateMD5Endpoint", ValidateMD5.class, paramUtility);
    results.put("result", sci.getClient().executeSync(cdmId, throwException));
    log.info("executeSyncWorkItem ValdiateMD5 finished");
    return results;
  }
}
