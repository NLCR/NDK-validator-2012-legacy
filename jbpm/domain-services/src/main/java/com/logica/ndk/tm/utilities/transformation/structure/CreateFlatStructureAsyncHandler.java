package com.logica.ndk.tm.utilities.transformation.structure;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.integration.wf.task.Scan;

/**
 * @author rudi
 */
public class CreateFlatStructureAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    final String cdmId = (String) workItem.getParameter("cdmId");
    final String scansString = (String) workItem.getParameter("scans");
    
    log.info("scans: " + scansString);
    Preconditions.checkNotNull(cdmId, "cdmId must not be null");
    Preconditions.checkNotNull(scansString, "scans must not be null");

    ObjectMapper mapper = new ObjectMapper();
    List<Scan> scans = mapper.readValue(scansString, new TypeReference<ArrayList<Scan>>() { });

    final AsyncCallInfo<CreateFlatStructure> aci = new AsyncCallInfo<CreateFlatStructure>("createFlatStructureEndpoint", CreateFlatStructure.class, paramUtility);
    aci.getClient().executeAsync(cdmId, scans);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}
