package com.logica.ndk.tm.utilities.integration.wf.enumerator;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * WF activity representation
 * 
 * @author majdaf
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Activity extends Enumerator {

  String system;
  boolean trashed;

  //String final; -> ignored parameter because of the name as keyword
  String packageType;
  String accessRequired;
  String em;
  int order;
  

  public Activity() {
  }

  public Activity(Long id, String code) {
    super(id, code);
  }

  public String getSystem() {
    return system;
  }

  public void setSystem(String system) {
    this.system = system;
  }

  public boolean getTrashed() {
    return trashed;
  }

  public void setTrashed(boolean trashed) {
    this.trashed = trashed;
  }

  public String getPackageType() {
    return packageType;
  }

  public void setPackageType(String packageType) {
    this.packageType = packageType;
  }

  public String getAccessRequired() {
    return accessRequired;
  }

  public void setAccessRequired(String accessRequired) {
    this.accessRequired = accessRequired;
  }

  public String getEm() {
    return em;
  }

  public void setEm(String em) {
    this.em = em;
  }

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }
}
