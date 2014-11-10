package com.logica.ndk.jbpm.core.integration.api;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @author brizat
 */

@Entity
public class NotifyProcess {

  private static final long serialVersionUID = 5210l;

  @Id
  @GeneratedValue 
  private Long id;
  private Long processInstanceId;
  private Long workItemId;
  @Temporal(TemporalType.TIMESTAMP)
  private Date createdDate;
  @Temporal(TemporalType.TIMESTAMP)
  private Date notifyAt;
  private String state;

  public NotifyProcess() {
  }

  public NotifyProcess(Long processInstanceId, long workItemId, Date createdDate, Date notifyAt, String state) {
    this.id = null;
    this.processInstanceId = processInstanceId;
    this.workItemId = workItemId;
    this.createdDate = createdDate;
    this.notifyAt = notifyAt;
    this.state = state;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getprocessInstanceId() {
    return processInstanceId;
  }

  public void setprocessInstanceId(long processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public long getWorkItemId() {
    return workItemId;
  }

  public void setWorkItemId(long workItemId) {
    this.workItemId = workItemId;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public Date getNotifyAt() {
    return notifyAt;
  }

  public void setNotifyAt(Date notifyAt) {
    this.notifyAt = notifyAt;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((createdDate == null) ? 0 : createdDate.hashCode());
    result = prime * result + (int) (id ^ (id >>> 32));
    result = prime * result + ((notifyAt == null) ? 0 : notifyAt.hashCode());
    result = prime * result + (int) (processInstanceId ^ (processInstanceId >>> 32));
    result = prime * result + ((state == null) ? 0 : state.hashCode());
    result = prime * result + (int) (workItemId ^ (workItemId >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    NotifyProcess other = (NotifyProcess) obj;
    if (createdDate == null) {
      if (other.createdDate != null)
        return false;
    } 
    else if (!createdDate.equals(other.createdDate))
      return false;
    if (!id.equals(id))
      return false;
    if (notifyAt == null) {
      if (other.notifyAt != null)
        return false;
    }
    else if (!notifyAt.equals(other.notifyAt))
      return false;
    if (!processInstanceId.equals(other.processInstanceId))
      return false;
    if (state == null) {
      if (other.state != null)
        return false;
    }
    else if (!state.equals(other.state))
      return false;
    if (!workItemId.equals(other.workItemId))
      return false;
    return true;
  }

}
