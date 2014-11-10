package com.logica.ndk.tm.jbpm.handler.ocr;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.io.CopyAltoFromOcrOutput;
import com.logica.ndk.tm.utilities.io.CopyTxtFromOcrOutput;


/**
 * @author ondrusekl
 */
public class CopyTxtFromOcrOutputAsyncHandler extends AbstractAsyncHandler {

  @Override
  protected String executeAsyncWorkItem(final WorkItem workItem, final List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");
    
    final String TXT_FILE_MASK = TmConfig.instance().getString("process.ocr.textFileSuffix");

    final CDM cdm = new CDM();
    final String cdmId = (String) workItem.getParameter("cdmId");
    
    String ocr = (String) workItem.getParameter("ocr");
    String ocrFont = (String) workItem.getParameter("ocrFont");
    String language = (String) workItem.getParameter("language");
    
    if (ocr == null){
      ocr = "";
    } 
    if (ocrFont == null){
      ocrFont = "";
    }
    
    if (language==null){
      language = "";
    }
    
    final AsyncCallInfo<CopyTxtFromOcrOutput> aci = new AsyncCallInfo<CopyTxtFromOcrOutput>("copyTxtFromOcrOutputEndpoint", CopyTxtFromOcrOutput.class, paramUtility);
    log.info("Copy TXT files with mask ", TXT_FILE_MASK);
    //aci.getClient().copyAsync(OCR_ENGINE_OUTPUT_DIR + "/" + cdmId, cdm.getAltoDir(cdmId).getAbsolutePath(), ALTO_FILE_MASK);
    aci.getClient().executeAsync(cdmId, ocr, ocrFont, language, cdm.getTxtDir(cdmId).getAbsolutePath(), TXT_FILE_MASK);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(final Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }

}
