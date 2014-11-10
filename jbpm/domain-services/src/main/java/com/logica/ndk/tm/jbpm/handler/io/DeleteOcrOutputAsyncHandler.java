package com.logica.ndk.tm.jbpm.handler.io;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.base.Preconditions;
import com.logica.ndk.commons.ocr.OcrProfileHelper;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.io.Delete;

/**
 * @author kovalcikm
 */
public class DeleteOcrOutputAsyncHandler extends AbstractAsyncHandler {

  @Override
  public String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    String cdmId = (String) workItem.getParameter("cdmId");
    log.info("MoveOcrFilesAsyncHandler cdmID: {}", cdmId);

    final String ocr = resolveParam((String) workItem.getParameter("ocr"), workItem.getParameters());
    log.info("Ocr profile: " + ocr);

    OcrProfileHelper ocrProfileHelper = new OcrProfileHelper();
    ocrProfileHelper.setOcr(ocr);

    final String OCR_ENGINE_TRANSFER_OUT_DIR = ocrProfileHelper.retrieveFromConfig("outputDir");
    final String DELETE_PATH = OCR_ENGINE_TRANSFER_OUT_DIR + File.separator + cdmId;

    String throwNotFoudExObj = (String) workItem.getParameter("notThrowNotFoudEx");
    log.info("Parameter throwNotFoudEx: " + throwNotFoudExObj);

    boolean throwNotFoudEx = false;
    if (Boolean.parseBoolean(throwNotFoudExObj)) {
      throwNotFoudEx = true;
    }

    Preconditions.checkNotNull(DELETE_PATH, "path must not be null");
    final AsyncCallInfo<Delete> aci = new AsyncCallInfo<Delete>("deleteEndpoint", Delete.class, paramUtility);
    aci.getClient().deleteAsync(DELETE_PATH, throwNotFoudEx);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }

}
