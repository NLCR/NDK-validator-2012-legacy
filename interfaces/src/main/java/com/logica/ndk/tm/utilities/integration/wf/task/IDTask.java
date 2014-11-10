package com.logica.ndk.tm.utilities.integration.wf.task;

import com.logica.ndk.tm.utilities.integration.wf.enumerator.Enumerator;

public class IDTask extends PackageTask {
  String cdmId;
  String uuid;
  public String getUuid() {
    return uuid;
  }
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }
  String url;
  Enumerator importType;
  String contractId;
  
  public String getUrl() {
    return url;
  }
  public void setUrl(String url) {
    this.url = url;
  }
  public Enumerator getImportType() {
    return importType;
  }
  public void setImportType(Enumerator importType) {
    this.importType = importType;
  }
  public String getContractId() {
    return contractId;
  }
  public void setContractId(String contractId) {
    this.contractId = contractId;
  }
  public String getCdmId() {
    return cdmId;
  }
  public void setCdmId(String cdmId) {
    this.cdmId = cdmId;
  }

  
  
}
