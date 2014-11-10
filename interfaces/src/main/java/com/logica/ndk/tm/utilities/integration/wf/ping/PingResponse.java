package com.logica.ndk.tm.utilities.integration.wf.ping;

import java.util.Date;

/**
 * WF Ping representation
 * @author majdaf
 *
 */
public class PingResponse {
  Date time;
  Safe safe;
  
  public Date getTime() {
    return time;
  }
  public void setTime(Date time) {
    this.time = time;
  }
  public Safe getSafe() {
    return safe;
  }
  public void setSafe(Safe safe) {
    this.safe = safe;
  }
}
