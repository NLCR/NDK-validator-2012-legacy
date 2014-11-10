package com.logica.ndk.tm.utilities.wa;

import java.io.Serializable;
import java.util.Date;

/**
 * Log event for processed WA.
 * 
 * @author Rudolf Daco
 */
public class WaLogEvent implements Serializable {

  private static final long serialVersionUID = -8410571371290382290L;

  private long id;
  private String cdmId;
  private long filesInWa;
  private Date created;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getCdmId() {
    return cdmId;
  }

  public void setCdmId(String cdmId) {
    this.cdmId = cdmId;
  }

  public long getFilesInWa() {
    return filesInWa;
  }

  public void setFilesInWa(long filesInWa) {
    this.filesInWa = filesInWa;
  }

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

}
