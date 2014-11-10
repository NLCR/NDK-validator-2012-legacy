package com.logica.ndk.tm.utilities.transformation.format.migration;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="formatMigrationScans")
public class FormatMigrationScans {

  private Integer targetDpi;
  private List<FormatMigrationScan> scans;

  public FormatMigrationScans() {
  }

  public FormatMigrationScans(List<FormatMigrationScan> scans, Integer targetDpi) {
    this.scans = scans;
    this.targetDpi = targetDpi;
  }

  public List<FormatMigrationScan> getScans() {
    return scans;
  }

  public void setScans(List<FormatMigrationScan> scans) {
    this.scans = scans;
  }

  public Integer getTargetDpi() {
    return targetDpi;
  }

  public void setTargetDpi(Integer targetDpi) {
    this.targetDpi = targetDpi;
  }
  
  
  
}
