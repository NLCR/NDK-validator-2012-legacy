package com.logica.ndk.tm.utilities.integration.wf.task;

/**
 *
 * @author krchnacekm
 */
public class TaskHeaderBuilder {
    String activityCode;
    Long id;
    String documentLocalityCode;
    String title;
    String localityCode;
    boolean error;
    String barCode;
    String reservedByName;
    String reservedInternalId;
    String packageType;
    String modifyDT;
    String cdmId;
    String deactivated;
    
    public TaskHeaderBuilder() {
    }
    
    public TaskHeader build() {
        return new TaskHeader(this.activityCode, this.id, this.documentLocalityCode, this.title, this.localityCode, this.error, this.barCode, this.reservedByName, this.reservedInternalId, this.packageType, this.modifyDT, this.cdmId, this.deactivated);
    }

    public TaskHeaderBuilder setActivityCode(String activityCode) {
        this.activityCode = activityCode;
        return this;
    }

    public TaskHeaderBuilder setId(Long id) {
        this.id = id;
        return this;
    }

    public TaskHeaderBuilder setDocumentLocalityCode(String documentLocalityCode) {
        this.documentLocalityCode = documentLocalityCode;
        return this;
    }

    public TaskHeaderBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public TaskHeaderBuilder setLocalityCode(String localityCode) {
        this.localityCode = localityCode;
        return this;
    }

    public TaskHeaderBuilder setError(boolean error) {
        this.error = error;
        return this;
    }

    public TaskHeaderBuilder setBarCode(String barCode) {
        this.barCode = barCode;
        return this;
    }

    public TaskHeaderBuilder setReservedByName(String reservedByName) {
        this.reservedByName = reservedByName;
        return this;
    }

    public TaskHeaderBuilder setReservedInternalId(String reservedInternalId) {
        this.reservedInternalId = reservedInternalId;
        return this;
    }

    public TaskHeaderBuilder setPackageType(String packageType) {
        this.packageType = packageType;
        return this;
    }

    public TaskHeaderBuilder setModifyDT(String modifyDT) {
        this.modifyDT = modifyDT;
        return this;
    }

    public TaskHeaderBuilder setCdmId(String cdmId) {
        this.cdmId = cdmId;
        return this;
    } 
    
    public TaskHeaderBuilder setDeactivated(String deactivated) {
      this.deactivated = deactivated;
      return this;
  } 
}
