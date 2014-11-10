package com.logica.ndk.jbpm.core.integration.api;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @author Rudolf Daco
 *
 */
@Entity
public class ActiveWorkItem implements Serializable {
  private static final long serialVersionUID = -5066255844731748270L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;
  private long workItemId;
  private String workItemHandlerClass;
  private long processInstanceId;
  private String correlationId;
  @Temporal(TemporalType.TIMESTAMP)
  private Date created;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getWorkItemId() {
    return workItemId;
  }

  public void setWorkItemId(long workItemNameId) {
    this.workItemId = workItemNameId;
  }

  public String getWorkItemHandlerClass() {
    return workItemHandlerClass;
  }

  public void setWorkItemHandlerClass(String workItemHandlerClass) {
    this.workItemHandlerClass = workItemHandlerClass;
  }

  public long getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(long processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public String getCorrelationId() {
    return correlationId;
  }

  public void setCorrelationId(String correlationId) {
    this.correlationId = correlationId;
  }

  public String toString() {
    return "ActiveWorkItem: workItemHandlerClass: '" + workItemHandlerClass + "' workItemId:'" + workItemId + "' processInstanceId:'" + processInstanceId +
        "' correlationId:'" + correlationId + "'";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((created == null) ? 0 : created.hashCode());
    result = prime * result + (int) id;
    result = prime * result + (int) workItemId;
    result = prime * result + ((workItemHandlerClass == null) ? 0 : workItemHandlerClass.hashCode());;
    result = prime * result + (int) processInstanceId;
    result = prime * result + ((correlationId == null) ? 0 : correlationId.hashCode());;
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
    ActiveWorkItem other = (ActiveWorkItem) obj;
    if (created == null) {
      if (other.created != null)
        return false;
    }
    else if (!created.equals(other.created))
      return false;
    if (id != other.id)
      return false;
    if (workItemId != other.workItemId)
      return false;
    if (processInstanceId != other.processInstanceId)
      return false;
    if (workItemHandlerClass == null) {
      if (other.workItemHandlerClass != null)
        return false;
    }
    else if (!workItemHandlerClass.equals(other.workItemHandlerClass))
      return false;
    if (correlationId == null) {
      if (other.correlationId != null)
        return false;
    }
    else if (!correlationId.equals(other.correlationId))
      return false;
    return true;
  }
}
