package com.logica.ndk.tm.utilities.integration.wf.task;

import java.util.Date;

/**
 * Representation of WF Signature
 * @author majdaf
 *
 */
public class Signature {
  Long id;
  String signatureType;
  Long packageId;
  Date createDT;
  String createUserName;
  String activityCode;
  boolean error;
  String note;
  
  public String toString() {
    return signatureType + "(" + id + ")";
  }
  
  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
  }
  public String getSignatureType() {
    return signatureType;
  }
  public void setSignatureType(String signatureType) {
    this.signatureType = signatureType;
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
  public String getActivityCode() {
    return activityCode;
  }
  public void setActivityCode(String activityCode) {
    this.activityCode = activityCode;
  }
  public boolean isError() {
    return error;
  }
  public void setError(boolean error) {
    this.error = error;
  }
  public String getNote() {
    return note;
  }
  public void setNote(String note) {
    this.note = note;
  }
}
