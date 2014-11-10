package com.logica.ndk.tm.utilities.integration.wf.task;

import java.io.Serializable;
import java.util.Date;

import com.logica.ndk.tm.utilities.integration.wf.enumerator.Enumerator;


/**
 * Representation of WF Scan
 * @author majdaf
 *
 */
public class Scan extends Task implements Serializable {
  private static final long serialVersionUID = 1L;
  public static final String[] HEADER = new String[] { "packageId", "createDT", "createUserName", "scanId", "scannerCode",
    "scanTypeCode", "localURN", "note", "scanCount", "doublePage", "pages", "validity", "scanMode", "statePP", "cropTypeCode",
    "profilePPCode", "dimensionX", "dimensionY", "scanDuration" };
    
  Long packageId;
  Date createDT;
  String createUserName;
  Long scanId;
  String scannerCode;
  String scanTypeCode;
  String localURN;
  String note;
  Integer scanCount;
  Boolean doublePage;
  String pages;
  Boolean validity;
  String scanModeCode;
  int statePP;
  String cropTypeCode;
  String profilePPCode;
  int dimensionX;
  int dimensionY;
  Long scanDuration;
  Integer dpi;
  
  public Scan(){
   }
  
  public Scan(Long packageId, Date createDT, String createUserName, Long scanId, String scannerCode, String scanTypeCode, String localURN, String note, Integer scanCount, Boolean doublePage, String pages, Boolean validity, String scanModeCode, int statePP, String cropTypeCode, String profilePPCode,
      int dimensionX, int dimensionY, Long scanDuration) {
    super();
    this.packageId = packageId;
    this.createDT = createDT;
    this.createUserName = createUserName;
    this.scanId = scanId;
    this.scannerCode = scannerCode;
    this.scanTypeCode = scanTypeCode;
    this.localURN = localURN;
    this.note = note;
    this.scanCount = scanCount;
    this.doublePage = doublePage;
    this.pages = pages;
    this.validity = validity;
    this.scanModeCode = scanModeCode;
    this.statePP = statePP;
    this.cropTypeCode = cropTypeCode;
    this.profilePPCode = profilePPCode;
    this.dimensionX = dimensionX;
    this.dimensionY = dimensionY;
    this.scanDuration = scanDuration;
  }
  
  
  public Scan(Long packageId, Date createDT, String createUserName, Long scanId, String scannerCode, String scanTypeCode, String localURN, String note, Integer scanCount, Boolean doublePage, String pages, Boolean validity, String scanModeCode, int statePP, String cropTypeCode, String profilePPCode,
      int dimensionX, int dimensionY, Long scanDuration, int dpi) {
    super();
    this.packageId = packageId;
    this.createDT = createDT;
    this.createUserName = createUserName;
    this.scanId = scanId;
    this.scannerCode = scannerCode;
    this.scanTypeCode = scanTypeCode;
    this.localURN = localURN;
    this.note = note;
    this.scanCount = scanCount;
    this.doublePage = doublePage;
    this.pages = pages;
    this.validity = validity;
    this.scanModeCode = scanModeCode;
    this.statePP = statePP;
    this.cropTypeCode = cropTypeCode;
    this.profilePPCode = profilePPCode;
    this.dimensionX = dimensionX;
    this.dimensionY = dimensionY;
    this.scanDuration = scanDuration;
    this.dpi = dpi;
  }



  public String toString() {
    return scanId + "(" + scanTypeCode + ")";
  }
  
  public Long getPackageId() {
    return packageId;
  }
  public void setPackageId(Long packageId) {
    this.packageId = packageId;
  }
  public Date getCreateDT() {
    return createDT;
  }
  public void setCreateDT(Date createDT) {
    this.createDT = createDT;
  }
  public String getCreateUserName() {
    return createUserName;
  }
  public void setCreateUserName(String createUserName) {
    this.createUserName = createUserName;
  }
  public Long getScanId() {
    return scanId;
  }
  public void setScanId(Long scanId) {
    this.scanId = scanId;
  }
  public String getScanTypeCode() {
    return scanTypeCode;
  }
  public void setScanTypeCode(String scanTypeCode) {
    this.scanTypeCode = scanTypeCode;
  }
  public String getLocalURN() {
    return localURN;
  }
  public void setLocalURN(String localURN) {
    this.localURN = localURN;
  }
  public String getNote() {
    return note;
  }
  public void setNote(String note) {
    this.note = note;
  }
  public Integer getScanCount() {
    return scanCount;
  }
  public void setScanCount(Integer scanCount) {
    this.scanCount = scanCount;
  }
  public Boolean getDoublePage() {
    return doublePage;
  }
  public void setDoublePage(Boolean doublePage) {
    this.doublePage = doublePage;
  }
  public String getPages() {
    return pages;
  }
  public void setPages(String pages) {
    this.pages = pages;
  }
  public Boolean getValidity() {
    return validity;
  }
  public void setValidity(Boolean validity) {
    this.validity = validity;
  }
  public String getScanModeCode() {
    return scanModeCode;
  }
  public void setScanModeCode(String scanModeCode) {
    this.scanModeCode = scanModeCode;
  }
  public int getStatePP() {
    return statePP;
  }
  public void setStatePP(int statePP) {
    this.statePP = statePP;
  }
  public int getDimensionX() {
    return dimensionX;
  }
  public void setDimensionX(int dimensionX) {
    this.dimensionX = dimensionX;
  }
  public int getDimensionY() {
    return dimensionY;
  }
  public void setDimensionY(int dimensionY) {
    this.dimensionY = dimensionY;
  }
  public Long getScanDuration() {
    return scanDuration;
  }
  public void setScanDuration(Long scanDuration) {
    this.scanDuration = scanDuration;
  }
  public String getCropTypeCode() {
    return cropTypeCode;
  }
  public void setCropTypeCode(String cropTypeCode) {
    this.cropTypeCode = cropTypeCode;
  }
  public String getProfilePPCode() {
    return profilePPCode;
  }
  public void setProfilePPCode(String profilePPCode) {
    this.profilePPCode = profilePPCode;
  }
  public String getScannerCode() {
    return scannerCode;
  }
  public void setScannerCode(String scannerCode) {
    this.scannerCode = scannerCode;
  }

  @Override
  public Enumerator getImportType() {
    return null;
  }
  @Override
  public void setImportType(Enumerator enumerator) {
    // TODO Auto-generated method stub
    
  }



  public Integer getDpi() {
    return dpi;
  }



  public void setDpi(Integer dpi) {
    this.dpi = dpi;
  }
  
}
