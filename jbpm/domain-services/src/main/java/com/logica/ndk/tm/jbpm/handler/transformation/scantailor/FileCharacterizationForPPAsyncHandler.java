package com.logica.ndk.tm.jbpm.handler.transformation.scantailor;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.ParamMap;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.file.FileCharacterization;

/**
 * @author rudi
 */
public class FileCharacterizationForPPAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");
    final String cdmId = (String) workItem.getParameter("cdmId");
    CDM cdm = new CDM();
    final String rawDataDir = cdm.getRawDataDir(cdmId).getAbsolutePath();
    log.debug("rawDataDir: {}", rawDataDir);
    final String amdDir = cdm.getAmdDir(cdmId).getAbsolutePath();
    log.debug("amdDir: {}", amdDir);
    final AsyncCallInfo<FileCharacterization> aci = new AsyncCallInfo<FileCharacterization>("fileCharacterizationEndpoint", FileCharacterization.class, paramUtility);
    aci.getClient().executeAsync(cdmId, rawDataDir, amdDir, new ParamMap());
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }

}
