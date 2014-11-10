package com.logica.ndk.tm.utilities.transformation.scantailor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.FormatMigrationHelper;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author rudi
 */
public class RunScantailorPreprocessAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String cdmId = (String) workItem.getParameter("cdmId");
    final String profile = (String) workItem.getParameter("profile");
    String colorMode = (String) workItem.getParameter("colorMode");
    final String cropType = (String) workItem.getParameter("cropType");
    final int dimensionX = Integer.valueOf((String) workItem.getParameter("dimensionX"));
    final int dimensionY = Integer.valueOf((String) workItem.getParameter("dimensionY"));
    Integer outputDpi;

    String outputDpiString = (String) workItem.getParameter("outputDpi");
    FormatMigrationHelper migrationHelper = new FormatMigrationHelper();
    CDM cdm = new CDM();
    if (colorMode == null && migrationHelper.isFormatMigration(cdm.getCdmProperties(cdmId).getProperty("importType"))) {
      colorMode = "MIXED";
    }

    if ((outputDpiString != null) && (!outputDpiString.isEmpty())) {
      outputDpi = Integer.valueOf(outputDpiString);
    }
    else {
      outputDpi = 0;
    }

    final AsyncCallInfo<RunScantailorPreprocess> aci = new AsyncCallInfo<RunScantailorPreprocess>("runScantailorPreprocessEndpoint", RunScantailorPreprocess.class, paramUtility);
    aci.getClient().executeAsync(cdmId, profile, colorMode, cropType, dimensionX, dimensionY, outputDpi);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }
}
