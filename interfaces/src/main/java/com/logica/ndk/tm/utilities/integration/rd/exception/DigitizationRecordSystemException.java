package com.logica.ndk.tm.utilities.integration.rd.exception;

import javax.xml.ws.WebFault;

import com.logica.ndk.tm.utilities.SystemException;

/**
 * Exception on communication with Digitization registry.
 * 
 * @author ondrusekl
 */
@WebFault
public class DigitizationRecordSystemException extends SystemException {

  private static final long serialVersionUID = 1L;

  public DigitizationRecordSystemException() {
    super();
  }

  public DigitizationRecordSystemException(String message, Throwable cause) {
    super(message, cause);
  }

  public DigitizationRecordSystemException(String message) {
    super(message);
  }

  public DigitizationRecordSystemException(Throwable cause) {
    super(cause);
  }

}
