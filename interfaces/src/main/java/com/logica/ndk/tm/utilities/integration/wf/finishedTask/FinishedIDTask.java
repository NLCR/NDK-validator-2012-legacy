package com.logica.ndk.tm.utilities.integration.wf.finishedTask;

import com.logica.ndk.tm.utilities.integration.wf.enumerator.Enumerator;

/**
 * Finished task over import batch in WF
 * @author majdaf
 *
 */
public class FinishedIDTask extends FinishedTask {
  // Create batch params
  Enumerator importType;
  String url;
  
  // Finish upload params
  int iEntityCount;
  String pathId;
  String uuid;
  
  public Enumerator getImportType() {
    return importType;
  }
  public void setImportType(Enumerator importType) {
    this.importType = importType;
  }
  public String getUrl() {
    return url;
  }
  public void setUrl(String url) {
    this.url = url;
  }
  public int getIEntityCount() {
    return iEntityCount;
  }
  public void setIEntityCount(int entityCount) {
    iEntityCount = entityCount;
  }
  public String getPathId() {
    return pathId;
  }
  public void setPathId(String pathId) {
    this.pathId = pathId;
  }
  public String getUuid() {
    return uuid;
  }
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }
  

}
