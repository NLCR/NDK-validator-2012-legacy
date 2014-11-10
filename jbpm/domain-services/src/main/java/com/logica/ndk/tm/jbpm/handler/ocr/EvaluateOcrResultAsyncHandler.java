/**
 * 
 */
package com.logica.ndk.tm.jbpm.handler.ocr;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.ocr.EvaluateOcrResult;

/**
 * @author kovalcikm
 */
public class EvaluateOcrResultAsyncHandler extends AbstractAsyncHandler {

  @Override
  protected String executeAsyncWorkItem(final WorkItem workItem, final List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    final String cdmId = (String) workItem.getParameter("cdmId");
    final Integer ocrPagesExeption = (Integer) workItem.getParameter("ocrPagesException");

    Preconditions.checkNotNull(cdmId, "cdmId must not be null");

    final AsyncCallInfo<EvaluateOcrResult> aci = new AsyncCallInfo<EvaluateOcrResult>("evaluateOcrResultEndpoint", EvaluateOcrResult.class, paramUtility);
    aci.getClient().executeAsync(cdmId, ocrPagesExeption);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(final Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }

}
