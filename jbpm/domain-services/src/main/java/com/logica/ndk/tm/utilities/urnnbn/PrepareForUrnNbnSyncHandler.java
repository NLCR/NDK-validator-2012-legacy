/**
 * 
 */
package com.logica.ndk.tm.utilities.urnnbn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.urnnbn.PrepareForUrnNbn;

/**
 * @author kovalcikm
 *
 */
public class PrepareForUrnNbnSyncHandler extends AbstractSyncHandler{
  @Override
  protected Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    String cdmId = (String) workItem.getParameter("cdmId");
    final String registrarCode = (String) workItem.getParameter("sigla");
    final Integer pageCount = Integer.parseInt((String) workItem.getParameter("pageCount"));
    final SyncCallInfo<PrepareForUrnNbn> sci = new SyncCallInfo<PrepareForUrnNbn>("prepareForUrnNbnEndpoint", PrepareForUrnNbn.class, paramUtility);
    results.put("result", sci.getClient().executeSync(cdmId, registrarCode,pageCount));
    return results;
  }
}
