/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

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

/**
 * @author kovalcikm
 */
public class UpdateFoxmlMetadataAsyncHandler extends AbstractAsyncHandler {
  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");
    log.info("CreateScansCSVAsyncHandler started.");
    final String cdmId = (String) workItem.getParameter("cdmId");
    final String updateDC = (String) workItem.getParameter("updateDC");
    final String updateMods = (String) workItem.getParameter("updateMods");
    final String updatePolicy = (String) workItem.getParameter("updatePolicy");
    final String locality = (String) workItem.getParameter("locality");
    final String processPages = (String) workItem.getParameter("processPages");
    String policyFilePath = (String) workItem.getParameter("policyFilePath");

    log.info("cdmId: " + cdmId);
    Preconditions.checkNotNull(cdmId, "cdmId must not be null");
    Preconditions.checkNotNull(updateDC, "updateDC must not be null");
    Preconditions.checkNotNull(updateMods, "updateMods must not be null");
    Preconditions.checkNotNull(updatePolicy, "updatePolicy must not be null");
    Preconditions.checkNotNull(locality, "locality must not be null");
    Preconditions.checkNotNull(processPages, "processPages must not be null");

    if (policyFilePath == null) {
      policyFilePath = "";
    }

    List<String> metadataParts = new ArrayList<String>();
    if (Boolean.parseBoolean(updateDC)) {
      metadataParts.add("dc");
    }
    if (Boolean.parseBoolean(updateMods)) {
      metadataParts.add("mods");
    }
    if (Boolean.parseBoolean(updatePolicy)) {
      metadataParts.add("policy");
    }
    boolean processPagesBool = Boolean.parseBoolean(processPages);

    final AsyncCallInfo<UpdateFoxmlMetadata> aci = new AsyncCallInfo<UpdateFoxmlMetadata>("updateFoxmlMetadataEndpoint", UpdateFoxmlMetadata.class, paramUtility);
    aci.getClient().executeAsync(cdmId, metadataParts, locality, policyFilePath, processPagesBool);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}
