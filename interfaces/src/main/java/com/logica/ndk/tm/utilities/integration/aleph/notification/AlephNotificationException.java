package com.logica.ndk.tm.utilities.integration.aleph.notification;

import javax.xml.ws.WebFault;

import com.logica.ndk.tm.utilities.BusinessException;

@WebFault
public class AlephNotificationException extends BusinessException {
  private static final long serialVersionUID = 1386847500671138996L;

  public AlephNotificationException() {
    super();
  }

  public AlephNotificationException(String message, Throwable cause) {
    super(message, cause);
  }

  public AlephNotificationException(String message) {
    super(message);
  }

  public AlephNotificationException(Throwable cause) {
    super(cause);
  }

  public AlephNotificationException(Throwable cause, Long errorCode) {
    super(cause, errorCode);
  }

  public AlephNotificationException(String message, Throwable cause, Long errorCode) {
    super(message, cause, errorCode);
  }

  public AlephNotificationException(String message, Long errorCode) {
    super(message, errorCode);
  }
  
  

}
