package com.logica.ndk.tm.utilities.integration.wf.ping;

import java.util.Date;

/**
 * Ping safe representation
 * @author majdaf
 *
 */
public class Safe {
  String build;
  Date buildDate;
  String version;
  String name;
  boolean licenced;
  
  public String getBuild() {
    return build;
  }
  public void setBuild(String build) {
    this.build = build;
  }
  public Date getBuildDate() {
    return buildDate;
  }
  public void setBuildDate(Date buildDate) {
    this.buildDate = buildDate;
  }
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public boolean isLicenced() {
    return licenced;
  }
  public void setLicenced(boolean licenced) {
    this.licenced = licenced;
  }
}
