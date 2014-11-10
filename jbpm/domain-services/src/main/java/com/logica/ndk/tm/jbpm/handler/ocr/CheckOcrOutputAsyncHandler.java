package com.logica.ndk.tm.jbpm.handler.ocr;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map;

import org.drools.runtime.process.WorkItem;

import com.google.common.collect.Maps;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.process.CheckOcrOutputResponse;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ProcessParams;
import com.logica.ndk.tm.utilities.ocr.CheckOcrOutput;
import com.logica.ndk.tm.utilities.ocr.OCRStatus;

/**
 * @author ondrusekl
 */
public class CheckOcrOutputAsyncHandler extends AbstractAsyncHandler {

  @Override
  protected String executeAsyncWorkItem(WorkItem workItem, List<ParamUtility> paramUtility) throws Exception {
    checkNotNull(workItem, "workItem must not be null");

    log.info("executeAsyncWorkItem started");

    Integer counter = (Integer) workItem.getParameter("counter");
    final int OCR_CHECK_MAX_RETRY = TmConfig.instance().getInt("process.ocr.maxRetry");
    if (counter > OCR_CHECK_MAX_RETRY) {
      throw new BusinessException("Max retrying ocr check exceeded.");
    }

    String cdmId = (String) workItem.getParameter("cdmId");
    String ocr = (String) workItem.getParameter("ocr");
    String ocrFont = (String) workItem.getParameter("ocrFont");
    String language = (String) workItem.getParameter("language");

    log.info("OCR: " + ocr);

    if (ocr == null) {
      ocr = "";
    }
    if (ocrFont == null) {
      ocrFont = "";
    }

    if (language == null) {
      language = "";
    }

    final AsyncCallInfo<CheckOcrOutput> aci = new AsyncCallInfo<CheckOcrOutput>("checkOcrOutputEndpoint", CheckOcrOutput.class, paramUtility);

    aci.getClient().checkAsync(cdmId);

    log.info("executeAsyncWorkItem finished");
    return aci.getCorrelationId();
  }

  @Override
  public Map<String, Object> processResponse(Object response) throws Exception {
    checkNotNull(response, "response must not be null");

    log.info("processResponse started");

    final Map<String, Object> results = Maps.newHashMap();

    CheckOcrOutputResponse checkStatus = (CheckOcrOutputResponse) response;
    log.info("Code: " + checkStatus.getResponseCode());
    log.info("OCR: " + checkStatus.getOcr());
    log.info("OCR rate: " + checkStatus.getOcrRate());

    if (OCRStatus.RESPONSE_SOFT_LIMIT_EXCEEDED.equalsIgnoreCase(checkStatus.getResponseCode())) {
      throw new BusinessException("Probably OCR engine not running");
    }
    else if (OCRStatus.RESPONSE_HARD_LIMIT_EXCEEDED.equalsIgnoreCase(checkStatus.getResponseCode())) {
      // TODO ondrusekl (23.2.2012): Informovat ze proces trva moc dlouho
      throw new BusinessException("Process is staying in OCR engine too long");
    }
    else if (OCRStatus.RESPONSE_IN_PROGRESS.equalsIgnoreCase(checkStatus.getResponseCode())) {

      log.info("OCR in progess");
      return results;
    }
    else if (OCRStatus.RESPONSE_EXCEPTION_OCCURED.equalsIgnoreCase(checkStatus.getResponseCode())) {
      log.info("OCR process has generated exception file");
      results.put(ProcessParams.PARAM_NAME_OCR_RATE, checkStatus.getOcrRate());
      results.put(ProcessParams.PARAM_NAME_OCR_PAGES_OK, checkStatus.getOcrPagesOk());
      results.put(ProcessParams.PARAM_NAME_OCR_PAGES_EXCEPTION, checkStatus.getOcrPagesException());
      results.put(ProcessParams.PARAM_NAME_OCR_LICENCE_USED, checkStatus.getOcrLicenceUsed());
      return results;
//      throw new BusinessException("OCR process has generated exception file");
    }
    else {
      log.info("OCR engine finished job");

      //results.put(ProcessParams.PARAM_NAME_OCR, checkStatus.getOcr());
      results.put(ProcessParams.PARAM_NAME_OCR_RATE, checkStatus.getOcrRate());
      results.put(ProcessParams.PARAM_NAME_OCR_PAGES_OK, checkStatus.getOcrPagesOk());
      results.put(ProcessParams.PARAM_NAME_OCR_PAGES_EXCEPTION, checkStatus.getOcrPagesException());
      results.put(ProcessParams.PARAM_NAME_OCR_LICENCE_USED, checkStatus.getOcrLicenceUsed());

      log.info("processResponse finished");
      return results;
    }
  }

}
