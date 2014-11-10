package com.logica.ndk.tm.utilities.integration.wf.task;

/**
 * Represents a basic information about WF Task as response to GetWaitingTasks
 *
 * @author majdaf
 */
public class TaskHeader {
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
    String pathId;
    String deactivated;

    public TaskHeader(Task task) {
        id = task.getId();
        packageType = task.getPackageType();
        activityCode = task.getActivity().getCode();
    }

    public TaskHeader() {
    }

    public TaskHeader(String activityCode, Long id, String documentLocalityCode, String title, String localityCode, boolean error, String barCode, String reservedByName, String reservedInternalId, String packageType, String modifyDT, String pathId, String deactivated) {
        this.activityCode = activityCode;
        this.id = id;
        this.documentLocalityCode = documentLocalityCode;
        this.title = title;
        this.localityCode = localityCode;
        this.error = error;
        this.barCode = barCode;
        this.reservedByName = reservedByName;
        this.reservedInternalId = reservedInternalId;
        this.packageType = packageType;
        this.modifyDT = modifyDT;
        this.pathId = pathId;
        this.deactivated = deactivated;
    }

    public String toString() {
        String result = String.valueOf(getId());
        if (getReservedByName() != null) {
            result += " by " + getReservedByName();
        }
        if (getTitle() != null && getTitle().length() > 0) {
            result += " [" + getTitle() + "]";
        }
        return result;
    }


    public String getActivityCode() {
        return activityCode;
    }

    public void setActivityCode(String activityCode) {
        this.activityCode = activityCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReservedByName() {
        return reservedByName;
    }

    public void setReservedByName(String reservedByName) {
        this.reservedByName = reservedByName;
    }

    public String getReservedInternalId() {
        return reservedInternalId;
    }

    public void setReservedInternalId(String reservedInternalId) {
        this.reservedInternalId = reservedInternalId;
    }

    public String getLocalityCode() {
        return localityCode;
    }

    public void setLocalityCode(String localityCode) {
        this.localityCode = localityCode;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public String getDocumentLocalityCode() {
        return documentLocalityCode;
    }

    public void setDocumentLocalityCode(String documentLocalityCode) {
        this.documentLocalityCode = documentLocalityCode;
    }

    public String getPackageType() {
        return packageType;
    }

    public void setPackageType(String packageType) {
        this.packageType = packageType;
    }

    public String getModifyDT() {
        return modifyDT;
    }

    public void setModifyDT(String modifyDT) {
        this.modifyDT = modifyDT;
    }

    public String getPathId() {
        return pathId;
    }

    public void setPathId(String pathId) {
        this.pathId = pathId;
    }
    
    public String getDeactivated() {
      return deactivated;
    }

    public void setDeactivated(String deactivated) {
      this.deactivated = deactivated;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final TaskHeader that = (TaskHeader) o;

        if (error != that.error) return false;
        if (activityCode != null ? !activityCode.equals(that.activityCode) : that.activityCode != null) return false;
        if (barCode != null ? !barCode.equals(that.barCode) : that.barCode != null) return false;
        if (documentLocalityCode != null ? !documentLocalityCode.equals(that.documentLocalityCode) : that.documentLocalityCode != null)
            return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (localityCode != null ? !localityCode.equals(that.localityCode) : that.localityCode != null) return false;
        if (modifyDT != null ? !modifyDT.equals(that.modifyDT) : that.modifyDT != null) return false;
        if (packageType != null ? !packageType.equals(that.packageType) : that.packageType != null) return false;
        if (pathId != null ? !pathId.equals(that.pathId) : that.pathId != null) return false;
        if (reservedByName != null ? !reservedByName.equals(that.reservedByName) : that.reservedByName != null)
            return false;
        if (reservedInternalId != null ? !reservedInternalId.equals(that.reservedInternalId) : that.reservedInternalId != null)
            return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (deactivated != null ? !deactivated.equals(that.deactivated) : that.deactivated != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = activityCode != null ? activityCode.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (documentLocalityCode != null ? documentLocalityCode.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (localityCode != null ? localityCode.hashCode() : 0);
        result = 31 * result + (error ? 1 : 0);
        result = 31 * result + (barCode != null ? barCode.hashCode() : 0);
        result = 31 * result + (reservedByName != null ? reservedByName.hashCode() : 0);
        result = 31 * result + (reservedInternalId != null ? reservedInternalId.hashCode() : 0);
        result = 31 * result + (packageType != null ? packageType.hashCode() : 0);
        result = 31 * result + (modifyDT != null ? modifyDT.hashCode() : 0);
        result = 31 * result + (pathId != null ? pathId.hashCode() : 0);
        result = 31 * result + (deactivated != null ? deactivated.hashCode() : 0);
        return result;
    }
}
