package com.logica.ndk.tm.log;

import java.io.Serializable;
import java.util.Date;

public class LogEvent implements Serializable {
  private static final long serialVersionUID = 7847488497828459115L;

  private long id;
  private String processInstanceId;
  private String nodeId;
  private String eventType;
  private String utilityName;
  private String message;
  private boolean exceptionWasThrown;
  private Long duration; // in milliseconds
  private Date created;

  protected LogEvent() {
  }

  public LogEvent(String processInstanceId, String nodeId, String eventType, String utilityName, String message, boolean exceptionWasThrown, Long duration, Date created) {
    super();
    this.processInstanceId = processInstanceId;
    this.nodeId = nodeId;
    this.eventType = eventType;
    this.utilityName = utilityName;
    this.message = message;
    this.exceptionWasThrown = exceptionWasThrown;
    this.duration = duration;
    this.created = created;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public String getNodeId() {
    return nodeId;
  }

  public void setNodeId(String nodeId) {
    this.nodeId = nodeId;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public String getUtilityName() {
    return utilityName;
  }

  public void setUtilityName(String utilityName) {
    this.utilityName = utilityName;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public boolean isExceptionWasThrown() {
    return exceptionWasThrown;
  }

  public void setExceptionWasThrown(boolean exceptionWasThrown) {
    this.exceptionWasThrown = exceptionWasThrown;
  }

  public Long getDuration() {
    return duration;
  }

  public void setDuration(Long duration) {
    this.duration = duration;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String toString() {
    return "LogEvent: {\n" +
        "processInstanceId: " + processInstanceId + ",\n" +
        "nodeId: " + nodeId + ",\n" +
        "eventType: " + eventType + ",\n" +
        "utilityName: " + utilityName + ",\n" +
        "message: " + message + ",\n" +
        "exceptionWasThrown: " + exceptionWasThrown + ",\n" +
        "duration: " + duration + " ms,\n" +
        "created: " + created + "\n" +
        "}";
  }
}
