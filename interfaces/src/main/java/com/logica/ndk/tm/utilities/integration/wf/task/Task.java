package com.logica.ndk.tm.utilities.integration.wf.task;

import com.logica.ndk.tm.utilities.integration.wf.enumerator.Activity;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Enumerator;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.User;

import java.util.Date;

/**
 * Represents a task passed from WF to TM
 * 
 * @author majdaf
 */
public abstract class Task {
  Long id;
  String packageType;
  Activity activity;
  String pathId;
  String uuid;
  Enumerator locality;
  Boolean error;
  String note;
  User reservedBy;
  Date reservedDT;
  String reservedInternalId;
  String progress;
  String comment;
  Long sourcePackage;
  Date activityDT;
  Date finishDT;
  Task sourcePackageObject;
  String modifyDT;
  String deactivated;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Activity getActivity() {
    return activity;
  }

  public void setActivity(Activity activity) {
    this.activity = activity;
  }

//  public Boolean isError() {
//    return error;
//  }

  public User getReservedBy() {
    return reservedBy;
  }

  public void setReservedBy(User reservedBy) {
    this.reservedBy = reservedBy;
  }

  public String getReservedInternalId() {
    return reservedInternalId;
  }

  public void setReservedInternalId(String reservedInternalId) {
    this.reservedInternalId = reservedInternalId;
  }

  public Date getReservedDT() {
    return reservedDT;
  }

  public void setReservedDT(Date reservedDT) {
    this.reservedDT = reservedDT;
  }

  public Boolean getError() {
    return error;
  }

  public void setError(Boolean error) {
    this.error = error;
  }

  public Long getSourcePackage() {
    return sourcePackage;
  }

  public void setSourcePackage(Long sourcePackage) {
    this.sourcePackage = sourcePackage;
  }

  public Date getActivityDT() {
    return activityDT;
  }

  public void setActivityDT(Date activityDT) {
    this.activityDT = activityDT;
  }

  public Date getFinishDT() {
    return finishDT;
  }

  public void setFinishDT(Date finishDT) {
    this.finishDT = finishDT;
  }

  public String getPackageType() {
    return packageType;
  }

  public void setPackageType(String packageType) {
    this.packageType = packageType;
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

  public Enumerator getLocality() {
    return locality;
  }

  public void setLocality(Enumerator locality) {
    this.locality = locality;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public String getProgress() {
    return progress;
  }

  public void setProgress(String progress) {
    this.progress = progress;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public Task getSourcePackageObject() {
    return sourcePackageObject;
  }

  public void setSourcePackageObject(Task sourcePackageObject) {
    this.sourcePackageObject = sourcePackageObject;
  }

  public abstract Enumerator getImportType();

  public abstract void setImportType(Enumerator enumerator);

  public String getModifyDT() {
    return modifyDT;
  }

  public void setModifyDT(String modifyDT) {
    this.modifyDT = modifyDT;
  }

  public String getDeactivated() {
    return deactivated;
  }

  public void setDeactivated(String deactivated) {
    this.deactivated = deactivated;
  }

  public boolean equals(Object o) {
    if (o == null || !(o instanceof Task)) {
      return false;
    }
    Task other = (Task) o;
    if ((other.getId() == null && this.getId() != null) || other.getId() != null && this.getId() == null) {
      return false;
    }

    return other.getId().equals(this.getId());
  }
}
