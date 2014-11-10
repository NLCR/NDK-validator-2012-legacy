package com.logica.ndk.tm.jbpm.handler.ocr;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.ocr.FilesList;

/**
 * @author ondrusekl
 */
public class FilesListAsyncHandler extends AbstractAsyncHandler {

  @Override
  protected String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    final String cdmId = (String) workItem.getParameter("cdmId");
    final AsyncCallInfo<FilesList> aci = new AsyncCallInfo<FilesList>("filesListEndpoint", FilesList.class, paramUtility);
    aci.getClient().generateAsync(cdmId);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("inputPageCount", response);
    return results;
  }

}
