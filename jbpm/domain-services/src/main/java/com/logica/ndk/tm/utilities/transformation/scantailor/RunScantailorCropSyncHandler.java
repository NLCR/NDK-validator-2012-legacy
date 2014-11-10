package com.logica.ndk.tm.utilities.transformation.scantailor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractSyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author rudi
 */
public class RunScantailorCropSyncHandler extends AbstractSyncHandler {

  @Override
  public Map<String, Object> executeSyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    final String cdmId = (String) workItem.getParameter("cdmId");
    final String profile = (String) workItem.getParameter("profile");
    final String colorMode = (String) workItem.getParameter("colorMode");
    final String cropType = (String) workItem.getParameter("cropType");
    final int dimensionX = (Integer) workItem.getParameter("dimensionX");
    final int dimensionY = (Integer) workItem.getParameter("dimensionY");
    final int outputDpi = (Integer) workItem.getParameter("outputDpi");
    final SyncCallInfo<RunScantailorCrop> sci = new SyncCallInfo<RunScantailorCrop>("runScantailorCropEndpoint", RunScantailorCrop.class, paramUtility);
    results.put("result", sci.getClient().executeSync(cdmId, profile, colorMode, cropType, dimensionX, dimensionY, outputDpi));
    return results;
  }
}
