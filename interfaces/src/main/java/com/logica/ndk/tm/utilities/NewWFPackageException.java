/**
 * 
 */
package com.logica.ndk.tm.utilities;

/**
 * @author kovalcikm
 */
public class NewWFPackageException extends UtilityException {
  private static final long serialVersionUID = 1L;

  private String packageId;
  private String reason;

  public NewWFPackageException(String packageId, String reason) {
    this.packageId = packageId;
    this.reason = reason;
  }

  public String getPackageId() {
    return packageId;
  }

  public void setPackageId(String packageId) {
    this.packageId = packageId;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

}
