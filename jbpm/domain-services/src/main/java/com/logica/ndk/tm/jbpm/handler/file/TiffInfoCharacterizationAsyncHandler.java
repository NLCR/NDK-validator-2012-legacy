/**
 * 
 */
package com.logica.ndk.tm.jbpm.handler.file;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.file.TiffInfoCharacterization;

/**
 * @author kovalcikm
 *
 */
public class TiffInfoCharacterizationAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String cdmId = (String) workItem.getParameter("cdmId");
    final String dir = resolveParam((String) workItem.getParameter("dir"), workItem.getParameters());
    final AsyncCallInfo<TiffInfoCharacterization> aci = new AsyncCallInfo<TiffInfoCharacterization>("tiffInfoCharacterizationEndpoint", TiffInfoCharacterization.class, paramUtility);
    aci.getClient().executeAsync(cdmId, dir);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}
