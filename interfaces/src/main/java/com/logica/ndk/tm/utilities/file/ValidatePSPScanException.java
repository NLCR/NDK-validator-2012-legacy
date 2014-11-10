package com.logica.ndk.tm.utilities.file;

import javax.xml.ws.WebFault;

import com.logica.ndk.tm.utilities.UtilityException;

@WebFault
public class ValidatePSPScanException extends UtilityException {
  private static final long serialVersionUID = -7026593028630491305L;

  protected ValidatePSPScanException() {
    super();
  }

  protected ValidatePSPScanException(String message, Throwable cause) {
    super(message, cause);
  }

  protected ValidatePSPScanException(String message) {
    super(message);
  }

  protected ValidatePSPScanException(Throwable cause) {
    super(cause);
  }
}
