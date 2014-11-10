/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.em;

import java.io.Serializable;

/**
 * @author kovalcikm
 */
public class WAIdentifierWrapper implements Serializable {
  private static final long serialVersionUID = 1L;

  private String titleUuid;
  private String tmHash;

  public WAIdentifierWrapper() {
  }

  public WAIdentifierWrapper(String titleUuid, String tmHash) {
    super();
    this.titleUuid = titleUuid;
    this.tmHash = tmHash;
  }

  public String getTitleUuid() {
    return titleUuid;
  }

  public void setTitleUuid(String titleUuid) {
    this.titleUuid = titleUuid;
  }

  public String getTmHash() {
    return tmHash;
  }

  public void setTmHash(String tmHash) {
    this.tmHash = tmHash;
  }

}
