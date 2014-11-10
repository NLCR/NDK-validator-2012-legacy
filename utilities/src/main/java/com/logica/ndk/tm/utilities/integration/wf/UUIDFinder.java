package com.logica.ndk.tm.utilities.integration.wf;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.logica.ndk.tm.utilities.transformation.em.getuuid.UUIDType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Finder for querying WF tasks (REST API)
 * @author majdaf
 *
 */
public class UUIDFinder {
  
  private static final Logger log = LoggerFactory.getLogger(UUIDFinder.class);
    private static final String EMPTY_STRING = "";
	
  String ccnb;
  String isbn;
  String issn;
  String recordIdentifier;
  String volumeNumber;
  boolean findVolume;
  int maxItems = 2000;

  public UUIDFinder() {

  }

    /**
     * Creates UUIDFinder object. Method ignores empty values.
     *
     * @param recordIdentifier
     * @param ccnb
     * @param issn
     * @param volume
     * @param type
     * @return UUIDFinder
     */
    public UUIDFinder(String recordIdentifier, String ccnb, String issn, String volume, String type) {
        final UUIDFinder uuidFinder = new UUIDFinder();
        if (!EMPTY_STRING.equals(recordIdentifier)) {
            this.recordIdentifier = recordIdentifier;
        }
        if (!EMPTY_STRING.equals(ccnb)) {
            this.ccnb = ccnb;
        }
        if (!EMPTY_STRING.equals(issn)) {
            this.issn = issn;
        }
        if (!EMPTY_STRING.equals(volume)) {
            this.volumeNumber = volume;
        }
        if (!EMPTY_STRING.equals(type) && UUIDType.VOLUME_TYPE.getValue().equals(type)) {
            this.findVolume = true;
        }
        else {
            this.findVolume = false;
        }
    }
  
  public String getQueryParams() {
    StringBuffer b = new StringBuffer();
    // Max items
    b.append("maxItems=" + maxItems);
    
    // CCNB
    if (ccnb != null) {
      b.append("&ccnb=" + ccnb);
    }

    // ISBN
    if (isbn != null) {
      b.append("&isbn=" + isbn);
    }

    // ISSN
    if (issn != null) {
      b.append("&issn=" + issn);
    }

    // Record identifier
    if (recordIdentifier != null) {
      b.append("&recordIdentifier=" + recordIdentifier);
    }

    // Volume number
    if (volumeNumber != null) {
      final String encoding = "UTF8";
      try {	
		String encodedVolumeNumber = URLEncoder.encode(volumeNumber, encoding);
		b.append("&volumeNumber=" + encodedVolumeNumber);
      } catch (UnsupportedEncodingException e) {
		log.error(String.format("UnsupportedEncodingException during encodig of volumeNumber: %s, encoding: %s", volumeNumber, encoding), e);
      }
      
    }

    if (findVolume) {
      b.append("&findVolume=true");
    }
    
    return b.toString();
  }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UUIDFinder that = (UUIDFinder) o;

        if (findVolume != that.findVolume) return false;
        if (maxItems != that.maxItems) return false;
        if (ccnb != null ? !ccnb.equals(that.ccnb) : that.ccnb != null) return false;
        if (isbn != null ? !isbn.equals(that.isbn) : that.isbn != null) return false;
        if (issn != null ? !issn.equals(that.issn) : that.issn != null) return false;
        if (recordIdentifier != null ? !recordIdentifier.equals(that.recordIdentifier) : that.recordIdentifier != null)
            return false;
        if (volumeNumber != null ? !volumeNumber.equals(that.volumeNumber) : that.volumeNumber != null) return false;

        return true;
    }

    public String getCcnb() {
    return ccnb;
  }


  public void setCcnb(String ccnb) {
    this.ccnb = ccnb;
  }


  public String getIsbn() {
    return isbn;
  }


  public void setIsbn(String isbn) {
    this.isbn = isbn;
  }


  public String getIssn() {
    return issn;
  }


  public void setIssn(String issn) {
    this.issn = issn;
  }


  public String getRecordIdentifier() {
    return recordIdentifier;
  }


  public void setRecordIdentifier(String recordIdentifier) {
    this.recordIdentifier = recordIdentifier;
  }


  public String getVolumeNumber() {
    return volumeNumber;
  }


  public void setVolumeNumber(String volumeNumber) {
    this.volumeNumber = volumeNumber;
  }


  public boolean isFindVolume() {
    return findVolume;
  }


  public void setFindVolume(boolean findVolume) {
    this.findVolume = findVolume;
  }


  public int getMaxItems() {
    return maxItems;
  }


  public void setMaxItems(int maxItems) {
    this.maxItems = maxItems;
  }    

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.ccnb != null ? this.ccnb.hashCode() : 0);
        hash = 29 * hash + (this.isbn != null ? this.isbn.hashCode() : 0);
        hash = 29 * hash + (this.issn != null ? this.issn.hashCode() : 0);
        hash = 29 * hash + (this.recordIdentifier != null ? this.recordIdentifier.hashCode() : 0);
        hash = 29 * hash + (this.volumeNumber != null ? this.volumeNumber.hashCode() : 0);
        hash = 29 * hash + (this.findVolume ? 1 : 0);
        hash = 29 * hash + this.maxItems;
        return hash;
    }
    
    

    @Override
    public String toString() {
        return "UUIDFinder{" + "ccnb=" + ccnb + ", isbn=" + isbn + ", issn=" + issn + ", recordIdentifier=" + recordIdentifier + ", volumeNumber=" + volumeNumber + ", findVolume=" + findVolume + ", maxItems=" + maxItems + '}';
    }

    
  
}
