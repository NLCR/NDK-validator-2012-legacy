package com.logica.ndk.tm.process;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.ws.WebFault;

/**
 * Business exception thrown by JBPMWSFacade.
 * 
 * @author Rudolf Daco
 */
@WebFault(name = "JBPMBusinessException")
@XmlAccessorType(XmlAccessType.FIELD)
public class JBPMBusinessException extends Exception {
  private static final long serialVersionUID = -3399607841025174057L;

  /**
   * Class name of the root exception
   */
  private String rootExceptionName;

  public JBPMBusinessException() {
    super();
  }

  public JBPMBusinessException(String message, Throwable cause) {
    super(message, cause);
    if (cause != null) {
      rootExceptionName = cause.getClass().getName();
    }
  }
  
  public JBPMBusinessException(Throwable cause) {
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
