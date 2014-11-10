package com.logica.ndk.tm.utilities.transformation.format.migration;

import java.util.List;

import com.logica.ndk.tm.utilities.jhove.MixEnvBean;

public class FormatMigrationScan {
  
  private Long scanNumber;
  private MixEnvBean envBean;
  private List<String> listOfFiles;
  

  public FormatMigrationScan() {
  }
  
  public FormatMigrationScan(Long scanNumber, MixEnvBean envBean, List<String> listOfFiles) {
    super();
    this.scanNumber = scanNumber;
    this.envBean = envBean;
    this.listOfFiles = listOfFiles;
  }

  public Long getScanNumber() {
    return scanNumber;
  }
  public void setScanNumber(Long scanNumber) {
    this.scanNumber = scanNumber;
  }
  public MixEnvBean getEnvBean() {
    return envBean;
  }
  public void setEnvBean(MixEnvBean envBean) {
    this.envBean = envBean;
  }
  public List<String> getListOfFiles() {
    return listOfFiles;
  }
  public void setListOfFiles(List<String> listOfFiles) {
    this.listOfFiles = listOfFiles;
  }
  
  
  
}
