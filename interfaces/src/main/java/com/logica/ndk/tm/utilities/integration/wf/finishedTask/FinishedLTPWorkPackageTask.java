package com.logica.ndk.tm.utilities.integration.wf.finishedTask;


/**
 * Finished task over intelectual entity in WF
 * @author majdaf
 *
 */
public class FinishedLTPWorkPackageTask extends FinishedIETask {
  Boolean processManual;
  String aipId;
  String barCode;

  public Boolean getProcessManual() {
    return processManual;
  }

  public void setProcessManual(Boolean processManual) {
    this.processManual = processManual;
  }

  public Boolean isProcessManual() {
    return processManual;
  }

  public String getAipId() {
    return aipId;
  }

  public void setAipId(String aipId) {
    this.aipId = aipId;
  }

  public String getBarCode() {
    return barCode;
  }

  public void setBarCode(String barCode) {
    this.barCode = barCode;
  }
}
