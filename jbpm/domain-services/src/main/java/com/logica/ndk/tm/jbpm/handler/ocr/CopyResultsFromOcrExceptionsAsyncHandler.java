/**
 * 
 */
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
import com.logica.ndk.tm.utilities.io.CopyToSmallFiles;

/**
 * @author kovalcikm
 *
 */
public class CopyResultsFromOcrExceptionsAsyncHandler extends AbstractAsyncHandler {

  private final String RESULTS_FILE_MASK = TmConfig.instance().getString("process.ocr.resultFileSuffix");

  @Override
  protected String executeAsyncWorkItem(final WorkItem workItem, final List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

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
    OcrProfileHelper ocrProfileHelper = new OcrProfileHelper();
    ocrProfileHelper.setOcr(ocr);
    ocrProfileHelper.setOcrFont(ocrFont);
    ocrProfileHelper.setLanguage(language);
    final String OCR_ENGINE_OUTPUT_DIR = ocrProfileHelper.retrieveFromConfig("outputDir");
    final String OCR_ENGINE_EXCEPTION_DIR = ocrProfileHelper.retrieveFromConfig("exceptionsDir");

    final CDM cdm = new CDM();

    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
    Date date = new Date();
    String dateTime = dateFormat.format(date);

    final String cdmId = (String) workItem.getParameter("cdmId");
    final AsyncCallInfo<CopyToSmallFiles> aci = new AsyncCallInfo<CopyToSmallFiles>("copyToSmallFilesEndpoint", CopyToSmallFiles.class, paramUtility);

    aci.getClient().copySmallFilesAsync(OCR_ENGINE_EXCEPTION_DIR + File.separator + cdmId, cdm.getOcrDir(cdmId) + "/exception" + File.separator + dateTime, RESULTS_FILE_MASK);
    log.info("Ocr exception result copied to: " + cdm.getOcrDir(cdmId) + "/exception" + File.separator + dateTime);
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(final Object response) throws Exception {
    final Map<String, Object> results = new HashMap<String, Object>();
    results.put("result", response);
    return results;
  }

}
