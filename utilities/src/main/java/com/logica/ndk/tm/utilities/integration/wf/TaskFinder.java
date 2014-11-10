package com.logica.ndk.tm.utilities.integration.wf;

import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.config.TmConfig;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Logger;

/**
 * Finder for querying WF tasks (REST API)
 *
 * @author majdaf
 *
 */
public class TaskFinder {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(TaskFinder.class);

    String barCode;
    boolean onlyWaiting;
    boolean onlyForSystem;
    String reservedInternalId;
    String activityCode;
    int maxItems = TmConfig.instance().getInt("wf.maxItems");
    Boolean error;
    String packageType;
    String localityCode;
    String recordIdentifier;
    String ccnb;
    String issn;
    String volumeNumber;
    String titleUUID;
    String volumeUUID;

    public String getQueryParams() {
        StringBuffer b = new StringBuffer();
        // Max items
        b.append("maxItems=" + maxItems);

        // Package type
        if (packageType != null) {
            b.append("&packageType=" + packageType);
        }

        // Only waiting
        if (onlyWaiting) {
            b.append("&onlyWaiting=true");
        }

        // Only for system
        if (onlyForSystem) {
            b.append("&onlyForSystem=true");
        }

        // Reserved internal ID
        if (reservedInternalId != null) {
            b.append("&reservedInternalId=" + reservedInternalId);
        }

        // Bar code
        if (barCode != null) {
            b.append("&barCode=" + barCode);
        }

        // Activity code
        if (activityCode != null) {
            b.append("&activityCode=" + activityCode);
        }

        // Error
        if (error != null) {
            b.append("&error=" + error);
        }

        //document locality code    
        if (localityCode != null) {
            b.append("&localityCode=" + localityCode);
        }

        if (recordIdentifier != null) {
            b.append("&recordIdentifier=" + recordIdentifier);
        }

        if (ccnb != null) {
            b.append("&ccnb=" + ccnb);
        }

        if (issn != null) {
            b.append("&issn=" + issn);
        }

        if (volumeNumber != null) {
            final String encoding = "UTF8";
            try {
                String encodedVolumeNumber = URLEncoder.encode(volumeNumber, encoding);
                b.append("&volumeNumber=" + encodedVolumeNumber);
            } catch (UnsupportedEncodingException e) {
                log.error(String.format("UnsupportedEncodingException during encodig of volumeNumber: %s, encoding: %s", volumeNumber, encoding), e);
            }
        }

        if (titleUUID != null) {
          b.append("&titleUUID=" + titleUUID);
        }
        
        if (volumeUUID != null) {
            b.append("&volumeUUID=" + volumeUUID);
          }

        return b.toString();
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public boolean isOnlyWaiting() {
        return onlyWaiting;
    }

    public void setOnlyWaiting(boolean onlyWaiting) {
        this.onlyWaiting = onlyWaiting;
    }

    public boolean isOnlyForSystem() {
        return onlyForSystem;
    }

    public void setOnlyForSystem(boolean onlyForSystem) {
        this.onlyForSystem = onlyForSystem;
    }

    public String getReservedInternalId() {
        return reservedInternalId;
    }

    public void setReservedInternalId(String reservedInternalId) {
        this.reservedInternalId = reservedInternalId;
    }

    public int getMaxItems() {
        return maxItems;
    }

    public void setMaxItems(int maxItems) {
        this.maxItems = maxItems;
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

    public void setError(Boolean error) {
        this.error = error;
    }

    public String getPackageType() {
        return packageType;
    }

    public void setPackageType(String packageType) {
        this.packageType = packageType;
    }

    public String getRecordIdentifier() {
        return recordIdentifier;
    }

    public void setRecordIdentifier(String recordIdentifier) {
        this.recordIdentifier = recordIdentifier;
    }

    public String getCcnb() {
        return ccnb;
    }

    public void setCcnb(String ccnb) {
        this.ccnb = ccnb;
    }

    public String getIssn() {
        return issn;
    }

    public void setIssn(String issn) {
        this.issn = issn;
    }

    public String getVolumeNumber() {
        return volumeNumber;
    }

    public void setVolumeNumber(String volumeNumber) {
        this.volumeNumber = volumeNumber;
    }

    public String getLocalityCode() {
        return localityCode;
    }

    public void setLocalityCode(String localityCode) {
        this.localityCode = localityCode;
    }

    public String getTitleUUID() {
      return titleUUID;
    }

    public void setTitleUUID(String titleUUID) {
      this.titleUUID = titleUUID;
    }
    
    public String getVolumeUUID() {
		return volumeUUID;
	}

	public void setVolumeUUID(String volumeUUID) {
		this.volumeUUID = volumeUUID;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskFinder that = (TaskFinder) o;

        if (maxItems != that.maxItems) return false;
        if (onlyForSystem != that.onlyForSystem) return false;
        if (onlyWaiting != that.onlyWaiting) return false;
        if (activityCode != null ? !activityCode.equals(that.activityCode) : that.activityCode != null) return false;
        if (barCode != null ? !barCode.equals(that.barCode) : that.barCode != null) return false;
        if (ccnb != null ? !ccnb.equals(that.ccnb) : that.ccnb != null) return false;
        if (error != null ? !error.equals(that.error) : that.error != null) return false;
        if (issn != null ? !issn.equals(that.issn) : that.issn != null) return false;
        if (localityCode != null ? !localityCode.equals(that.localityCode) : that.localityCode != null) return false;
        if (packageType != null ? !packageType.equals(that.packageType) : that.packageType != null) return false;
        if (recordIdentifier != null ? !recordIdentifier.equals(that.recordIdentifier) : that.recordIdentifier != null)
            return false;
        if (reservedInternalId != null ? !reservedInternalId.equals(that.reservedInternalId) : that.reservedInternalId != null)
            return false;
        if (volumeNumber != null ? !volumeNumber.equals(that.volumeNumber) : that.volumeNumber != null) return false;
        if (titleUUID != null ? !titleUUID.equals(that.titleUUID) : that.titleUUID != null) return false;
        if (volumeUUID != null ? !volumeUUID.equals(that.volumeUUID) : that.volumeUUID != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = barCode != null ? barCode.hashCode() : 0;
        result = 31 * result + (onlyWaiting ? 1 : 0);
        result = 31 * result + (onlyForSystem ? 1 : 0);
        result = 31 * result + (reservedInternalId != null ? reservedInternalId.hashCode() : 0);
        result = 31 * result + (activityCode != null ? activityCode.hashCode() : 0);
        result = 31 * result + maxItems;
        result = 31 * result + (error != null ? error.hashCode() : 0);
        result = 31 * result + (packageType != null ? packageType.hashCode() : 0);
        result = 31 * result + (localityCode != null ? localityCode.hashCode() : 0);
        result = 31 * result + (recordIdentifier != null ? recordIdentifier.hashCode() : 0);
        result = 31 * result + (ccnb != null ? ccnb.hashCode() : 0);
        result = 31 * result + (issn != null ? issn.hashCode() : 0);
        result = 31 * result + (volumeNumber != null ? volumeNumber.hashCode() : 0);
        result = 31 * result + (titleUUID != null ? titleUUID.hashCode() : 0);
        result = 31 * result + (volumeUUID != null ? volumeUUID.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TaskFinder{" +
                "barCode='" + barCode + '\'' +
                ", onlyWaiting=" + onlyWaiting +
                ", onlyForSystem=" + onlyForSystem +
                ", reservedInternalId='" + reservedInternalId + '\'' +
                ", activityCode='" + activityCode + '\'' +
                ", maxItems=" + maxItems +
                ", error=" + error +
                ", packageType='" + packageType + '\'' +
                ", localityCode='" + localityCode + '\'' +
                ", recordIdentifier='" + recordIdentifier + '\'' +
                ", ccnb='" + ccnb + '\'' +
                ", issn='" + issn + '\'' +
                ", volumeNumber='" + volumeNumber + '\'' +
                ", titleUUID='" + titleUUID + '\'' +
                ", volumeUUID='" + volumeUUID + '\'' +
                '}';
    }

}
