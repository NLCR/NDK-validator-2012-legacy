/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.manual;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author majdaf
 */
public class CountPreprocessPagesAsyncHandler extends AbstractAsyncHandler {
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {

    checkNotNull(workItem, "workItem must not be null");

    final String cdmId = (String) workItem.getParameter("cdmId");
    final String profilesString = (String) workItem.getParameter("profiles");

    log.info("profiles: " + profilesString);
    Preconditions.checkNotNull(cdmId, "cdmId must not be null");
    Preconditions.checkNotNull(profilesString, "profiles must not be null");

    List<String> profiles = Arrays.asList(profilesString.split(";"));
    
    final AsyncCallInfo<CountPreprocessPages> aci = new AsyncCallInfo<CountPreprocessPages>("countPreprocessPagesEndpoint", CountPreprocessPages.class, paramUtility);
    aci.getClient().executeAsync(cdmId, profiles);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}
