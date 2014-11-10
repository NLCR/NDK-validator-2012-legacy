package com.logica.ndk.tm.utilities;

import java.io.Serializable;
import java.util.Date;

import javax.xml.ws.WebFault;

/**
 * Exception message sluzi na prenos informacii o chybach medzi systemami. Je urcena napr. na prenos v JMS, pretoze v
 * JMS nemozme prenasat objekty s lubovolnymi Exceptions pretoze klient nevie deserializovat vynimky ktorych triedy
 * nepozna.
 * 
 * @author Rudolf Daco
 */
@WebFault
public class ExceptionMessage extends UtilityException implements Serializable {
  private static final long serialVersionUID = 6822050158057418254L;

  public ExceptionMessage(String message) {
    super(message);
    initCause(null);
  }

  private String exceptionName;
  private String componentName;
  private String endpointUri;
  private Date timeStamp;

  public String getExceptionName() {
    return exceptionName;
  }

  public void setExceptionName(String exceptionName) {
    this.exceptionName = exceptionName;
  }

  public String getComponentName() {
    return componentName;
  }

  public void setComponentName(String componentName) {
    this.componentName = componentName;
  }

  public String getEndpointUri() {
    return endpointUri;
  }

  public void setEndpointUri(String endpointUri) {
    this.endpointUri = endpointUri;
  }

  public Date getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(Date timeStamp) {
    this.timeStamp = timeStamp;
  }

  public String getLocalizedMessage() {
    return ErrorHelper.getLocalizedMessage(getErrorCode());
  }
}
