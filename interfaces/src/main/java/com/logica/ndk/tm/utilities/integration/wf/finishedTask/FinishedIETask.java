package com.logica.ndk.tm.utilities.integration.wf.finishedTask;

import com.logica.ndk.tm.utilities.integration.wf.enumerator.Enumerator;

/**
 * Finished task over intelectual entity in WF
 * 
 * @author majdaf
 */
public class FinishedIETask extends FinishedTask {
  // Create intellectual entity params
  Boolean processEM;
  Boolean processLTP;
  Boolean processKrameriusNkcr;
  Boolean processKrameriusMzk;
  Boolean processUrnnbn;
  Enumerator sourcePackage;
  String pathId;
  String uuid;
  String title;
  String author;
  String language;
  String isbn;
  String issn;
  String ccnb;
  String sigla;
  String volumeDate;
  String volumeNumber;
  String partNumber;
  String urnnbn;
  String typeCode;
  String pageCount;
  String rdId;
  String dateIssued;
  String issueNumber;
  String volumeUUID;
  String issueUUID;
  String recordIdentifier;
  String titleUUID;
  String barCode;
  String partName;

  public Boolean isProcessEM() {
    return processEM;
  }

  public Boolean getProcessEM() {
    return processEM;
  }

  public void setProcessEM(Boolean processEM) {
    this.processEM = processEM;
  }

  public Boolean isProcessLTP() {
    return processLTP;
  }

  public Boolean getProcessLTP() {
    return processLTP;
  }

  public void setProcessLTP(Boolean processLTP) {
    this.processLTP = processLTP;
  }

  public Boolean isProcessKrameriusNkcr() {
    return processKrameriusNkcr;
  }

  public Boolean getProcessKrameriusNkcr() {
    return processKrameriusNkcr;
  }

  public void setProcessKrameriusNkcr(Boolean processKrameriusNkcr) {
    this.processKrameriusNkcr = processKrameriusNkcr;
  }

  public Boolean isProcessKrameriusMzk() {
    return processKrameriusMzk;
  }

  public Boolean getProcessKrameriusMzk() {
    return processKrameriusMzk;
  }

  public void setProcessKrameriusMzk(Boolean processKrameriusMzk) {
    this.processKrameriusMzk = processKrameriusMzk;
  }

  public Enumerator getSourcePackage() {
    return sourcePackage;
  }

  public void setSourcePackage(Enumerator sourcePackage) {
    this.sourcePackage = sourcePackage;
  }

  public String getPathId() {
    return pathId;
  }

  public void setPathId(String pathId) {
    this.pathId = pathId;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getIsbn() {
    return isbn;
  }

  public void setIsbn(String isbn) {
    this.isbn = isbn;
  }

  public String getIssn() {
    return issn;
  }

  public void setIssn(String issn) {
    this.issn = issn;
  }

  public String getCcnb() {
    return ccnb;
  }

  public void setCcnb(String ccnb) {
    this.ccnb = ccnb;
  }

  public String getSigla() {
    return sigla;
  }

  public void setSigla(String sigla) {
    this.sigla = sigla;
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

  public String getUrnnbn() {
    return urnnbn;
  }

  public void setUrnnbn(String urnnbn) {
    this.urnnbn = urnnbn;
  }

  public String getTypeCode() {
    return typeCode;
  }

  public void setTypeCode(String typeCode) {
    this.typeCode = typeCode;
  }

  public String getPageCount() {
    return pageCount;
  }

  public void setPageCount(String pageCount) {
    this.pageCount = pageCount;
  }

  public String getRdId() {
    return rdId;
  }

  public void setRdId(String rdId) {
    this.rdId = rdId;
  }

  public Boolean isProcessUrnnbn() {
    return processUrnnbn;
  }

  public Boolean getProcessUrnnbn() {
    return processUrnnbn;
  }

  public void setProcessUrnnbn(Boolean processUrnnbn) {
    this.processUrnnbn = processUrnnbn;
  }

  public String getDateIssued() {
    return dateIssued;
  }

  public void setDateIssued(String dateIssued) {
    this.dateIssued = dateIssued;
  }

  public String getIssueNumber() {
    return issueNumber;
  }

  public void setIssueNumber(String issueNumber) {
    this.issueNumber = issueNumber;
  }

  public String getVolumeUUID() {
    return volumeUUID;
  }

  public void setVolumeUUID(String volumeUUID) {
    this.volumeUUID = volumeUUID;
  }

  public String getIssueUUID() {
    return issueUUID;
  }

  public void setIssueUUID(String issueUUID) {
    this.issueUUID = issueUUID;
  }

  public String getRecordIdentifier() {
    return recordIdentifier;
  }

  public void setRecordIdentifier(String recordIdentifier) {
    this.recordIdentifier = recordIdentifier;
  }

  public String getTitleUUID() {
    return titleUUID;
  }

  public void setTitleUUID(String titleUUID) {
    this.titleUUID = titleUUID;
  }

  public String getBarCode() {
    return barCode;
  }

  public void setBarCode(String barCode) {
    this.barCode = barCode;
  }

  public String getPartName() {
    return partName;
  }

  public void setPartName(String partName) {
    this.partName = partName;
  }
}
