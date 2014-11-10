package com.logica.ndk.tm.jbpm.handler.ocr;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.commons.ocr.OcrProfileHelper;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.io.CopyTo;
import com.logica.ndk.tm.utilities.io.CopyToSmallFiles;
import com.logica.ndk.tm.utilities.ocr.CopyOcrResult;

/**
 * @author ondrusekl
 */
public class CopyOcrResultAsyncHandler extends AbstractAsyncHandler {

  private final String RESULTS_FILE_MASK = TmConfig.instance().getString("process.ocr.resultFileSuffix");

  @Override
  protected String executeAsyncWorkItem(final WorkItem workItem, final List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");
    
    final String cdmId = (String) workItem.getParameter("cdmId");

    String ocr = (String) workItem.getParameter("ocr");
    String ocrFont = (String) workItem.getParameter("ocrFont");
    String language = (String) workItem.getParameter("language");

    if (ocr == null) {
      ocr = "";
    }
    if (ocrFont == null) {
      ocrFont = "";
    }

    if (language == null) {
      language = "";
    }

    final AsyncCallInfo<CopyOcrResult> aci = new AsyncCallInfo<CopyOcrResult>("copyOcrResultEndpoint", CopyOcrResult.class, paramUtility);
    aci.getClient().executeAsync(cdmId, ocr, ocrFont, language);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(final Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }

}
