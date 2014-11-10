package com.logica.ndk.tm.utilities.transformation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * @author Rudolf Daco
 *
 */
public class MergeCDMAsyncHandler extends AbstractAsyncHandler {
  @Override
  protected String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    final String cdmIdMaster = (String) workItem.getParameter("cdmIdMaster");
    final String cdmIdSlave = (String) workItem.getParameter("cdmIdSlave");
    Preconditions.checkNotNull(cdmIdMaster, "cdmIdMaster must not be null");
    Preconditions.checkNotNull(cdmIdSlave, "cdmIdSlave must not be null");
    final AsyncCallInfo<MergeCDM> aci = new AsyncCallInfo<MergeCDM>("mergeCDMEndpoint", MergeCDM.class, paramUtility);
    aci.getClient().executeAsync(cdmIdMaster, cdmIdSlave);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("result", response);
    return result;
  }
}
