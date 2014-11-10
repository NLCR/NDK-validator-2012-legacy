package com.logica.ndk.tm.utilities.transformation.em;

/**
 * @author ondrusekl
 */
public class EmCsvRecord implements Comparable<EmCsvRecord> {

  public static final String[] HEADER = new String[] { "pageId", "pageLabel", "pageType", "pageOrder", "pageOrderLabel", "dmdId", "scanId", "scanType", "scanNote", "admid", "scanMode", "profilOCR", "OCRResult" };

  private String pageId;
  private String pageLabel;
  private EmPageType pageType;
  private int pageOrder;
  private String pageOrderLabel;
  private String dmdId;
  private String scanId;
  private String scanType;
  private String scanNote;
  private String admid;
  private String scanMode;
  private String profilOCR;
  private String OCRResult;

  public EmCsvRecord(final String pageId, final String pageLabel, final EmPageType pageType, final int pageOrder, final String pageOrderLabel, final String dmdId, final String scanId, final String scanType, final String scanNote, final String admid, final String scanMode, final String profilOCR, final String OCRResult) {
    this.pageId = pageId;
    this.pageLabel = pageLabel;
    this.pageType = pageType;
    this.pageOrder = pageOrder;
    this.pageOrderLabel = pageOrderLabel;
    this.dmdId = dmdId;
    this.scanId = scanId;
    this.scanType = scanType;
    this.scanNote = scanNote;
    this.admid = admid;
    this.scanMode = scanMode;
    this.profilOCR = profilOCR;
    this.OCRResult = OCRResult;
  }

  public String[] asCsvRecord() {
    return new String[] { pageId, pageLabel, pageType.name(), String.valueOf(pageOrder), pageOrderLabel, dmdId, scanId, scanType, scanNote, admid, scanMode, profilOCR, OCRResult };
  }

  public String getPageId() {
    return pageId;
  }

  public void setPageId(final String pageId) {
    this.pageId = pageId;
  }

  public String getPageLabel() {
    return pageLabel;
  }

  public void setPageLabel(final String pageLabel) {
    this.pageLabel = pageLabel;
  }

  public EmPageType getPageType() {
    return pageType;
  }

  public void setPageType(final EmPageType pageType) {
    this.pageType = pageType;
  }

  public int getPageOrder() {
    return pageOrder;
  }

  public void setPageOrder(final int pageOrder) {
    this.pageOrder = pageOrder;
  }

  public String getPageOrderLabel() {
    return pageOrderLabel;
  }

  public String getDmdId() {
    return dmdId;
  }

  public void setDmdId(final String dmdId) {
    this.dmdId = dmdId;
  }

  public void setPageOrderLabel(final String pageOrderLabel) {
    this.pageOrderLabel = pageOrderLabel;
  }

  public String getScanId() {
    return scanId;
  }

  public void setScanId(final String scanId) {
    this.scanId = scanId;
  }

  public String getScanType() {
    return scanType;
  }

  public void setScanType(final String scanType) {
    this.scanType = scanType;
  }

  public String getScanNote() {
    return scanNote;
  }

  public void setScanNote(final String scanNote) {
    this.scanNote = scanNote;
  }

  public String getAdmid() {
    return admid;
  }

  public void setAdmid(final String admid) {
    this.admid = admid;
  }

  public String getScanMode() {
    return scanMode;
  }

  public void setScanMode(String scanMode) {
    this.scanMode = scanMode;
  }

    public String getProfilOCR() {
        return profilOCR;
    }

    public void setProfilOCR(String profilOCR) {
        this.profilOCR = profilOCR;
    }

    public String getOCRResult() {
        return OCRResult;
    }

    public void setOCRResult(String OCRResult) {
        this.OCRResult = OCRResult;
    }

  


public enum EmPageType {
    normalPage, // default
    spine,
    advertisement,
    cover,
    blank,
    frontCover,
    frontEndSheet,
    index,
    listOfIllustrations,
    listOfMaps,
    listOfTables,
    tableOfContents,
    table,
    titlePage,
    flyLeaf,
    backCover,
    backEndSheet,
    customInclude,
    forDeletion,
    jacket,
    frontJacket,
    map
  }

  @Override
  public int compareTo(EmCsvRecord o) {
    return new Integer(this.pageOrder).compareTo(new Integer(o.getPageOrder()));
  }

}
