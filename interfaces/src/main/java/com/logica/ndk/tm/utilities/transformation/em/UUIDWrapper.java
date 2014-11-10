/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.em;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kovalcikm
 */
public class UUIDWrapper implements Serializable {

  private static final long serialVersionUID = 1L;
  private List<UUID> uuidsList;

  public UUIDWrapper() {
    uuidsList = new ArrayList<UUID>();
  }

  public void addUuid(UUID uuid) {
    this.uuidsList.add(uuid);
  }

  public List<UUID> getUuidsList() {
    return uuidsList;
  }

  public void setUuidsList(List<UUID> uuidsList) {
    this.uuidsList = uuidsList;
  }

  @Override
  public String toString() {
    return "UUIDWrapper{" +
        "uuidsList=" + uuidsList +
        '}';
  }
}
