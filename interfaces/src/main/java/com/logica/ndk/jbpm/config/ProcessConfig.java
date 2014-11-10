package com.logica.ndk.jbpm.config;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author brizat
 *
 */
@XmlRootElement
@XmlType(propOrder={"processId", "maxInstances", "priority", "stop", "errorStopTreshold" ,"errorStop"})
public class ProcessConfig {

  private String processId;
  private Integer maxInstances;
  private Boolean stop;
  private Boolean errorStop;
  private Integer errorStopTreshold;
  private Integer priority;

  public ProcessConfig() {
  }

  public String getProcessId() {
    return processId;
  }

  public void setProcessId(String name) {
    this.processId = name;
  }

  public Integer getMaxInstances() {
    return maxInstances;
  }

  public void setMaxInstances(Integer maxInstances) {
    this.maxInstances = maxInstances;
  }

  public Boolean getStop() {
    return stop;
  }

  public void setStop(Boolean stop) {
    this.stop = stop;
  }

  public Boolean getErrorStop() {
    return errorStop;
  }

  public void setErrorStop(Boolean errorStop) {
    this.errorStop = errorStop;
  }

  public Integer getErrorStopTreshold() {
    return errorStopTreshold;
  }

  public void setErrorStopTreshold(Integer errorStopTreshold) {
    this.errorStopTreshold = errorStopTreshold;
  }

  public Integer getPriority() {
    return priority;
  }

  public void setPriority(Integer priority) {
    this.priority = priority;
  }
  
}
