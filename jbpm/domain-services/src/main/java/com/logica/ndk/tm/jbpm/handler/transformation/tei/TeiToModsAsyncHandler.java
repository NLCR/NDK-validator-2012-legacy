package com.logica.ndk.tm.jbpm.handler.transformation.tei;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.transformation.tei.TeiToMods;

public class TeiToModsAsyncHandler extends AbstractAsyncHandler {

  @Override
  protected String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    log.info("executeAsyncWorkItem started");

    final String inFilePath = (String) workItem.getParameter("inFilePath");
    checkNotNull(inFilePath, "inFilePath must not be null");
    log.debug("cdmId: " + inFilePath);

    final String outFilePath = (String) workItem.getParameter("outFilePath");
    checkNotNull(outFilePath, "outFilePath must not be null");
    log.debug("cdmId: " + outFilePath);

    final AsyncCallInfo<TeiToMods> aci = new AsyncCallInfo<TeiToMods>("TeiToModsEndpoint", TeiToMods.class, paramUtility);
    aci.getClient().executeAsync(inFilePath, outFilePath);

    log.info("executeAsyncWorkItem finished");
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {

    log.info("processResponse started");

    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);

    log.info("processResponse finished");
    return results;
  }

}
