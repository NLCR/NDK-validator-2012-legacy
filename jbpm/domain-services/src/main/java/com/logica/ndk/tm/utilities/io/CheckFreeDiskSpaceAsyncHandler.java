package com.logica.ndk.tm.utilities.io;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * Check free disk space for process running. If given required minimal space or size of 
 * given directory is bigger than actual free disk space exception is raised.
 *  	 
 * @author Petr Palous
 *
 */
public class CheckFreeDiskSpaceAsyncHandler extends AbstractAsyncHandler {
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
  	final String cdmId = (String) workItem.getParameter("cdmId");
  	final String requiredMinFreeSpaceMB = (String) workItem.getParameter("requiredMinFreeSpaceMB");
    final String cdmReferenceDir = resolveParam((String) workItem.getParameter("cdmReferenceDir"), workItem.getParameters());
    final String growCoef = (String) workItem.getParameter("growCoef");
        
    final AsyncCallInfo<CheckFreeDiskSpace> aci = new AsyncCallInfo<CheckFreeDiskSpace>("checkFreeDiskSpaceEndpoint", CheckFreeDiskSpace.class, paramUtility);
    log.info("Going to run utility CheckFreeDiskSpace.");
    aci.getClient().executeAsync(cdmId, requiredMinFreeSpaceMB, cdmReferenceDir, growCoef);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    log.info("Utility CheckFreeDiskSpace finished.");
    results.put("result", response);
    return results;
  }


}
