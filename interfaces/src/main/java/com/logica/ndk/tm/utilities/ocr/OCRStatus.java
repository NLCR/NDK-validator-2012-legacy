package com.logica.ndk.tm.utilities.ocr;

import com.logica.ndk.tm.utilities.ResponseStatus;

/**
 * @author ondrusekl
 */
public abstract class OCRStatus extends ResponseStatus {

  public final static String RESPONSE_SOFT_LIMIT_EXCEEDED = "SOFT LIMIT EXCEEDED";
  public final static String RESPONSE_HARD_LIMIT_EXCEEDED = "OCR PROCESSING TIMEOUT";
  public final static String RESPONSE_IN_PROGRESS = "IN PROGRESS";
  public final static String RESPONSE_EXCEPTION_OCCURED = "EXCEPTION OCCURED";

}
