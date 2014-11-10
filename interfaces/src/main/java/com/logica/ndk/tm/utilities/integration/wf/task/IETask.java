package com.logica.ndk.tm.utilities.integration.wf.task;

import com.logica.ndk.tm.utilities.integration.wf.enumerator.Enumerator;

public class IETask extends PackageTask {
  boolean processEM;
  boolean processLTP;
  boolean processKrameriusNkcr;
  boolean processKrameriusMzk;
  boolean processUrnnbn;
  String typeCode;
  String volumeDate;
  String volumeNumber;
  String partNumber;
  boolean ocrChanged;
  Enumerator importType;

  public boolean isProcessEM() {
    return processEM;
  }

  public void setProcessEM(boolean processEM) {
    this.processEM = processEM;
  }

  public boolean isProcessLTP() {
    return processLTP;
  }

  public void setProcessLTP(boolean processLTP) {
    this.processLTP = processLTP;
  }

  public boolean isProcessKrameriusNkcr() {
    return processKrameriusNkcr;
  }

  public void setProcessKrameriusNkcr(boolean processKrameriusNkcr) {
    this.processKrameriusNkcr = processKrameriusNkcr;
  }

  public boolean isProcessKrameriusMzk() {
    return processKrameriusMzk;
  }

  public void setProcessKrameriusMzk(boolean processKrameriusMzk) {
    this.processKrameriusMzk = processKrameriusMzk;
  }

  public String getTypeCode() {
    return typeCode;
  }

  public void setTypeCode(String typeCode) {
    this.typeCode = typeCode;
  }

  public String getVolumeDate() {
    return volumeDate;
  }

  public void setVolumeDate(String volumeDate) {
    this.volumeDate = volumeDate;
  }

  public String getVolumeNumber() {
    return volumeNumber;
  }

  public void setVolumeNumber(String volumeNumber) {
    this.volumeNumber = volumeNumber;
  }

  public String getPartNumber() {
    return partNumber;
  }

  public void setPartNumber(String partNumber) {
    this.partNumber = partNumber;
  }

  public boolean isOcrChanged() {
    return ocrChanged;
  }

  public void setOcrChanged(boolean ocrChanged) {
    this.ocrChanged = ocrChanged;
  }

  public Enumerator getImportType() {
    return importType;
  }

  public void setImportType(Enumerator importType) {
    this.importType = importType;
  }

  public boolean isProcessUrnnbn() {
    return processUrnnbn;
  }

  public void setProcessUrnnbn(boolean processUrnnbn) {
    this.processUrnnbn = processUrnnbn;
  }
}
