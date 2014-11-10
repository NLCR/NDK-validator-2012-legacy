/**
 * 
 */
package com.logica.ndk.tm.utilities.ocr;

/**
 * @author kovalcikm
 */
public class OcrResultInfo {
  String ocrProfile;
  int numberOfLicences = 0;
  int numberOfPages = 0;

  public OcrResultInfo() {

  }

  public OcrResultInfo(String ocrProfile, int numberOfLicences, int numberOfPages) {
    super();
    this.ocrProfile = ocrProfile;
    this.numberOfLicences = numberOfLicences;
    this.numberOfPages = numberOfPages;
  }

  public String getOcrProfile() {
    return ocrProfile;
  }

  public void setOcrProfile(String ocrProfile) {
    this.ocrProfile = ocrProfile;
  }

  public int getNumberOfLicences() {
    return numberOfLicences;
  }

  public void setNumberOfLicences(int numberOfLicences) {
    this.numberOfLicences = numberOfLicences;
  }

  public int getNumberOfPages() {
    return numberOfPages;
  }

  public void setNumberOfPages(int numberOfPages) {
    this.numberOfPages = numberOfPages;
  }

}
