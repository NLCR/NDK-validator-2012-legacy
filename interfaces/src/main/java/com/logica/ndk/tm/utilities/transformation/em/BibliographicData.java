package com.logica.ndk.tm.utilities.transformation.em;

import java.io.Serializable;

/**
 * Bibliographic data wrapper
 * 
 * @author majdaf
 */
public class BibliographicData implements Serializable {
  private static final long serialVersionUID = 1L;

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
  int pageCount;
  String type;
  String dateIssued;
  String issueNumber;
  String urnnbn;
  String barCode;
  String libraryId;
  String uuid;
  String pressmark;
  String titleUUID;
  String volumeUUID;
  String issueUUID;
  String recordIdentifier;
  String partName;

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

  public static long getSerialVersionUID() {
    return serialVersionUID;
  }

  public int getPageCount() {
    return pageCount;
  }

  public void setPageCount(int pageCount) {
    this.pageCount = pageCount;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
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

  public String getUrnnbn() {
    return urnnbn;
  }

  public void setUrnnbn(String urnnbn) {
    this.urnnbn = urnnbn;
  }

  public String getBarCode() {
    return barCode;
  }

  public void setBarCode(String barcode) {
    this.barCode = barcode;
  }

  public String getLibraryId() {
    return libraryId;
  }

  public void setLibraryId(String libraryId) {
    this.libraryId = libraryId;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getPressmark() {
    return pressmark;
  }

  public void setPressmark(String pressmark) {
    this.pressmark = pressmark;
  }

  public String getTitleUUID() {
    return titleUUID;
  }

  public void setTitleUUID(String titleUUID) {
    this.titleUUID = titleUUID;
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

  public String getPartName() {
    return partName;
  }

  public void setPartName(String partName) {
    this.partName = partName;
  }

  @Override
  public String toString() {
    return "BibliographicData{" +
        "title='" + title + '\'' +
        ", author='" + author + '\'' +
        ", language='" + language + '\'' +
        ", isbn='" + isbn + '\'' +
        ", issn='" + issn + '\'' +
        ", ccnb='" + ccnb + '\'' +
        ", sigla='" + sigla + '\'' +
        ", volumeDate='" + volumeDate + '\'' +
        ", volumeNumber='" + volumeNumber + '\'' +
        ", partNumber='" + partNumber + '\'' +
        ", pageCount=" + pageCount +
        ", type='" + type + '\'' +
        ", dateIssued='" + dateIssued + '\'' +
        ", issueNumber='" + issueNumber + '\'' +
        ", urnnbn='" + urnnbn + '\'' +
        ", barCode='" + barCode + '\'' +
        ", libraryId='" + libraryId + '\'' +
        ", uuid='" + uuid + '\'' +
        ", pressmark='" + pressmark + '\'' +
        ", titleUUID='" + titleUUID + '\'' +
        ", volumeUUID='" + volumeUUID + '\'' +
        ", issueUUID='" + issueUUID + '\'' +
        ", recordIdentifier='" + recordIdentifier + '\'' +
        ", partName='" + partName + '\'' +
        '}';
  }
}
