package com.logica.ndk.tm.jbpm.handler.file;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.ParamMap;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.file.FileCharacterization;

/**
 * @author rudi
 */
public class FileCharacterizationAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String cdmId = (String) workItem.getParameter("cdmId");
    final String source = resolveParam((String) workItem.getParameter("source"), workItem.getParameters());
    final String target = resolveParam((String) workItem.getParameter("target"), workItem.getParameters());
    final AsyncCallInfo<FileCharacterization> aci = new AsyncCallInfo<FileCharacterization>("fileCharacterizationEndpoint", FileCharacterization.class, paramUtility);
    aci.getClient().executeAsync(cdmId, source, target, new ParamMap());
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}
