/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.em;

import java.io.Serializable;

import com.logica.ndk.tm.utilities.integration.wf.task.UUIDResult;

/**
 * @author kovalcikm
 */
public class UUID implements Serializable, Comparable<UUID> {
  private static final long serialVersionUID = 1L;
  private String value;
  private String source;
  private String title = ""; //can be emtpy
  private String link = ""; //can be empty 
  private String volumeNumber;

  public UUID() {
  }

  public UUID(String value, String source, String title, String link, String volumeNumber) {
    super();
    this.value = value;
    this.source = source;
    this.title = title;
    this.link = link;
    this.volumeNumber = volumeNumber;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public String getVolumeNumber() {
    return volumeNumber;
  }

  public void setVolumeNumber(final String volumeNumber) {
    this.volumeNumber = volumeNumber;
  }

  @Override
  public String toString() {
    return "UUID{" +
        "value='" + value + '\'' +
        ", source='" + source + '\'' +
        ", title='" + title + '\'' +
        ", link='" + link + '\'' +
        ", volumeNumber='" + volumeNumber + '\'' +
        '}';
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    final UUID uuid = (UUID) o;

    if (link != null ? !link.equals(uuid.link) : uuid.link != null)
      return false;
    if (source != null ? !source.equals(uuid.source) : uuid.source != null)
      return false;
    if (title != null ? !title.equals(uuid.title) : uuid.title != null)
      return false;
    if (value != null ? !value.equals(uuid.value) : uuid.value != null)
      return false;
    if (volumeNumber != null ? !volumeNumber.equals(uuid.volumeNumber) : uuid.volumeNumber != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = value != null ? value.hashCode() : 0;
    result = 31 * result + (source != null ? source.hashCode() : 0);
    result = 31 * result + (title != null ? title.hashCode() : 0);
    result = 31 * result + (link != null ? link.hashCode() : 0);
    result = 31 * result + (volumeNumber != null ? volumeNumber.hashCode() : 0);
    return result;
  }

  @Override
  public int compareTo(UUID o) {
    // Not UUID
    if (!(o instanceof UUID)) {
      return 1;
    }
    UUID other = (UUID)o;
    
    // Check empty volumeNumber
    if (other.getVolumeNumber() == null && this.volumeNumber == null) {
      return 0;
    }
    if (other.getVolumeNumber() == null) {
      return 1;
    }
    if (this.volumeNumber == null) {
      return -1;
    }

    // Check non-numbers, if non-number, compare as string
    int otherVolumeNumber = 0;
    int thisVolumeNumber = 0;
    
    try {
      otherVolumeNumber = Integer.parseInt(other.volumeNumber);
    } catch (Exception e){
      return this.volumeNumber.compareTo(other.volumeNumber);
    }
    
    try {
      thisVolumeNumber = Integer.parseInt(this.volumeNumber);
    } catch (Exception e){
      return this.volumeNumber.compareTo(other.volumeNumber);
    }

    // Compare volumeNumbers
    if (thisVolumeNumber > otherVolumeNumber) {
      return 1;
    } else if (thisVolumeNumber < otherVolumeNumber) {
      return -1;
    } else {
      return 0;
    }
  }

}
