package com.logica.ndk.tm.process;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.ws.WebFault;

/**
 * System exception thrown by JBPMWSFacade.
 * 
 * @author Rudolf Daco
 */
@WebFault(name = "JBPMSystemException")
@XmlAccessorType(XmlAccessType.FIELD)
public class JBPMSystemException extends Exception {
  private static final long serialVersionUID = -6457051998688823900L;
  
  /**
   * Class name of the root exception
   */
  private String rootExceptionName;

  public JBPMSystemException() {
    super();
  }

  public JBPMSystemException(String message, Throwable cause) {
    super(message, cause);
    if (cause != null) {
      rootExceptionName = cause.getClass().getName();
    }
  }

  public JBPMSystemException(Throwable cause) {
    super(cause);
    if (cause != null) {
      rootExceptionName = cause.getClass().getName();
    }
  }
  
  public String getRootExceptionName() {
    return rootExceptionName;
  }

  public void setRootExceptionName(String rootExceptionName) {
    this.rootExceptionName = rootExceptionName;
  }
}
