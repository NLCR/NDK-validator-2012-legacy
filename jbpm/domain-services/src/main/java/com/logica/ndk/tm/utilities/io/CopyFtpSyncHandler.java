/**
 * 
 */
package com.logica.ndk.tm.utilities.io;

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
public class CopyFtpSyncHandler extends AbstractSyncHandler{

  @Override
  protected Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    String url = (String) workItem.getParameter("url");
    String destDir = (String) workItem.getParameter("destDir");
    String login = (String) workItem.getParameter("login");
    String password = (String) workItem.getParameter("password");
    final SyncCallInfo<CopyFtp> sci = new SyncCallInfo<CopyFtp>("copyFtpEndpoint", CopyFtp.class, paramUtility);
    results.put("result", sci.getClient().executeSync(url, destDir, login, password));
    return results;
  }

  
}
