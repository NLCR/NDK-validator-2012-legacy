package com.logica.ndk.tm.utilities.transformation.em.getuuid.cdm;

import com.logica.ndk.tm.utilities.integration.wf.TaskFinder;

/**
 *
 * @author krchnacekm
 */
public final class TaskFinderBuilder {

    private String barCode;
    private boolean onlyWaiting;
    private boolean onlyForSystem;
    private String reservedInternalId;
    private String activityCode;
    private int maxItems = 2000;
    private Boolean error;
    private String packageType;
    private String localityCode;
    private String ccnb;
    private String recordIdentifier;
    private String issn;
    private String volumeNumber;
    
    public TaskFinderBuilder() {
        
    }
    
    public TaskFinderBuilder setCcnb(String ccnb) {
        this.ccnb = ccnb;
        return this;
    }

    public TaskFinderBuilder setRecordIdentifier(String recordIdentifier) {
        this.recordIdentifier = recordIdentifier;
        return this;
    }

    public TaskFinderBuilder setIssn(String issn) {
        this.issn = issn;
        return this;
    }
    
    public TaskFinderBuilder setVolumeNumber(String volumeNumber) {
        this.volumeNumber = volumeNumber;
        return this;
    }

    public TaskFinderBuilder setBarCode(String barCode) {
        this.barCode = barCode;
        return this;
    }

    public TaskFinderBuilder setOnlyWaiting(boolean onlyWaiting) {
        this.onlyWaiting = onlyWaiting;
        return this;
    }

    public TaskFinderBuilder setOnlyForSystem(boolean onlyForSystem) {
        this.onlyForSystem = onlyForSystem;
        return this;
    }

    public TaskFinderBuilder setReservedInternalId(String reservedInternalId) {
        this.reservedInternalId = reservedInternalId;
        return this;
    }

    public TaskFinderBuilder setActivityCode(String activityCode) {
        this.activityCode = activityCode;
        return this;
    }

    public TaskFinderBuilder setMaxItems(int maxItems) {
        this.maxItems = maxItems;
        return this;
    }

    public TaskFinderBuilder setError(Boolean error) {
        this.error = error;
        return this;
    }

    public TaskFinderBuilder setPackageType(String packageType) {
        this.packageType = packageType;
        return this;
    }

    public TaskFinderBuilder setLocalityCode(String localityCode) {
        this.localityCode = localityCode;
        return this;
    }

    public TaskFinder build() {
        final TaskFinder finder = new TaskFinder();
        finder.setBarCode(barCode);
        finder.setOnlyWaiting(onlyWaiting);
        finder.setOnlyForSystem(onlyForSystem);
        finder.setReservedInternalId(reservedInternalId);
        finder.setActivityCode(activityCode);
        finder.setMaxItems(maxItems);
        finder.setError(error);
        finder.setPackageType(packageType);
        finder.setLocalityCode(localityCode);
        finder.setCcnb(ccnb);
        finder.setRecordIdentifier(recordIdentifier);
        finder.setIssn(issn); 
        finder.setVolumeNumber(volumeNumber);
        return finder;
    }
}
