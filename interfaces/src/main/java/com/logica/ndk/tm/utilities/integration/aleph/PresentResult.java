package com.logica.ndk.tm.utilities.integration.aleph;

import java.io.Serializable;

public class PresentResult implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  String OAIMARC;
  String docNumber;
  String recordIdentifier;

  public String getOAIMARC() {
    return OAIMARC;
  }

  public void setOAIMARC(String oaimarc) {
    OAIMARC = oaimarc;
  }

  public String getDocNumber() {
    return docNumber;
  }

  public void setDocNumber(String docNumber) {
    this.docNumber = docNumber;
  }

  public String getRecordIdentifier() {
    return recordIdentifier;
  }

  public void setRecordIdentifier(String recordIdentifier) {
    this.recordIdentifier = recordIdentifier;
  }
  
}
