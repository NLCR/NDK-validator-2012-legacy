/**
 * 
 */
package com.logica.ndk.tm.utilities.file;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author kovalcikm
 *
 */
public class CreateCDMByIdAsyncHandler extends AbstractAsyncHandler{
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    String id = (String) workItem.getParameter("id");


    final AsyncCallInfo<CreateCDMById> aci = new AsyncCallInfo<CreateCDMById>("createCDMByIdEndpoint", CreateCDMById.class, paramUtility);
    aci.getClient().executeAsync(id);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}