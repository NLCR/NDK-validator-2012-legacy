package com.logica.ndk.tm.utilities.integration.wf.task;


public class LTPWorkPackageTask extends IETask {
  String dataType;
  String idAIP;
  String url;
  String outputURL;
  Boolean test;
  Boolean processManual;  
  String note;
  String urnnbn;

  public String getDataType() {
    return dataType;
  }

  public void setDataType(String dataType) {
    this.dataType = dataType;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getIdAIP() {
    return idAIP;
  }

  public void setIdAIP(String idAIP) {
    this.idAIP = idAIP;
  }

  public String getOutputURL() {
    return outputURL;
  }

  public void setOutputURL(String outputURL) {
    this.outputURL = outputURL;
  }

  public Boolean getTest() {
    return test;
  }

  public Boolean isTest() {
    return test;
  }

  public void setTest(Boolean test) {
    this.test = test;
  }

  public Boolean getProcessManual() {
    return processManual;
  }

  public Boolean isProcessManual() {
    return processManual;
  }

  public void setProcessManual(Boolean processManual) {
    this.processManual = processManual;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public String getUrnnbn() {
    return urnnbn;
  }

  public void setUrnnbn(String urnnbn) {
    this.urnnbn = urnnbn;
  }
    
}
