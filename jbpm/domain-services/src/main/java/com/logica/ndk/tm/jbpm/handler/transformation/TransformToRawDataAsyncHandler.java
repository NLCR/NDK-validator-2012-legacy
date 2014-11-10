/**
 * 
 */
package com.logica.ndk.tm.jbpm.handler.transformation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.ProcessParams;
import com.logica.ndk.tm.utilities.transformation.TransformToRawData;

/**
 * @author kovalcikm
 */
public class TransformToRawDataAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {

    final String sourceDirPath = resolveParam((String) workItem.getParameter("source"), workItem.getParameters());
    final String targetDirPath = resolveParam((String) workItem.getParameter("target"), workItem.getParameters());
    final String cdmId = (String) workItem.getParameter("cdmId");

    final AsyncCallInfo<TransformToRawData> aci = new AsyncCallInfo<TransformToRawData>("transformToRawDataEndpoint", TransformToRawData.class, paramUtility);
    aci.getClient().executeAsync(cdmId, sourceDirPath, targetDirPath);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    log.info("Setting dpi to process: " + (String) response);
    results.put(ProcessParams.PARAM_NAME_DPI, (String) response);
    return results;
  }
}
