package com.logica.ndk.tm.utilities;

import javax.xml.ws.WebFault;

/**
 * Parent for all exceptions from package <code>com.logica.ndk.tm.utilities</code>.
 * 
 * @author ondrusekl
 */
@WebFault
public abstract class UtilityException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private Long errorCode;
  private String nodeId;

  protected UtilityException() {
    super();
  }

  protected UtilityException(String message, Throwable cause) {
    super(message, cause);
  }

  protected UtilityException(String message) {
    super(message);
  }

  protected UtilityException(Throwable cause) {
    super(cause);
  }

  protected UtilityException(String message, Throwable cause, Long errorCode) {
    super(message, cause);
    this.errorCode = errorCode;
  }

  protected UtilityException(String message, Long errorCode) {
    super(message);
    this.errorCode = errorCode;
  }

  protected UtilityException(Throwable cause, Long errorCode) {
    super(cause);
    this.errorCode = errorCode;
  }

  public Long getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(Long errorCode) {
    this.errorCode = errorCode;
  }
  
  public String getNodeId() {
    return nodeId;
  }

  public void setNodeId(String nodeId) {
    this.nodeId = nodeId;
  }

}
