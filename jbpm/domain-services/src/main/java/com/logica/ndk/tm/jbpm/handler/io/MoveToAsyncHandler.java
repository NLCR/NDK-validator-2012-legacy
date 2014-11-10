package com.logica.ndk.tm.jbpm.handler.io;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.io.MoveTo;

/**
 * @author palousp
 */
public class MoveToAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String sourceDirName = resolveParam((String) workItem.getParameter("sourceDirName"), workItem.getParameters());
    final String destDirName = resolveParam((String) workItem.getParameter("destDirName"), workItem.getParameters());
    final String pattern = resolveParam((String) workItem.getParameter("pattern"), workItem.getParameters());
    
    log.info("sourceDirName token: {}",workItem.getParameter("sourceDirName"));
    log.info("destDirName token: {}",workItem.getParameter("destDirName"));
    
    log.info("sourceDirName token resolved to : {}",sourceDirName);
    log.info("destDirName token resolved to : {}",destDirName);
    
    log.info("cdmID: {}",workItem.getParameter("cdmId"));
    
    Preconditions.checkNotNull(sourceDirName, "sourceDirName must not be null");
    Preconditions.checkNotNull(destDirName, "destDirName must not be null");
    
    final AsyncCallInfo<MoveTo> aci = new AsyncCallInfo<MoveTo>("moveToEndpoint", MoveTo.class, paramUtility);
    aci.getClient().moveDirAsync(sourceDirName, destDirName, pattern);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }

}
