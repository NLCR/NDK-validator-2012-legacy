package com.logica.ndk.tm.jbpm.handler.ocr;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.ocr.CopyToOcrInput;

/**
 * Copy images to OCR transfer (input) directory according list. The list of images is read
 * from file created in FilesListImpl.
 * 
 * @author Petr Palous
 */
public class CopyToOcrInputAsyncHandler extends AbstractAsyncHandler {

  @Override
  protected String executeAsyncWorkItem(final WorkItem workItem, final List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    final String cdmId = (String) workItem.getParameter("cdmId");

    final AsyncCallInfo<CopyToOcrInput> aci = new AsyncCallInfo<CopyToOcrInput>("copyToOcrInputEndpoint", CopyToOcrInput.class, paramUtility);
    aci.getClient().copyAsync(cdmId);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(final Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }

}
