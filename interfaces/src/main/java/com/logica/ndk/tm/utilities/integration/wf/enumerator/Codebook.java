package com.logica.ndk.tm.utilities.integration.wf.enumerator;

import java.io.Serializable;

/**
 * WF codebook representation
 * @author majdaf
 *
 */
public class Codebook extends Enumerator implements Serializable {
  private static final long serialVersionUID = 1L;
  String cbType;
  Boolean trashed;
  
  public String getCbType() {
    return cbType;
  }
  public void setCbType(String cbType) {
    this.cbType = cbType;
  }
  public Boolean getTrashed() {
    return trashed;
  }
  public void setTrashed(Boolean trashed) {
    this.trashed = trashed;
  }
  
}
