package com.logica.ndk.tm.utilities.integration.wf.finishedTask;

import com.logica.ndk.tm.utilities.integration.wf.enumerator.Enumerator;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Scanner;

public class FinishedPackageTask extends FinishedTask {
  
  // Create dir params
  String pathId;
  String uuid;

  // Scan params
  Long scanId;
  Boolean complete;
  String localURN;
  String scanTypeCode;
  String pages;
  String scanDuration;
  String scannerCode;
  int scanCount;
  Boolean doublePage;
  Integer dpi;
  String cropTypeCode;
  String profilePPCode;
  String profileMCCode;
  String profileUCCode;
  int dimensionX;
  int dimensionY;
  String profilePP;
  String profileMC;
  String profileUC;
  Boolean scanAtPreparation;
  Boolean destructiveDigitization;
  String pageCount;
  int minOCRRate;
  String colorCode;
  String scanModeCode;
  int ocrLicenceUsed;

  // OCR params
  Enumerator ocr;
  int ocrRate;

  public Integer getDpi() {
		return dpi;
  }

  public void setDpi(Integer dpi) {
		this.dpi = dpi;
  }

  public Boolean isComplete() {
    return complete;
  }

  public void setComplete(Boolean complete) {
    this.complete = complete;
  }

  public String getLocalURN() {
    return localURN;
  }

  public void setLocalURN(String localURN) {
    this.localURN = localURN;
  }

  public String getScanTypeCode() {
    return scanTypeCode;
  }

  public void setScanTypeCode(String scanTypeCode) {
    this.scanTypeCode = scanTypeCode;
  }

  public String getPages() {
    return pages;
  }

  public void setPages(String pages) {
    this.pages = pages;
  }

  public String getScanDuration() {
    return scanDuration;
  }

  public void setScanDuration(String scanDuration) {
    this.scanDuration = scanDuration;
  }

  public Boolean isDoublePage() {
    return doublePage;
  }

  public void setDoublePage(Boolean doublePage) {
    this.doublePage = doublePage;
  }

  public String getScannerCode() {
    return scannerCode;
  }

  public void setScannerCode(String scannerCode) {
    this.scannerCode = scannerCode;
  }

  public Enumerator getOcr() {
    return ocr;
  }

  public void setOcr(Enumerator ocr) {
    this.ocr = ocr;
  }

  public int getOcrRate() {
    return ocrRate;
  }

  public void setOcrRate(int ocrRate) {
    this.ocrRate = ocrRate;
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

  public int getScanCount() {
    return scanCount;
  }

  public void setScanCount(int scanCount) {
    this.scanCount = scanCount;
  }

  public Long getScanId() {
    return scanId;
  }

  public void setScanId(Long scanId) {
    this.scanId = scanId;
  }

  public Boolean getComplete() {
    return complete;
  }

  public Boolean getDoublePage() {
    return doublePage;
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

  public String getProfilePP() {
    return profilePP;
  }

  public void setProfilePP(String profilePP) {
    this.profilePP = profilePP;
  }

  public String getProfileMC() {
    return profileMC;
  }

  public void setProfileMC(String profileMC) {
    this.profileMC = profileMC;
  }

  public String getProfileUC() {
    return profileUC;
  }

  public void setProfileUC(String profileUC) {
    this.profileUC = profileUC;
  }

  public Boolean getScanAtPreparation() {
    return scanAtPreparation;
  }

  public void setScanAtPreparation(Boolean scanAtPreparation) {
    this.scanAtPreparation = scanAtPreparation;
  }

  public Boolean getDestructiveDigitization() {
    return destructiveDigitization;
  }

  public void setDestructiveDigitization(Boolean destructiveDigitization) {
    this.destructiveDigitization = destructiveDigitization;
  }

  public String getPageCount() {
    return pageCount;
  }

  public void setPageCount(String pageCount) {
    this.pageCount = pageCount;
  }

  public int getMinOCRRate() {
    return minOCRRate;
  }

  public void setMinOCRRate(int minOCRRate) {
    this.minOCRRate = minOCRRate;
  }

  public String getColorCode() {
    return colorCode;
  }

  public void setColorCode(String colorCode) {
    this.colorCode = colorCode;
  }

  public String getScanModeCode() {
    return scanModeCode;
  }

  public void setScanModeCode(String scanModeCode) {
    this.scanModeCode = scanModeCode;
  }

  public String getProfileMCCode() {
    return profileMCCode;
  }

  public void setProfileMCCode(String profileMCCode) {
    this.profileMCCode = profileMCCode;
  }

  public String getProfileUCCode() {
    return profileUCCode;
  }

  public void setProfileUCCode(String profileUCCode) {
    this.profileUCCode = profileUCCode;
  }

  public int getOcrLicenceUsed() {
    return ocrLicenceUsed;
  }

  public void setOcrLicenceUsed(int ocrLicenceUsed) {
    this.ocrLicenceUsed = ocrLicenceUsed;
  }
  
  
}
