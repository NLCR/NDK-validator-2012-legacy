/**
 * 
 */
package com.logica.ndk.tm.utilities.file;

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
public class CreateCDMByIdSyncHandler extends AbstractSyncHandler{
  
  @Override
  protected Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    String id = (String) workItem.getParameter("id");

    final SyncCallInfo<CreateCDMById> sci = new SyncCallInfo<CreateCDMById>("createCdmByIdEndpoint", CreateCDMById.class, paramUtility);
    results.put("result", sci.getClient().executeSync(id));
    return results;
  }

}
